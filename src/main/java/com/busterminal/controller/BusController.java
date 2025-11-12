package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/bus")
public class BusController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("list".equals(action))
            listBuses(request, response);
        else if ("edit".equals(action))
            editBusForm(request, response);
        else if ("view".equals(action))
            viewBusDetails(request, response);
        else if ("available".equals(action))
            listAvailableBuses(request, response);
        else
            listBuses(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("create".equals(action))
            addBus(request, response);
        else if ("update".equals(action))
            updateBus(request, response);
        else if ("delete".equals(action))
            deleteBus(request, response);
        else if ("changeStatus".equals(action))
            changeBusStatus(request, response);
    }

    private void listBuses(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Bus> buses = Bus.getAllBuses();

            List<Map<String, Object>> busesWithDetails = new ArrayList<>();
            for (Bus bus : buses) {
                Map<String, Object> busDetails = new HashMap<>();
                busDetails.put("bus", bus);

                // Get terminal name if available
                if (bus.currentTerminal > 0) {
                    Terminal terminal = new Terminal();
                    terminal.terminalID = bus.currentTerminal;
                    terminal.getRecord();
                    busDetails.put("terminalName", terminal.terminalName);
                } else {
                    busDetails.put("terminalName", "Not assigned");
                }

                // Get maintenance status
                busDetails.put("inMaintenance", "Maintenance".equals(bus.status));

                // Get scheduled trips count
                int scheduledTrips = getScheduledTripsCount(bus.busID);
                busDetails.put("scheduledTrips", scheduledTrips);

                busesWithDetails.add(busDetails);
            }

            // Get all terminals for the dropdown in the form
            List<Terminal> terminals = Terminal.getAllTerminals();

            request.setAttribute("busesWithDetails", busesWithDetails);
            request.setAttribute("terminals", terminals);
            request.setAttribute("buses", buses); // Keep original attribute for backward compatibility
            request.getRequestDispatcher("/admin/manage_bus.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving buses: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_bus.jsp")
                    .forward(request, response);
        }
    }

    private void listAvailableBuses(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Bus> buses = Bus.getAvailableBuses();

            List<Terminal> terminals = Terminal.getAllTerminals();

            request.setAttribute("buses", buses);
            request.setAttribute("terminals", terminals);
            request.getRequestDispatcher("/admin/available_buses.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving available buses: " + e.getMessage());
            request.getRequestDispatcher("/admin/available_buses.jsp")
                    .forward(request, response);
        }
    }

    private void editBusForm(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            Bus bus = new Bus();
            bus.busID = Integer.parseInt(request.getParameter("id"));

            if (bus.getRecord() == 1) {
                // Get all terminals for the dropdown
                List<Terminal> terminals = Terminal.getAllTerminals();

                request.setAttribute("bus", bus);
                request.setAttribute("terminals", terminals);
                request.setAttribute("editMode", true);
                request.getRequestDispatcher("/admin/manage_bus.jsp")
                        .forward(request, response);
            } else {
                request.setAttribute("error", "Bus not found");
                response.sendRedirect("bus?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving bus: " + e.getMessage());
            response.sendRedirect("bus?action=list");
        }
    }

    private void viewBusDetails(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            int busID = Integer.parseInt(request.getParameter("id"));
            Bus bus = new Bus();
            bus.busID = busID;

            if (bus.getRecord() == 1) {
                // Get terminal information
                Terminal terminal = new Terminal();
                if (bus.currentTerminal > 0) {
                    terminal.terminalID = bus.currentTerminal;
                    terminal.getRecord();
                }

                // Get maintenance history
                List<Map<String, Object>> maintenanceHistory = getMaintenanceHistory(busID);

                // Get scheduled trips
                List<Schedule> scheduledTrips = Schedule.getSchedulesByBusID(busID);
                List<Schedule> completedTrips = Schedule.getCompletedTripsByBusID(busID);

                request.setAttribute("scheduledTrips", scheduledTrips);
                request.setAttribute("completedTrips", completedTrips);

                // Get assigned staff
                List<Staff> assignedStaff = getAssignedStaff(busID);

                request.setAttribute("bus", bus);
                request.setAttribute("terminal", terminal);
                request.setAttribute("maintenanceHistory", maintenanceHistory);
                request.setAttribute("scheduledTrips", scheduledTrips);
                request.setAttribute("assignedStaff", assignedStaff);
                request.getRequestDispatcher("/admin/view_bus.jsp")
                        .forward(request, response);
            } else {
                request.setAttribute("error", "Bus not found");
                response.sendRedirect("bus?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving bus details: " + e.getMessage());
            response.sendRedirect("bus?action=list");
        }
    }

    private void addBus(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            // Validate input
            Map<String, String> validationErrors = validateBusInput(request);

            if (!validationErrors.isEmpty()) {
                request.setAttribute("validationErrors", validationErrors);
                request.setAttribute("busNumber", request.getParameter("busNumber"));
                request.setAttribute("capacity", request.getParameter("capacity"));
                request.setAttribute("status", request.getParameter("status"));
                request.setAttribute("currentTerminal", request.getParameter("currentTerminal"));

                // Get all terminals for the dropdown
                List<Terminal> terminals = Terminal.getAllTerminals();
                request.setAttribute("terminals", terminals);

                request.getRequestDispatcher("/admin/manage_bus.jsp")
                        .forward(request, response);
                return;
            }

            Bus bus = new Bus();
            bus.busNumber = request.getParameter("busNumber");
            bus.capacity = Integer.parseInt(request.getParameter("capacity"));
            bus.status = request.getParameter("status");

            String currentTerminalParam = request.getParameter("currentTerminal");
            bus.currentTerminal = (currentTerminalParam != null && !currentTerminalParam.isEmpty())
                    ? Integer.parseInt(currentTerminalParam)
                    : 0;

            if (bus.addRecord() == 1) {
                request.setAttribute("success", "Bus added successfully");
                response.sendRedirect("bus?action=list");
            } else {
                request.setAttribute("error", "Failed to add bus");
                request.getRequestDispatcher("/admin/manage_bus.jsp")
                        .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_bus.jsp")
                    .forward(request, response);
        }
    }

    private void updateBus(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            // Validate input
            Map<String, String> validationErrors = validateBusInput(request);

            if (!validationErrors.isEmpty()) {
                request.setAttribute("validationErrors", validationErrors);

                // Recreate bus object for form
                Bus bus = new Bus();
                bus.busID = Integer.parseInt(request.getParameter("busID"));
                bus.busNumber = request.getParameter("busNumber");
                bus.capacity = Integer.parseInt(request.getParameter("capacity"));
                bus.status = request.getParameter("status");

                String currentTerminalParam = request.getParameter("currentTerminal");
                bus.currentTerminal = (currentTerminalParam != null && !currentTerminalParam.isEmpty())
                        ? Integer.parseInt(currentTerminalParam)
                        : 0;

                // Get all terminals for the dropdown
                List<Terminal> terminals = Terminal.getAllTerminals();

                request.setAttribute("bus", bus);
                request.setAttribute("terminals", terminals);
                request.setAttribute("editMode", true);

                request.getRequestDispatcher("/admin/manage_bus.jsp")
                        .forward(request, response);
                return;
            }

            Bus bus = new Bus();
            bus.busID = Integer.parseInt(request.getParameter("busID"));
            bus.busNumber = request.getParameter("busNumber");
            bus.capacity = Integer.parseInt(request.getParameter("capacity"));
            bus.status = request.getParameter("status");

            String currentTerminalParam = request.getParameter("currentTerminal");
            bus.currentTerminal = (currentTerminalParam != null && !currentTerminalParam.isEmpty())
                    ? Integer.parseInt(currentTerminalParam)
                    : 0;

            // Before update, check if bus can change status (e.g., if it has scheduled
            // trips)
            Bus existingBus = new Bus();
            existingBus.busID = bus.busID;
            existingBus.getRecord();

            if (!existingBus.status.equals(bus.status)) {
                // Validate status change: Out of Order can only be set if status is Available
                // or Maintenance
                if (bus.status.equals("Out of Order")) {
                    if (!existingBus.status.equals("Available") && !existingBus.status.equals("Maintenance")) {
                        request.setAttribute("error", "Cannot set bus to 'Out of Order': " +
                                "Bus must be Available or in Maintenance status.");

                        bus = existingBus; // Revert to original status

                        // Get all terminals for the dropdown
                        List<Terminal> terminals = Terminal.getAllTerminals();

                        request.setAttribute("bus", bus);
                        request.setAttribute("terminals", terminals);
                        request.setAttribute("editMode", true);

                        request.getRequestDispatcher("/admin/manage_bus.jsp")
                                .forward(request, response);
                        return;
                    }
                }

                if (bus.status.equals("Maintenance")) {
                    // If changing to maintenance, check for scheduled trips
                    int scheduledTrips = getScheduledTripsCount(bus.busID);
                    if (scheduledTrips > 0) {
                        request.setAttribute("error", "Cannot set bus to maintenance: Bus has " +
                                scheduledTrips + " scheduled trips.");

                        bus = existingBus; // Revert to original status

                        // Get all terminals for the dropdown
                        List<Terminal> terminals = Terminal.getAllTerminals();

                        request.setAttribute("bus", bus);
                        request.setAttribute("terminals", terminals);
                        request.setAttribute("editMode", true);

                        request.getRequestDispatcher("/admin/manage_bus.jsp")
                                .forward(request, response);
                        return;
                    }

                    // If no scheduled trips, create a maintenance record
                    MaintenanceType defaultType = getDefaultMaintenanceType();
                    if (defaultType != null) {
                        Maintenance maintenance = new Maintenance();
                        maintenance.busID = bus.busID;
                        maintenance.maintenanceTypeID = defaultType.maintenanceTypeID;
                        maintenance.startingDate = new Timestamp(System.currentTimeMillis());
                        maintenance.addRecord();
                    }
                }
            }

            if (bus.modRecord() == 1) {
                request.setAttribute("success", "Bus updated successfully");
                response.sendRedirect("bus?action=list");
            } else {
                request.setAttribute("error", "Failed to update bus");
                request.setAttribute("bus", bus);
                request.getRequestDispatcher("/admin/manage_bus.jsp")
                        .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_bus.jsp")
                    .forward(request, response);
        }
    }

    private void deleteBus(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            int busID = Integer.parseInt(request.getParameter("id"));

            // Check if bus has scheduled trips
            int scheduledTrips = getScheduledTripsCount(busID);
            if (scheduledTrips > 0) {
                request.setAttribute("error", "Cannot delete bus: Bus has " +
                        scheduledTrips + " scheduled trips.");
                listBuses(request, response);
                return;
            }

            // Check if bus has active maintenance
            boolean hasActiveMaintenance = hasActiveMaintenance(busID);
            if (hasActiveMaintenance) {
                request.setAttribute("error", "Cannot delete bus: Bus has active maintenance records.");
                listBuses(request, response);
                return;
            }

            Bus bus = new Bus();
            bus.busID = busID;

            if (bus.delRecord() == 1) {
                request.setAttribute("success", "Bus deleted successfully");
                response.sendRedirect("bus?action=list");
            } else {
                request.setAttribute("error", "Failed to delete bus");
                response.sendRedirect("bus?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            response.sendRedirect("bus?action=list");
        }
    }

    private void changeBusStatus(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            int busID = Integer.parseInt(request.getParameter("id"));
            String newStatus = request.getParameter("status");

            // Validate status
            if (!isValidBusStatus(newStatus)) {
                request.setAttribute("error", "Invalid bus status");
                response.sendRedirect("bus?action=list");
                return;
            }

            Bus bus = new Bus();
            bus.busID = busID;

            if (bus.getRecord() == 1) {
                // Check status transition rules
                if (newStatus.equals("Maintenance")) {
                    // Check if bus has scheduled trips
                    int scheduledTrips = getScheduledTripsCount(busID);
                    if (scheduledTrips > 0) {
                        request.setAttribute("error", "Cannot set bus to maintenance: Bus has " +
                                scheduledTrips + " scheduled trips.");
                        response.sendRedirect("bus?action=list");
                        return;
                    }

                    // If no scheduled trips, create a maintenance record
                    MaintenanceType defaultType = getDefaultMaintenanceType();
                    if (defaultType != null) {
                        Maintenance maintenance = new Maintenance();
                        maintenance.busID = bus.busID;
                        maintenance.maintenanceTypeID = defaultType.maintenanceTypeID;
                        maintenance.startingDate = new Timestamp(System.currentTimeMillis());
                        maintenance.addRecord();
                    }
                }

                // Update status
                bus.status = newStatus;

                if (bus.modRecord() == 1) {
                    request.setAttribute("success", "Bus status updated successfully");
                } else {
                    request.setAttribute("error", "Failed to update bus status");
                }
            } else {
                request.setAttribute("error", "Bus not found");
            }

            response.sendRedirect("bus?action=list");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            response.sendRedirect("bus?action=list");
        }
    }

    // Helper methods
    private Map<String, String> validateBusInput(HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Validate bus number
        String busNumber = request.getParameter("busNumber");
        if (busNumber == null || busNumber.trim().isEmpty()) {
            errors.put("busNumber", "Bus number is required");
        } else if (busNumber.length() > 20) {
            errors.put("busNumber", "Bus number cannot exceed 20 characters");
        } else {
            // Check if bus number is unique (for new buses)
            String busIDParam = request.getParameter("busID");
            if (busIDParam == null || busIDParam.isEmpty()) {
                if (isBusNumberExists(busNumber)) {
                    errors.put("busNumber", "Bus number already exists");
                }
            } else {
                // For updates, check if the bus number belongs to another bus
                int busID = Integer.parseInt(busIDParam);
                if (isBusNumberExistsForOtherBus(busNumber, busID)) {
                    errors.put("busNumber", "Bus number already exists for another bus");
                }
            }
        }

        // Validate capacity
        String capacityStr = request.getParameter("capacity");
        if (capacityStr == null || capacityStr.trim().isEmpty()) {
            errors.put("capacity", "Capacity is required");
        } else {
            try {
                int capacity = Integer.parseInt(capacityStr);
                if (capacity < 10 || capacity > 100) {
                    errors.put("capacity", "Capacity must be between 10 and 100");
                }
            } catch (NumberFormatException e) {
                errors.put("capacity", "Capacity must be a number");
            }
        }

        // Validate status
        String status = request.getParameter("status");
        if (status == null || status.trim().isEmpty()) {
            errors.put("status", "Status is required");
        } else if (!isValidBusStatus(status)) {
            errors.put("status", "Invalid status value");
        }

        // Terminal validation is optional as it can be NULL

        return errors;
    }

    private boolean isValidBusStatus(String status) {
        if (status != null) {
            status = status.trim(); // Trim any whitespace
        }
        return status != null &&
                (status.equals("Available") ||
                        status.equals("In Transit") ||
                        status.equals("Scheduled") ||
                        status.equals("Maintenance") ||
                        status.equals("Out of Order"));
    }

    private boolean isBusNumberExists(String busNumber) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM Bus WHERE bus_number = ?");
            pStmt.setString(1, busNumber);
            ResultSet rs = pStmt.executeQuery();

            boolean exists = false;
            if (rs.next()) {
                exists = rs.getInt("count") > 0;
            }

            rs.close();
            pStmt.close();
            conn.close();

            return exists;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean isBusNumberExistsForOtherBus(String busNumber, int busID) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM Bus WHERE bus_number = ? AND bus_id != ?");
            pStmt.setString(1, busNumber);
            pStmt.setInt(2, busID);
            ResultSet rs = pStmt.executeQuery();

            boolean exists = false;
            if (rs.next()) {
                exists = rs.getInt("count") > 0;
            }

            rs.close();
            pStmt.close();
            conn.close();

            return exists;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private int getScheduledTripsCount(int busID) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM Schedule " +
                            "WHERE bus_id = ? AND status IN ('Scheduled', 'Departed')");
            pStmt.setInt(1, busID);
            ResultSet rs = pStmt.executeQuery();

            int count = 0;
            if (rs.next()) {
                count = rs.getInt("count");
            }

            rs.close();
            pStmt.close();
            conn.close();

            return count;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    private boolean hasActiveMaintenance(int busID) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM Maintenance " +
                            "WHERE bus_id = ? AND completion_time IS NULL");
            pStmt.setInt(1, busID);
            ResultSet rs = pStmt.executeQuery();

            boolean hasActive = false;
            if (rs.next()) {
                hasActive = rs.getInt("count") > 0;
            }

            rs.close();
            pStmt.close();
            conn.close();

            return hasActive;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private List<Map<String, Object>> getMaintenanceHistory(int busID) {
        List<Map<String, Object>> history = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT m.*, mt.type_name, mt.maintenance_cost, s.staff_name " +
                            "FROM Maintenance m " +
                            "LEFT JOIN Maintenance_Type mt ON m.maintenance_type_id = mt.maintenance_type_id " +
                            "LEFT JOIN Staff s ON m.assigned_mechanic = s.staff_id " +
                            "WHERE m.bus_id = ? " +
                            "ORDER BY m.starting_date DESC");
            pStmt.setInt(1, busID);
            ResultSet rs = pStmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> record = new HashMap<>();
                record.put("maintenance_id", rs.getInt("maintenance_id"));
                record.put("starting_date", rs.getTimestamp("starting_date"));
                record.put("completion_time", rs.getTimestamp("completion_time"));
                record.put("type_name", rs.getString("type_name"));
                record.put("maintenance_cost", rs.getDouble("maintenance_cost"));
                record.put("mechanic_name", rs.getString("staff_name"));

                // Calculate duration if maintenance is complete
                if (rs.getTimestamp("completion_time") != null) {
                    long durationMillis = rs.getTimestamp("completion_time").getTime() -
                            rs.getTimestamp("starting_date").getTime();
                    double durationHours = durationMillis / (1000.0 * 60 * 60);
                    record.put("duration_hours", durationHours);
                }

                history.add(record);
            }

            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return history;
    }

    private List<Staff> getAssignedStaff(int busID) {
        List<Staff> staffList = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT s.*, r.role_name FROM Staff s " +
                            "JOIN Role r ON s.role_id = r.role_id " +
                            "WHERE s.assigned_bus = ?");
            pStmt.setInt(1, busID);
            ResultSet rs = pStmt.executeQuery();

            while (rs.next()) {
                Staff staff = new Staff();
                staff.staffID = rs.getInt("staff_id");
                staff.staffName = rs.getString("staff_name");
                staff.roleID = rs.getInt("role_id");
                staff.assignedTerminal = rs.getInt("assigned_terminal");
                staff.assignedBus = rs.getInt("assigned_bus");
                staff.shift = rs.getString("shift");
                staff.contact = rs.getString("contact");
                staffList.add(staff);
            }

            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return staffList;
    }

    private MaintenanceType getDefaultMaintenanceType() {
        List<MaintenanceType> types = MaintenanceType.getAllMaintenanceTypes();
        if (!types.isEmpty()) {
            return types.get(0); // Return first maintenance type as default
        }
        return null;
    }
}

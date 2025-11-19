package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

@WebServlet("/maintenance")
public class MaintenanceController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("list".equals(action))
            listMaintenanceRecords(request, response);
        else if ("new".equals(action))
            showNewMaintenanceForm(request, response);
        else if ("view".equals(action))
            viewMaintenanceDetails(request, response);
        else if ("edit".equals(action)) {
            showEditMaintenanceForm(request, response);
        } else
            listMaintenanceRecords(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("create".equals(action))
            createMaintenance(request, response);
        else if ("complete".equals(action))
            completeMaintenance(request, response);
        else if ("createType".equals(action))
            createMaintenanceType(request, response);
        else if ("update".equals(action))
            updateMaintenance(request, response);
        else if ("delete".equals(action))
            deleteMaintenance(request, response);
    }

    private void listMaintenanceRecords(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            String filterStatus = request.getParameter("status");

            Connection conn = DBConnection.getConnection();
            StringBuilder query = new StringBuilder(
                    "SELECT m.*, b.bus_number, mt.type_name, mt.maintenance_cost, s.staff_name " +
                            "FROM Maintenance m " +
                            "JOIN Bus b ON m.bus_id = b.bus_id " +
                            "LEFT JOIN Maintenance_Type mt ON m.maintenance_type_id = mt.maintenance_type_id " +
                            "LEFT JOIN Staff s ON m.assigned_mechanic = s.staff_id ");

            // Apply filter if requested
            if ("active".equals(filterStatus)) {
                query.append("WHERE m.completion_time IS NULL ");
            } else if ("completed".equals(filterStatus)) {
                query.append("WHERE m.completion_time IS NOT NULL ");
            }

            query.append("ORDER BY m.starting_date DESC");

            PreparedStatement pStmt = conn.prepareStatement(query.toString());
            ResultSet rs = pStmt.executeQuery();

            List<Map<String, Object>> maintenanceRecords = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> record = new HashMap<>();
                record.put("maintenance_id", rs.getInt("maintenance_id"));
                record.put("bus_id", rs.getInt("bus_id"));
                record.put("bus_number", rs.getString("bus_number"));
                record.put("assigned_mechanic", rs.getInt("assigned_mechanic"));
                record.put("mechanic_name", rs.getString("staff_name"));
                record.put("maintenance_type_id", rs.getInt("maintenance_type_id"));
                record.put("type_name", rs.getString("type_name"));
                record.put("maintenance_cost", rs.getDouble("maintenance_cost"));
                record.put("starting_date", rs.getTimestamp("starting_date"));
                record.put("completion_time", rs.getTimestamp("completion_time"));

                // Calculate duration if maintenance is complete
                if (rs.getTimestamp("completion_time") != null) {
                    long durationMillis = rs.getTimestamp("completion_time").getTime() -
                            rs.getTimestamp("starting_date").getTime();
                    double durationHours = durationMillis / (1000.0 * 60 * 60);
                    record.put("duration_hours", durationHours);
                }

                maintenanceRecords.add(record);
            }

            rs.close();
            pStmt.close();
            conn.close();

            request.setAttribute("maintenanceRecords", maintenanceRecords);
            request.setAttribute("filterStatus", filterStatus);
            request.getRequestDispatcher("/admin/maintenance_list.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving maintenance records: " + e.getMessage());
            request.getRequestDispatcher("/admin/maintenance_list.jsp")
                    .forward(request, response);
        }
    }

    private void showNewMaintenanceForm(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get buses that are not in maintenance
            List<Bus> availableBuses = getAvailableBuses();

            // Get maintenance types
            List<MaintenanceType> maintenanceTypes = MaintenanceType.getAllMaintenanceTypes();

            // Get mechanics
            List<Staff> mechanics = getMechanics();

            // Check if a specific bus was requested
            String busIDParam = request.getParameter("busID");
            if (busIDParam != null && !busIDParam.trim().isEmpty()) {
                int busID = Integer.parseInt(busIDParam);
                Bus selectedBus = new Bus();
                selectedBus.busID = busID;
                selectedBus.getRecord();
                request.setAttribute("selectedBus", selectedBus);
            }

            request.setAttribute("buses", availableBuses);
            request.setAttribute("maintenanceTypes", maintenanceTypes);
            request.setAttribute("mechanics", mechanics);

            request.getRequestDispatcher("/admin/maintenance_list.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading maintenance form: " + e.getMessage());
            response.sendRedirect("maintenance?action=list");
        }
    }

    private void viewMaintenanceDetails(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            int maintenanceID = Integer.parseInt(request.getParameter("id"));

            // Get maintenance details with all related information
            Map<String, Object> maintenanceDetails = Maintenance.getMaintenanceDetails(maintenanceID);

            if (maintenanceDetails.isEmpty()) {
                request.setAttribute("error", "Maintenance record not found");
                response.sendRedirect("maintenance?action=list");
                return;
            }

            request.setAttribute("maintenance", maintenanceDetails);
            request.getRequestDispatcher("/admin/maintenance?action=list.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving maintenance details: " + e.getMessage());
            response.sendRedirect("maintenance?action=list");
        }
    }

    private void showCompleteMaintenanceForm(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            int maintenanceID = Integer.parseInt(request.getParameter("id"));

            Maintenance maintenance = new Maintenance();
            maintenance.maintenanceID = maintenanceID;

            if (maintenance.getRecord() == 1) {
                if (maintenance.completionTime != null) {
                    request.setAttribute("error", "This maintenance record is already completed");
                    response.sendRedirect("maintenance?action=view&id=" + maintenanceID);
                    return;
                }

                // Get bus details
                Bus bus = new Bus();
                bus.busID = maintenance.busID;
                bus.getRecord();

                // Get maintenance type details
                MaintenanceType type = new MaintenanceType();
                type.maintenanceTypeID = maintenance.maintenanceTypeID;
                type.getRecord();

                // Get mechanic details if assigned
                Staff mechanic = null;
                if (maintenance.assignedMechanic > 0) {
                    mechanic = new Staff();
                    mechanic.staffID = maintenance.assignedMechanic;
                    mechanic.getRecord();
                }

                // Get all mechanics for reassignment if needed
                List<Staff> mechanics = getMechanics();

                request.setAttribute("maintenance", maintenance);
                request.setAttribute("bus", bus);
                request.setAttribute("maintenanceType", type);
                request.setAttribute("assignedMechanic", mechanic);
                request.setAttribute("mechanics", mechanics);

                request.getRequestDispatcher("/admin/complete_maintenance.jsp")
                        .forward(request, response);
            } else {
                request.setAttribute("error", "Maintenance record not found");
                response.sendRedirect("maintenance?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading completion form: " + e.getMessage());
            response.sendRedirect("maintenance?action=list");
        }
    }

    private void listMaintenanceTypes(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            List<MaintenanceType> types = MaintenanceType.getAllMaintenanceTypes();

            // For each type, check if it's in use
            Map<Integer, Boolean> typeUsage = new HashMap<>();
            for (MaintenanceType type : types) {
                boolean inUse = isMaintenanceTypeInUse(type.maintenanceTypeID);
                typeUsage.put(type.maintenanceTypeID, inUse);
            }

            request.setAttribute("maintenanceTypes", types);
            request.setAttribute("typeUsage", typeUsage);

            request.getRequestDispatcher("/admin/maintenance_types.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving maintenance types: " + e.getMessage());
            request.getRequestDispatcher("/admin/maintenance_types.jsp")
                    .forward(request, response);
        }
    }

    private void showNewMaintenanceTypeForm(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/admin/new_maintenance_type.jsp")
                .forward(request, response);
    }

    private void showEditMaintenanceForm(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            int maintenanceID = Integer.parseInt(request.getParameter("id"));

            Maintenance m = new Maintenance();
            m.maintenanceID = maintenanceID;

            if (m.getRecord() == 1) {
                // Fetch dropdown data
                List<Bus> buses = Bus.getAllBuses();
                List<Staff> mechanics = Staff.getStaffByRole(4);
                List<MaintenanceType> types = MaintenanceType.getAllMaintenanceTypes();

                request.setAttribute("editMaintenance", m);
                request.setAttribute("buses", buses);
                request.setAttribute("mechanics", mechanics);
                request.setAttribute("maintenanceTypes", types);

                // Forward back to the list JSP
                request.getRequestDispatcher("/admin/maintenance_list.jsp")
                        .forward(request, response);
            } else {
                request.setAttribute("error", "Maintenance record not found");
                response.sendRedirect(request.getContextPath() + "/maintenance?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/maintenance?action=list");
        }
    }

    private void createMaintenance(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        try {
            int busID = Integer.parseInt(request.getParameter("busID"));
            int maintenanceTypeID = Integer.parseInt(request.getParameter("maintenanceTypeID"));
            String startingDateStr = request.getParameter("startingDate");
            String startingTimeStr = request.getParameter("startingTime");

            String mechanicIDStr = request.getParameter("mechanicID");
            Integer mechanicID = null;
            if (mechanicIDStr != null && !mechanicIDStr.trim().isEmpty()) {
                mechanicID = Integer.parseInt(mechanicIDStr);
            }

            if (mechanicID == null) {
                request.setAttribute("error", "Mechanic assignment is required");
                showNewMaintenanceForm(request,response);
                return;
            }

            // Start transaction
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. VALIDATE BUS EXISTS AND CHECK STATUS
            PreparedStatement busCheckStmt = conn.prepareStatement(
                    "SELECT status FROM Bus WHERE bus_id = ? FOR UPDATE");
            busCheckStmt.setInt(1, busID);
            ResultSet busRs = busCheckStmt.executeQuery();

            if (!busRs.next()) {
                conn.rollback();
                request.setAttribute("error", "Bus not found");
                showNewMaintenanceForm(request, response);
                return;
            }

            String busStatus = busRs.getString("status");
            busRs.close();
            busCheckStmt.close();

            // 2. VALIDATE STATUS (Available or Maintenance)
            if (!"Available".equals(busStatus) && !"Maintenance".equals(busStatus)) {
                conn.rollback();
                request.setAttribute("error",
                        "Maintenance can only be performed on buses with 'Available' or 'Maintenance' status. " +
                                "Current status: " + busStatus);
                showNewMaintenanceForm(request, response);
                return;
            }

            // 2a. VALIDATE MECHANIC REQUIRED AND CHECK FOR DUPLICATES
            String startingDateTime = startingDateStr + " " + startingTimeStr + ":00";
            Timestamp startingDate = Timestamp.valueOf(startingDateTime);
            String duplicateCheckQuery = "SELECT COUNT(*) as count FROM Maintenance WHERE " +
                    "bus_id = ? AND maintenance_type_id = ? AND starting_date = ?";

            PreparedStatement duplicateCheckStmt = conn.prepareStatement(duplicateCheckQuery);
            duplicateCheckStmt.setInt(1, busID);
            duplicateCheckStmt.setInt(2, maintenanceTypeID);
            duplicateCheckStmt.setTimestamp(3, startingDate);

            ResultSet duplicateRs = duplicateCheckStmt.executeQuery();
            boolean isDuplicate = false;
            if (duplicateRs.next()) {
                isDuplicate = duplicateRs.getInt("count") > 0;
            }
            duplicateRs.close();
            duplicateCheckStmt.close();

            if (isDuplicate) {
                conn.rollback();
                request.setAttribute("error",
                        "A maintenance record already exists for this maintenance type on this bus at this date/time.");
                showNewMaintenanceForm(request, response);
                return;
            }

            // 3. CHECK FOR SCHEDULED TRIPS AND HANDLE CASCADE CANCELLATION IF ANY
            // Only check for scheduled trips if bus is Available (not already in
            // Maintenance)
            List<Integer> scheduleIDs = new ArrayList<>();
            if ("Available".equals(busStatus)) {
                PreparedStatement schedulesStmt = conn.prepareStatement(
                        "SELECT schedule_id FROM Schedule " +
                                "WHERE bus_id = ? AND status IN ('Scheduled') " +
                                "AND departure_time > NOW()");
                schedulesStmt.setInt(1, busID);
                ResultSet schedulesRs = schedulesStmt.executeQuery();

                while (schedulesRs.next()) {
                    scheduleIDs.add(schedulesRs.getInt("schedule_id"));
                }
                schedulesRs.close();
                schedulesStmt.close();
            }

            // If there are scheduled trips, cancel them
            if (!scheduleIDs.isEmpty()) {
                // 3a. For each schedule, delete tickets
                for (Integer scheduleID : scheduleIDs) {
                    PreparedStatement deleteTicketsStmt = conn.prepareStatement(
                            "DELETE FROM Ticket WHERE schedule_id = ?");
                    deleteTicketsStmt.setInt(1, scheduleID);
                    int deletedTickets = deleteTicketsStmt.executeUpdate();
                    deleteTicketsStmt.close();

                    System.out.println("Deleted " + deletedTickets + " tickets for schedule " + scheduleID);
                }

                // 3b. Cancel all future schedules
                for (Integer scheduleID : scheduleIDs) {
                    PreparedStatement cancelScheduleStmt = conn.prepareStatement(
                            "UPDATE Schedule SET status = 'Cancelled' WHERE schedule_id = ?");
                    cancelScheduleStmt.setInt(1, scheduleID);
                    cancelScheduleStmt.executeUpdate();
                    cancelScheduleStmt.close();
                }

                // 3c. Clear staff assignments from this bus
                PreparedStatement clearStaffStmt = conn.prepareStatement(
                        "UPDATE Staff SET assigned_bus = NULL WHERE assigned_bus = ?");
                clearStaffStmt.setInt(1, busID);
                int clearedStaff = clearStaffStmt.executeUpdate();
                clearStaffStmt.close();

                System.out.println("Cleared " + clearedStaff + " staff assignments from bus " + busID);
                System.out.println("Cancelled " + scheduleIDs.size() + " schedules due to maintenance");
            }

            // 4. CREATE MAINTENANCE RECORD
            startingDateTime = startingDateStr + " " + startingTimeStr + ":00";
            startingDate = Timestamp.valueOf(startingDateTime);

            PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO Maintenance (bus_id, maintenance_type_id, assigned_mechanic, starting_date) " +
                            "VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            insertStmt.setInt(1, busID);
            insertStmt.setInt(2, maintenanceTypeID);
            insertStmt.setInt(3, mechanicID);
            insertStmt.setTimestamp(4, startingDate);

            int result = insertStmt.executeUpdate();

            if (result != 1) {
                conn.rollback();
                request.setAttribute("error", "Failed to create maintenance record");
                showNewMaintenanceForm(request, response);
                return;
            }

            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            int maintenanceID = 0;
            if (generatedKeys.next()) {
                maintenanceID = generatedKeys.getInt(1);
            }
            generatedKeys.close();
            insertStmt.close();

            // 5. UPDATE BUS STATUS TO 'Maintenance' (only if not already in Maintenance)
            if (!"Maintenance".equals(busStatus)) {
                PreparedStatement updateBusStmt = conn.prepareStatement(
                        "UPDATE Bus SET status = 'Maintenance' WHERE bus_id = ?");
                updateBusStmt.setInt(1, busID);
                updateBusStmt.executeUpdate();
                updateBusStmt.close();
            }

            // 6. COMMIT TRANSACTION
            conn.commit();

            // Success message with cascade info - use session for redirect
            HttpSession session = request.getSession();
            if (!scheduleIDs.isEmpty()) {
                session.setAttribute("message",
                        "Maintenance created successfully. " +
                                "Note: Bus had scheduled trips, so " + scheduleIDs.size()
                                + " schedule(s) have been cancelled.");
            } else {
                session.setAttribute("message", "Maintenance created successfully");
            }

            response.sendRedirect(request.getContextPath() + "/maintenance?action=list");
        } catch (NumberFormatException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
            e.printStackTrace();
            request.setAttribute("error", "Invalid input format");
            showNewMaintenanceForm(request, response);
        } catch (IllegalArgumentException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
            e.printStackTrace();
            request.setAttribute("error", "Invalid date/time format");
            showNewMaintenanceForm(request, response);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
            e.printStackTrace();
            request.setAttribute("error", "Database error: " + e.getMessage());
            showNewMaintenanceForm(request, response);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
            e.printStackTrace();
            request.setAttribute("error", "Error creating maintenance: " + e.getMessage());
            showNewMaintenanceForm(request, response);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void completeMaintenance(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            int maintenanceID = Integer.parseInt(request.getParameter("maintenanceID"));
            String assignedMechanicParam = request.getParameter("assignedMechanic");
            int assignedMechanic = 0;
            if (assignedMechanicParam != null && !assignedMechanicParam.trim().isEmpty()) {
                assignedMechanic = Integer.parseInt(assignedMechanicParam);
            }

            Maintenance maintenance = new Maintenance();
            maintenance.maintenanceID = maintenanceID;

            // Load the record from DB
            if (maintenance.getRecord() != 1) {
                request.setAttribute("error", "Maintenance record not found");
                request.getRequestDispatcher("/admin/maintenance_list.jsp").forward(request, response);
                return;
            }

            // Check if already completed
            if (maintenance.completionTime != null) {
                request.setAttribute("error", "This maintenance record is already completed");
                request.getRequestDispatcher("/admin/maintenance_list.jsp").forward(request, response);
                return;
            }

            // Update mechanic if changed
            if (assignedMechanic > 0 && assignedMechanic != maintenance.assignedMechanic) {
                maintenance.assignedMechanic = assignedMechanic;
            }

            // Complete the maintenance
            if (maintenance.completeMaintenance() == 1) {
                request.setAttribute("success", "Maintenance record completed successfully");
            } else {
                request.setAttribute("error", "Failed to complete maintenance record");
            }

            // Forward back to the list page without reloading all maintenances
            request.getRequestDispatcher("/admin/maintenance_list.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error completing maintenance record: " + e.getMessage());
            request.getRequestDispatcher("/admin/maintenance_list.jsp").forward(request, response);
        }
    }

    private void createMaintenanceType(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            // Validate input
            Map<String, String> validationErrors = validateMaintenanceTypeInput(request);

            if (!validationErrors.isEmpty()) {
                request.setAttribute("validationErrors", validationErrors);
                request.setAttribute("typeName", request.getParameter("typeName"));
                request.setAttribute("maintenanceCost", request.getParameter("maintenanceCost"));

                request.getRequestDispatcher("/admin/new_maintenance_type.jsp")
                        .forward(request, response);
                return;
            }

            String typeName = request.getParameter("typeName");
            double maintenanceCost = Double.parseDouble(request.getParameter("maintenanceCost"));

            MaintenanceType type = new MaintenanceType();
            type.typeName = typeName;
            type.maintenanceCost = maintenanceCost;

            if (type.addRecord() == 1) {
                request.setAttribute("success", "Maintenance type created successfully");
                response.sendRedirect("maintenance?action=types");
            } else {
                request.setAttribute("error", "Failed to create maintenance type");
                request.getRequestDispatcher("/admin/new_maintenance_type.jsp")
                        .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error creating maintenance type: " + e.getMessage());
            request.getRequestDispatcher("/admin/new_maintenance_type.jsp")
                    .forward(request, response);
        }
    }

    private void updateMaintenance(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            Maintenance m = new Maintenance();
            m.maintenanceID = Integer.parseInt(request.getParameter("maintenanceID"));
            m.busID = Integer.parseInt(request.getParameter("busID"));
            m.assignedMechanic = Integer.parseInt(request.getParameter("assignedMechanic"));
            m.maintenanceTypeID = Integer.parseInt(request.getParameter("maintenanceTypeID"));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

            // Starting date (required)
            String startingDateStr = request.getParameter("startingDate");
            if (startingDateStr == null || startingDateStr.isEmpty()) {
                request.setAttribute("error", "Starting date is required.");
                request.setAttribute("editMaintenance", m);
                request.setAttribute("buses", Bus.getAllBuses());
                request.setAttribute("mechanics", Staff.getStaffByRole(4));
                request.setAttribute("maintenanceTypes", MaintenanceType.getAllMaintenanceTypes());
                request.getRequestDispatcher("/admin/maintenance_list.jsp").forward(request, response);
                return;
            }
            m.startingDate = new Timestamp(sdf.parse(startingDateStr).getTime());

            // Completion date (optional)
            String completionTimeStr = request.getParameter("completionTime");
            if (completionTimeStr != null && !completionTimeStr.isEmpty()) {
                m.completionTime = new Timestamp(sdf.parse(completionTimeStr).getTime());
            }

            // Mechanic assignment validation
            if (m.assignedMechanic <= 0) {
                request.setAttribute("error", "Please assign a mechanic.");
                request.setAttribute("editMaintenance", m);
                request.setAttribute("buses", Bus.getAllBuses());
                request.setAttribute("mechanics", Staff.getStaffByRole(4));
                request.setAttribute("maintenanceTypes", MaintenanceType.getAllMaintenanceTypes());
                request.getRequestDispatcher("/admin/maintenance_list.jsp").forward(request, response);
                return;
            }

            // Update record
            if (m.modRecord() == 1) {
                response.sendRedirect(request.getContextPath() + "/maintenance?action=list");
            } else {
                request.setAttribute("error", "Failed to update maintenance record.");
                request.setAttribute("editMaintenance", m);
                request.setAttribute("buses", Bus.getAllBuses());
                request.setAttribute("mechanics", Staff.getStaffByRole(4));
                request.setAttribute("maintenanceTypes", MaintenanceType.getAllMaintenanceTypes());
                request.getRequestDispatcher("/admin/maintenance_list.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.setAttribute("editMaintenance", new Maintenance());
            request.setAttribute("buses", Bus.getAllBuses());
            request.setAttribute("mechanics", Staff.getStaffByRole(4));
            request.setAttribute("maintenanceTypes", MaintenanceType.getAllMaintenanceTypes());
            request.getRequestDispatcher("/admin/maintenance_list.jsp").forward(request, response);
        }
    }

    private void deleteMaintenance(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            Maintenance m = new Maintenance();
            m.maintenanceID = Integer.parseInt(request.getParameter("id"));

            if (m.delRecord() == 1) {
                request.setAttribute("success", "Maintenance type deleted successfully");
                response.sendRedirect("maintenance?action=list");
            } else {
                request.setAttribute("error", "Failed to delete maintenance type");
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error deleting maintenance: " + e.getMessage());
            response.sendRedirect("maintenance?action=list");
        }
    }

    // Helper methods
    private Map<String, String> validateMaintenanceInput(HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Validate bus
        String busIDStr = request.getParameter("busID");
        if (busIDStr == null || busIDStr.trim().isEmpty()) {
            errors.put("busID", "Bus is required");
        } else {
            try {
                int busID = Integer.parseInt(busIDStr);

                // Check if bus exists and is available
                Bus bus = new Bus();
                bus.busID = busID;

                if (bus.getRecord() != 1) {
                    errors.put("busID", "Bus not found");
                } else if (!"Available".equals(bus.status) && !"Maintenance".equals(bus.status)) {
                    errors.put("busID", "Bus is not available for maintenance");
                }

                // Check if bus has scheduled trips
                if ("Available".equals(bus.status)) {
                    int scheduledTrips = getScheduledTripsCount(busID);
                    if (scheduledTrips > 0) {
                        errors.put("busID", "Bus has " + scheduledTrips + " scheduled trips");
                    }
                }
            } catch (NumberFormatException e) {
                errors.put("busID", "Invalid bus ID format");
            }
        }

        // Validate maintenance type
        String maintenanceTypeIDStr = request.getParameter("maintenanceTypeID");
        if (maintenanceTypeIDStr == null || maintenanceTypeIDStr.trim().isEmpty()) {
            errors.put("maintenanceTypeID", "Maintenance type is required");
        } else {
            try {
                int maintenanceTypeID = Integer.parseInt(maintenanceTypeIDStr);

                // Check if maintenance type exists
                MaintenanceType type = new MaintenanceType();
                type.maintenanceTypeID = maintenanceTypeID;

                if (type.getRecord() != 1) {
                    errors.put("maintenanceTypeID", "Maintenance type not found");
                }
            } catch (NumberFormatException e) {
                errors.put("maintenanceTypeID", "Invalid maintenance type ID format");
            }
        }

        // Validate assigned mechanic (optional)
        String assignedMechanicStr = request.getParameter("assignedMechanic");
        if (assignedMechanicStr != null && !assignedMechanicStr.trim().isEmpty()) {
            try {
                int assignedMechanic = Integer.parseInt(assignedMechanicStr);

                // Check if mechanic exists and is a mechanic
                if (assignedMechanic > 0 && !isMechanic(assignedMechanic)) {
                    errors.put("assignedMechanic", "Selected staff is not a mechanic");
                }
            } catch (NumberFormatException e) {
                errors.put("assignedMechanic", "Invalid mechanic ID format");
            }
        }

        return errors;
    }

    private Map<String, String> validateMaintenanceTypeInput(HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Validate type name
        String typeName = request.getParameter("typeName");
        if (typeName == null || typeName.trim().isEmpty()) {
            errors.put("typeName", "Type name is required");
        } else if (typeName.length() > 100) {
            errors.put("typeName", "Type name cannot exceed 100 characters");
        } else {
            // Check if type name is unique (for new types)
            String typeIDStr = request.getParameter("typeID");
            if (typeIDStr == null || typeIDStr.isEmpty()) {
                if (isMaintenanceTypeNameExists(typeName)) {
                    errors.put("typeName", "Maintenance type name already exists");
                }
            } else {
                // For updates, check if the type name belongs to another type
                int typeID = Integer.parseInt(typeIDStr);
                if (isMaintenanceTypeNameExistsForOtherType(typeName, typeID)) {
                    errors.put("typeName", "Maintenance type name already exists for another type");
                }
            }
        }

        // Validate maintenance cost
        String maintenanceCostStr = request.getParameter("maintenanceCost");
        if (maintenanceCostStr == null || maintenanceCostStr.trim().isEmpty()) {
            errors.put("maintenanceCost", "Maintenance cost is required");
        } else {
            try {
                double maintenanceCost = Double.parseDouble(maintenanceCostStr);
                if (maintenanceCost < 0) {
                    errors.put("maintenanceCost", "Maintenance cost cannot be negative");
                }
            } catch (NumberFormatException e) {
                errors.put("maintenanceCost", "Maintenance cost must be a valid number");
            }
        }

        return errors;
    }

    private List<Bus> getAvailableBuses() {
        List<Bus> buses = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT * FROM Bus WHERE status IN ('Available', 'Maintenance') ORDER BY bus_number");
            ResultSet rs = pStmt.executeQuery();

            while (rs.next()) {
                Bus bus = new Bus();
                bus.busID = rs.getInt("bus_id");
                bus.busNumber = rs.getString("bus_number");
                bus.capacity = rs.getInt("capacity");
                bus.status = rs.getString("status");
                bus.currentTerminal = rs.getInt("current_terminal");
                buses.add(bus);
            }

            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return buses;
    }

    private List<Staff> getMechanics() {
        List<Staff> mechanics = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT s.* FROM Staff s " +
                            "JOIN Role r ON s.role_id = r.role_id " +
                            "WHERE r.role_name = 'Mechanic' " +
                            "ORDER BY s.staff_name");
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
                mechanics.add(staff);
            }

            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return mechanics;
    }

    private boolean isMechanic(int staffID) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM Staff s " +
                            "JOIN Role r ON s.role_id = r.role_id " +
                            "WHERE s.staff_id = ? AND r.role_name = 'Mechanic'");
            pStmt.setInt(1, staffID);
            ResultSet rs = pStmt.executeQuery();

            boolean isMechanic = false;
            if (rs.next()) {
                isMechanic = rs.getInt("count") > 0;
            }

            rs.close();
            pStmt.close();
            conn.close();

            return isMechanic;
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

    private boolean isMaintenanceTypeInUse(int typeID) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM Maintenance " +
                            "WHERE maintenance_type_id = ?");
            pStmt.setInt(1, typeID);
            ResultSet rs = pStmt.executeQuery();

            boolean inUse = false;
            if (rs.next()) {
                inUse = rs.getInt("count") > 0;
            }

            rs.close();
            pStmt.close();
            conn.close();

            return inUse;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean isMaintenanceTypeNameExists(String typeName) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM Maintenance_Type " +
                            "WHERE type_name = ?");
            pStmt.setString(1, typeName);
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

    private boolean isMaintenanceTypeNameExistsForOtherType(String typeName, int typeID) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM Maintenance_Type " +
                            "WHERE type_name = ? AND maintenance_type_id != ?");
            pStmt.setString(1, typeName);
            pStmt.setInt(2, typeID);
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
}

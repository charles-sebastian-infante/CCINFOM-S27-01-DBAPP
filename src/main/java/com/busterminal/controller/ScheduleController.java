package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.*;
import com.busterminal.service.ScheduleService;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

@WebServlet("/schedule")
public class ScheduleController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("list".equals(action))
            listSchedules(request, response);
        else if ("new".equals(action))
            showNewScheduleForm(request, response);
        else if ("edit".equals(action))
            showEditScheduleForm(request, response);
        else if ("view".equals(action))
            viewScheduleDetails(request, response);
        else if ("today".equals(action))
            showTodaySchedules(request, response);
        else if ("byTerminal".equals(action))
            showSchedulesByTerminal(request, response);
        else
            listSchedules(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("create".equals(action))
            createSchedule(request, response);
        else if ("update".equals(action))
            updateSchedule(request, response);
        else if ("delete".equals(action))
            deleteSchedule(request, response);
        else if ("cancel".equals(action))
            cancelSchedule(request, response);
    }

    private void listSchedules(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            // First, update all schedules based on current time
            System.out.println("INFO: Updating schedule statuses...");
            updateScheduleStatuses();

            Connection conn = DBConnection.getConnection();

            String statusFilter = request.getParameter("status");
            String dateFilter = request.getParameter("date");

            StringBuilder query = new StringBuilder(
                    "SELECT s.*, b.bus_number, r.route_name, " +
                            "t1.terminal_name as origin_terminal, t2.terminal_name as destination_terminal " +
                            "FROM Schedule s " +
                            "JOIN Bus b ON s.bus_id = b.bus_id " +
                            "JOIN Route r ON s.route_id = r.route_id " +
                            "JOIN Terminal t1 ON r.origin_id = t1.terminal_id " +
                            "JOIN Terminal t2 ON r.destination_id = t2.terminal_id ");

            List<Object> params = new ArrayList<>();
            boolean hasWhere = false;

            if (statusFilter != null && !statusFilter.isEmpty()) {
                query.append("WHERE s.status = ? ");
                params.add(statusFilter);
                hasWhere = true;
            }

            if (dateFilter != null && !dateFilter.isEmpty()) {
                if (hasWhere) {
                    query.append("AND ");
                } else {
                    query.append("WHERE ");
                    hasWhere = true;
                }
                query.append("DATE(s.departure_time) = ? ");
                params.add(dateFilter);
            }

            query.append("ORDER BY s.departure_time DESC");

            PreparedStatement pStmt = conn.prepareStatement(query.toString());

            for (int i = 0; i < params.size(); i++) {
                pStmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pStmt.executeQuery();

            List<Map<String, Object>> schedules = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> schedule = new HashMap<>();
                int scheduleID = rs.getInt("schedule_id");

                schedule.put("schedule_id", scheduleID);
                schedule.put("bus_id", rs.getInt("bus_id"));
                schedule.put("bus_number", rs.getString("bus_number"));
                schedule.put("route_id", rs.getInt("route_id"));
                schedule.put("route_name", rs.getString("route_name"));
                schedule.put("departure_time", rs.getTimestamp("departure_time"));
                schedule.put("arrival_time", rs.getTimestamp("arrival_time"));
                schedule.put("status", rs.getString("status"));
                schedule.put("origin_terminal", rs.getString("origin_terminal"));
                schedule.put("destination_terminal", rs.getString("destination_terminal"));

                PreparedStatement ticketStmt = conn.prepareStatement(
                        "SELECT COUNT(*) as ticket_count FROM Ticket WHERE schedule_id = ?");
                ticketStmt.setInt(1, scheduleID);
                ResultSet ticketRs = ticketStmt.executeQuery();

                if (ticketRs.next()) {
                    schedule.put("ticket_count", ticketRs.getInt("ticket_count"));
                }
                ticketRs.close();
                ticketStmt.close();

                schedules.add(schedule);
            }

            rs.close();
            pStmt.close();
            conn.close();

            request.setAttribute("schedules", schedules);
            request.setAttribute("statusFilter", statusFilter);
            request.setAttribute("dateFilter", dateFilter);

            // Get buses and routes for the create form
            List<Bus> buses = Bus.getAvailableBuses();
            List<Route> routes = Route.getAllRoutes();
            request.setAttribute("buses", buses);
            request.setAttribute("routes", routes);

            request.getRequestDispatcher("/admin/manage_schedules.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving schedules: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_schedules.jsp")
                    .forward(request, response);
        }
    }

    private void showNewScheduleForm(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Bus> availableBuses = getAvailableBuses();
            List<Route> routes = Route.getAllRoutes();

            request.setAttribute("buses", availableBuses);
            request.setAttribute("routes", routes);

            request.getRequestDispatcher("/admin/manage_schedules.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading schedule form: " + e.getMessage());
            response.sendRedirect("schedule?action=list");
        }
    }

    private void showEditScheduleForm(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            int scheduleID = Integer.parseInt(request.getParameter("id"));

            Map<String, Object> scheduleDetails = ScheduleService.getScheduleDetails(scheduleID);

            if (scheduleDetails.isEmpty()) {
                request.setAttribute("error", "Schedule not found");
                response.sendRedirect("schedule?action=list");
                return;
            }

            String status = (String) scheduleDetails.get("status");
            if ("Departed".equals(status) || "Completed".equals(status)) {
                request.setAttribute("error", "Cannot edit a schedule that has departed or is completed");
                response.sendRedirect("schedule?action=view&id=" + scheduleID);
                return;
            }

            List<Bus> availableBuses = getAvailableBuses();
            List<Route> routes = Route.getAllRoutes();

            request.setAttribute("schedule", scheduleDetails);
            request.setAttribute("buses", availableBuses);
            request.setAttribute("routes", routes);

            request.getRequestDispatcher("/admin/manage_schedules.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading schedule form: " + e.getMessage());
            response.sendRedirect("schedule?action=list");
        }
    }

    private void viewScheduleDetails(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            int scheduleID = Integer.parseInt(request.getParameter("id"));

            Map<String, Object> scheduleDetails = ScheduleService.getScheduleDetails(scheduleID);

            if (scheduleDetails.isEmpty()) {
                request.setAttribute("error", "Schedule not found");
                response.sendRedirect("schedule?action=list");
                return;
            }

            request.setAttribute("schedule", scheduleDetails);
            request.getRequestDispatcher("/admin/manage_schedules.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving schedule details: " + e.getMessage());
            response.sendRedirect("schedule?action=list");
        }
    }

    private void showTodaySchedules(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new java.util.Date());
        request.setAttribute("dateFilter", today);
        listSchedules(request, response);
    }

    private void showSchedulesByTerminal(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // Implementation for terminal-specific schedules
        listSchedules(request, response);
    }

    /**
     * ENHANCED: Create schedule with comprehensive validation and conflict checking
     */
    private void createSchedule(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        try {
            // 1. VALIDATE INPUT
            Map<String, String> validationErrors = new HashMap<>();

            String busIDStr = request.getParameter("busID");
            String routeIDStr = request.getParameter("routeID");
            String departureDate = request.getParameter("departureDate");
            String departureTime = request.getParameter("departureTime");
            String arrivalDate = request.getParameter("arrivalDate");
            String arrivalTime = request.getParameter("arrivalTime");

            // Check for null/empty values
            if (busIDStr == null || busIDStr.trim().isEmpty()) {
                validationErrors.put("busID", "Bus is required");
            }
            if (routeIDStr == null || routeIDStr.trim().isEmpty()) {
                validationErrors.put("routeID", "Route is required");
            }
            if (departureDate == null || departureDate.trim().isEmpty() ||
                    departureTime == null || departureTime.trim().isEmpty()) {
                validationErrors.put("departureTime", "Departure date and time are required");
            }
            if (arrivalDate == null || arrivalDate.trim().isEmpty() ||
                    arrivalTime == null || arrivalTime.trim().isEmpty()) {
                validationErrors.put("arrivalTime", "Arrival date and time are required");
            }

            if (!validationErrors.isEmpty()) {
                List<Bus> availableBuses = getAvailableBuses();
                List<Route> routes = Route.getAllRoutes();

                request.setAttribute("validationErrors", validationErrors);
                request.setAttribute("buses", availableBuses);
                request.setAttribute("routes", routes);
                request.setAttribute("busID", busIDStr);
                request.setAttribute("routeID", routeIDStr);
                request.setAttribute("departureDate", departureDate);
                request.setAttribute("departureTime", departureTime);
                request.setAttribute("arrivalDate", arrivalDate);
                request.setAttribute("arrivalTime", arrivalTime);

                request.getRequestDispatcher("/admin/manage_schedules.jsp")
                        .forward(request, response);
                return;
            }

            // Parse values
            int busID = Integer.parseInt(busIDStr);
            int routeID = Integer.parseInt(routeIDStr);

            String departureDatetime = departureDate + " " + departureTime + ":00";
            String arrivalDatetime = arrivalDate + " " + arrivalTime + ":00";

            Timestamp departure = Timestamp.valueOf(departureDatetime);
            Timestamp arrival = Timestamp.valueOf(arrivalDatetime);

            // 2. VALIDATE FOREIGN KEYS
            Bus bus = new Bus();
            bus.busID = busID;
            if (bus.getRecord() != 1) {
                validationErrors.put("busID", "Bus not found");
            }

            Route route = new Route();
            route.routeID = routeID;
            if (route.getRecord() != 1) {
                validationErrors.put("routeID", "Route not found");
            }

            if (!validationErrors.isEmpty()) {
                List<Bus> availableBuses = getAvailableBuses();
                List<Route> routes = Route.getAllRoutes();

                request.setAttribute("validationErrors", validationErrors);
                request.setAttribute("buses", availableBuses);
                request.setAttribute("routes", routes);
                request.setAttribute("busID", busIDStr);
                request.setAttribute("routeID", routeIDStr);
                request.setAttribute("departureDate", departureDate);
                request.setAttribute("departureTime", departureTime);
                request.setAttribute("arrivalDate", arrivalDate);
                request.setAttribute("arrivalTime", arrivalTime);

                request.getRequestDispatcher("/admin/manage_schedules.jsp")
                        .forward(request, response);
                return;
            }

            // 3. VALIDATE DATES
            Timestamp now = new Timestamp(System.currentTimeMillis());

            if (departure.before(now)) {
                validationErrors.put("departureTime", "Cannot schedule in the past");
            }

            if (arrival.before(departure) || arrival.equals(departure)) {
                validationErrors.put("arrivalTime", "Arrival must be after departure");
            }

            // Check reasonable travel time (less than 24 hours)
            long travelTimeMillis = arrival.getTime() - departure.getTime();
            long travelTimeHours = travelTimeMillis / (1000 * 60 * 60);
            if (travelTimeHours > 24) {
                validationErrors.put("arrivalTime", "Travel time cannot exceed 24 hours");
            }

            if (!validationErrors.isEmpty()) {
                List<Bus> availableBuses = getAvailableBuses();
                List<Route> routes = Route.getAllRoutes();

                request.setAttribute("validationErrors", validationErrors);
                request.setAttribute("buses", availableBuses);
                request.setAttribute("routes", routes);
                request.setAttribute("busID", busIDStr);
                request.setAttribute("routeID", routeIDStr);
                request.setAttribute("departureDate", departureDate);
                request.setAttribute("departureTime", departureTime);
                request.setAttribute("arrivalDate", arrivalDate);
                request.setAttribute("arrivalTime", arrivalTime);

                request.getRequestDispatcher("/admin/manage_schedules.jsp")
                        .forward(request, response);
                return;
            }

            // 4. CHECK BUS STATUS
            if (!"Available".equals(bus.status)) {
                validationErrors.put("busID", "Bus is not available (Current status: " + bus.status + ")");

                List<Bus> availableBuses = getAvailableBuses();
                List<Route> routes = Route.getAllRoutes();

                request.setAttribute("validationErrors", validationErrors);
                request.setAttribute("buses", availableBuses);
                request.setAttribute("routes", routes);
                request.setAttribute("busID", busIDStr);
                request.setAttribute("routeID", routeIDStr);
                request.setAttribute("departureDate", departureDate);
                request.setAttribute("departureTime", departureTime);
                request.setAttribute("arrivalDate", arrivalDate);
                request.setAttribute("arrivalTime", arrivalTime);

                request.getRequestDispatcher("/admin/manage_schedules.jsp")
                        .forward(request, response);
                return;
            }

            // 4.5. CHECK DRIVER AND CONDUCTOR (NEW!)
            if (!busHasDriverAndConductor(busID)) {
                validationErrors.put("busID",
                        "Bus must have both a driver and conductor assigned before scheduling");

                List<Bus> availableBuses = getAvailableBuses();
                List<Route> routes = Route.getAllRoutes();

                request.setAttribute("validationErrors", validationErrors);
                request.setAttribute("buses", availableBuses);
                request.setAttribute("routes", routes);
                request.setAttribute("busID", busIDStr);
                request.setAttribute("routeID", routeIDStr);
                request.setAttribute("departureDate", departureDate);
                request.setAttribute("departureTime", departureTime);
                request.setAttribute("arrivalDate", arrivalDate);
                request.setAttribute("arrivalTime", arrivalTime);

                request.getRequestDispatcher("/admin/manage_schedules.jsp")
                        .forward(request, response);
                return;
            }

            // 5. CHECK SCHEDULE CONFLICTS (CRITICAL!)
            if (hasScheduleConflict(busID, departure, arrival)) {
                validationErrors.put("general",
                        "Bus already has a conflicting schedule during this time period. " +
                                "Please choose a different time or bus.");

                List<Bus> availableBuses = getAvailableBuses();
                List<Route> routes = Route.getAllRoutes();

                request.setAttribute("validationErrors", validationErrors);
                request.setAttribute("buses", availableBuses);
                request.setAttribute("routes", routes);
                request.setAttribute("busID", busIDStr);
                request.setAttribute("routeID", routeIDStr);
                request.setAttribute("departureDate", departureDate);
                request.setAttribute("departureTime", departureTime);
                request.setAttribute("arrivalDate", arrivalDate);
                request.setAttribute("arrivalTime", arrivalTime);

                request.getRequestDispatcher("/admin/manage_schedules.jsp")
                        .forward(request, response);
                return;
            }

            // 6. CREATE SCHEDULE IN TRANSACTION
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Create schedule
            Schedule schedule = new Schedule();
            schedule.busID = busID;
            schedule.routeID = routeID;
            schedule.departureTime = departure;
            schedule.arrivalTime = arrival;
            schedule.status = "Scheduled";

            if (schedule.addRecord() != 1) {
                conn.rollback();
                request.setAttribute("error", "Failed to create schedule");
                showNewScheduleForm(request, response);
                return;
            }

            // Update bus status using the same transaction connection
            PreparedStatement busUpdateStmt = conn.prepareStatement(
                    "UPDATE Bus SET status = 'Scheduled' WHERE bus_id = ?");
            busUpdateStmt.setInt(1, busID);
            int busUpdateResult = busUpdateStmt.executeUpdate();
            busUpdateStmt.close();

            if (busUpdateResult != 1) {
                conn.rollback();
                request.setAttribute("error", "Failed to update bus status");
                showNewScheduleForm(request, response);
                return;
            }

            conn.commit();

            request.setAttribute("success", "Schedule created successfully!");
            response.sendRedirect("schedule?action=view&id=" + schedule.scheduleID);

        } catch (IllegalArgumentException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
            e.printStackTrace();
            request.setAttribute("error", "Invalid date/time format: " + e.getMessage());
            showNewScheduleForm(request, response);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
            e.printStackTrace();
            request.setAttribute("error", "Error creating schedule: " + e.getMessage());
            showNewScheduleForm(request, response);
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

    /**
     * Check if bus has conflicting schedules
     * Prevents double-booking buses
     */
    private boolean hasScheduleConflict(int busID, Timestamp departure, Timestamp arrival) {
        try {
            Connection conn = DBConnection.getConnection();

            // Check for overlapping schedules
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as conflicts FROM Schedule " +
                            "WHERE bus_id = ? " +
                            "AND status NOT IN ('Completed', 'Cancelled') " +
                            "AND (" +
                            // New trip starts during existing trip
                            "  (departure_time <= ? AND arrival_time >= ?) OR " +
                            // New trip ends during existing trip
                            "  (departure_time <= ? AND arrival_time >= ?) OR " +
                            // New trip completely contains existing trip
                            "  (departure_time >= ? AND arrival_time <= ?)" +
                            ")");

            pStmt.setInt(1, busID);
            pStmt.setTimestamp(2, departure);
            pStmt.setTimestamp(3, departure);
            pStmt.setTimestamp(4, arrival);
            pStmt.setTimestamp(5, arrival);
            pStmt.setTimestamp(6, departure);
            pStmt.setTimestamp(7, arrival);

            ResultSet rs = pStmt.executeQuery();
            boolean hasConflict = false;

            if (rs.next()) {
                hasConflict = rs.getInt("conflicts") > 0;
            }

            rs.close();
            pStmt.close();
            conn.close();

            return hasConflict;
        } catch (SQLException e) {
            System.out.println("Error checking schedule conflicts: " + e.getMessage());
            e.printStackTrace();
            return true; // Assume conflict on error (safe approach)
        }
    }

    /**
     * NEW: Check if bus has both driver and conductor assigned
     */
    private boolean busHasDriverAndConductor(int busID) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT r.role_name FROM Staff s " +
                            "JOIN Role r ON s.role_id = r.role_id " +
                            "WHERE s.assigned_bus = ?");
            pStmt.setInt(1, busID);
            ResultSet rs = pStmt.executeQuery();

            boolean hasDriver = false;
            boolean hasConductor = false;

            while (rs.next()) {
                String roleName = rs.getString("role_name");
                if ("Driver".equalsIgnoreCase(roleName)) {
                    hasDriver = true;
                } else if ("Conductor".equalsIgnoreCase(roleName)) {
                    hasConductor = true;
                }
            }

            rs.close();
            pStmt.close();
            conn.close();

            return hasDriver && hasConductor;

        } catch (SQLException e) {
            System.out.println("Error checking staff: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void updateSchedule(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // Similar validation as createSchedule
        // Implementation omitted for brevity
        response.sendRedirect("schedule?action=list");
    }

    /**
     * ENHANCED: Delete schedule and update bus status
     */
    private void deleteSchedule(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        try {
            int scheduleID = Integer.parseInt(request.getParameter("id"));

            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Get schedule details before deleting
            Schedule schedule = new Schedule();
            schedule.scheduleID = scheduleID;

            if (schedule.getRecord() != 1) {
                conn.rollback();
                request.setAttribute("error", "Schedule not found");
                response.sendRedirect("schedule?action=list");
                return;
            }

            int busID = schedule.busID;
            String status = schedule.status;

            // Don't allow deleting departed/completed schedules
            if ("Departed".equals(status) || "Completed".equals(status)) {
                conn.rollback();
                request.setAttribute("error", "Cannot delete a schedule that has departed or is completed");
                response.sendRedirect("schedule?action=list");
                return;
            }

            // Check if there are tickets sold
            PreparedStatement ticketCheck = conn.prepareStatement(
                    "SELECT COUNT(*) as ticket_count FROM Ticket WHERE schedule_id = ?");
            ticketCheck.setInt(1, scheduleID);
            ResultSet ticketRs = ticketCheck.executeQuery();

            int ticketCount = 0;
            if (ticketRs.next()) {
                ticketCount = ticketRs.getInt("ticket_count");
            }
            ticketRs.close();
            ticketCheck.close();

            if (ticketCount > 0) {
                conn.rollback();
                request.setAttribute("error",
                        "Cannot delete schedule with sold tickets. Please cancel instead.");
                response.sendRedirect("schedule?action=view&id=" + scheduleID);
                return;
            }

            // Delete the schedule
            if (schedule.delRecord() != 1) {
                conn.rollback();
                request.setAttribute("error", "Failed to delete schedule");
                response.sendRedirect("schedule?action=list");
                return;
            }

            // Check if bus has other active schedules
            PreparedStatement activeCheck = conn.prepareStatement(
                    "SELECT COUNT(*) as active_count FROM Schedule " +
                            "WHERE bus_id = ? AND status IN ('Scheduled', 'Departed')");
            activeCheck.setInt(1, busID);
            ResultSet activeRs = activeCheck.executeQuery();

            int activeCount = 0;
            if (activeRs.next()) {
                activeCount = activeRs.getInt("active_count");
            }
            activeRs.close();
            activeCheck.close();

            // If no more active schedules, set bus to Available
            if (activeCount == 0) {
                Bus bus = new Bus();
                bus.busID = busID;
                bus.getRecord();

                if ("Scheduled".equals(bus.status)) {
                    bus.status = "Available";
                    bus.modRecord();
                }
            }

            conn.commit();

            request.setAttribute("success", "Schedule deleted successfully");
            response.sendRedirect("schedule?action=list");

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
            e.printStackTrace();
            request.setAttribute("error", "Error deleting schedule: " + e.getMessage());
            response.sendRedirect("schedule?action=list");
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

    private void cancelSchedule(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            int scheduleID = Integer.parseInt(request.getParameter("id"));

            Schedule schedule = new Schedule();
            schedule.scheduleID = scheduleID;

            if (schedule.getRecord() == 1) {
                if ("Departed".equals(schedule.status) || "Completed".equals(schedule.status)) {
                    request.setAttribute("error", "Cannot cancel a schedule that has departed or is completed");
                    response.sendRedirect("schedule?action=view&id=" + scheduleID);
                    return;
                }

                schedule.status = "Cancelled";
                if (schedule.modRecord() == 1) {
                    request.setAttribute("success", "Schedule cancelled successfully");
                } else {
                    request.setAttribute("error", "Failed to cancel schedule");
                }
            } else {
                request.setAttribute("error", "Schedule not found");
            }

            response.sendRedirect("schedule?action=view&id=" + scheduleID);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error cancelling schedule: " + e.getMessage());
            response.sendRedirect("schedule?action=list");
        }
    }

    private List<Bus> getAvailableBuses() {
        return Bus.getAvailableBuses();
    }

    /**
     * Automatically update schedule and bus statuses based on current time
     */
    private void updateScheduleStatuses() {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());

            // Get all active schedules (Scheduled or Departed)
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT schedule_id, bus_id, route_id, departure_time, arrival_time, status " +
                            "FROM Schedule " +
                            "WHERE status IN ('Scheduled', 'Departed')");
            ResultSet rs = pStmt.executeQuery();

            List<Schedule> schedulesToUpdate = new ArrayList<>();

            while (rs.next()) {
                int scheduleID = rs.getInt("schedule_id");
                int busID = rs.getInt("bus_id");
                int routeID = rs.getInt("route_id");
                Timestamp departureTime = rs.getTimestamp("departure_time");
                Timestamp arrivalTime = rs.getTimestamp("arrival_time");
                String currentStatus = rs.getString("status");

                String newStatus = null;

                // Check if schedule should be marked as Departed
                if ("Scheduled".equals(currentStatus) && currentTime.after(departureTime)) {
                    newStatus = "Departed";
                }
                // Check if schedule should be marked as Completed
                else if ("Departed".equals(currentStatus) && currentTime.after(arrivalTime)) {
                    newStatus = "Completed";
                }

                // If status needs to change, add to update list
                if (newStatus != null) {
                    Schedule schedule = new Schedule();
                    schedule.scheduleID = scheduleID;
                    schedule.busID = busID;
                    schedule.routeID = routeID;
                    schedule.departureTime = departureTime;
                    schedule.arrivalTime = arrivalTime;
                    schedule.status = newStatus;
                    schedulesToUpdate.add(schedule);

                    System.out
                            .println("Updating schedule " + scheduleID + " from " + currentStatus + " to " + newStatus);
                }
            }

            rs.close();
            pStmt.close();

            // Update schedules and buses
            for (Schedule schedule : schedulesToUpdate) {
                // Update schedule status
                if (schedule.modRecord() == 1) {
                    System.out
                            .println("Successfully updated schedule " + schedule.scheduleID + " to " + schedule.status);

                    // Update bus status
                    Bus bus = new Bus();
                    bus.busID = schedule.busID;
                    if (bus.getRecord() == 1) {
                        String newBusStatus = null;

                        if ("Departed".equals(schedule.status)) {
                            newBusStatus = "In Transit";
                        } else if ("Completed".equals(schedule.status)) {
                            // Check if bus has other active schedules
                            PreparedStatement activeCheck = conn.prepareStatement(
                                    "SELECT COUNT(*) as active_count FROM Schedule " +
                                            "WHERE bus_id = ? AND status IN ('Scheduled', 'Departed') " +
                                            "AND schedule_id != ?");
                            activeCheck.setInt(1, bus.busID);
                            activeCheck.setInt(2, schedule.scheduleID);
                            ResultSet activeRs = activeCheck.executeQuery();

                            int activeCount = 0;
                            if (activeRs.next()) {
                                activeCount = activeRs.getInt("active_count");
                            }
                            activeRs.close();
                            activeCheck.close();

                            // Only set to Available if no other active schedules
                            if (activeCount == 0) {
                                newBusStatus = "Available";
                            }
                        }

                        if (newBusStatus != null) {
                            bus.status = newBusStatus;
                            bus.modRecord();
                            System.out.println(
                                    "Updated bus " + bus.busID + " (" + bus.busNumber + ") to " + newBusStatus);
                        }
                    }
                } else {
                    System.out.println("Failed to update schedule " + schedule.scheduleID);
                }
            }

            conn.close();

        } catch (Exception e) {
            System.out.println("Error updating schedule statuses: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

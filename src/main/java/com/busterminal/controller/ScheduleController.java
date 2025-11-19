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

            // 3. VALIDATE DATES AND TIME CONSTRAINTS
            Timestamp now = new Timestamp(System.currentTimeMillis());

            if (departure.before(now)) {
                validationErrors.put("departureTime", "Cannot schedule in the past");
            }

            if (arrival.before(departure) || arrival.equals(departure)) {
                validationErrors.put("arrivalTime", "Arrival must be after departure");
            }

            // Check departure and arrival time limits (must be before 10 PM)
            @SuppressWarnings("deprecation")
            int departureHour = departure.getHours();
            @SuppressWarnings("deprecation")
            int departureMinute = departure.getMinutes();
            @SuppressWarnings("deprecation")
            int arrivalHour = arrival.getHours();
            @SuppressWarnings("deprecation")
            int arrivalMinute = arrival.getMinutes();

            // Departure time must be before 10:00 PM (22:00)
            if (departureHour >= 22 || (departureHour == 21 && departureMinute > 59)) {
                validationErrors.put("departureTime",
                        "Departure time cannot be at or past 10:00 PM. Last allowed departure is 9:59 PM.");
            }

            // Check if arrival extends past the same day (crosses midnight)
            @SuppressWarnings("deprecation")
            int departureDay = departure.getDate();
            @SuppressWarnings("deprecation")
            int arrivalDay = arrival.getDate();

            if (arrivalDay != departureDay) {
                validationErrors.put("arrivalTime",
                        "Arrival time cannot extend to the next day. All trips must be completed on the same day before 10:00 PM.");
            } else {
                // Arrival time must be before 10:00 PM (22:00) on the same day
                if (arrivalHour >= 22 || (arrivalHour == 21 && arrivalMinute > 59)) {
                    validationErrors.put("arrivalTime",
                            "Arrival time cannot be at or past 10:00 PM. Last allowed arrival is 9:59 PM.");
                }
            }

            // Check reasonable travel time (less than 24 hours)
            long travelTimeMillis = arrival.getTime() - departure.getTime();
            long travelTimeHours = travelTimeMillis / (1000 * 60 * 60);
            if (travelTimeHours > 24) {
                validationErrors.put("arrivalTime", "Travel time cannot exceed 24 hours");
            }

            // Validate scheduled travel time is at least as long as route's minimum travel
            // time
            if (route.travelTime != null) {
                long routeTravelMillis = route.travelTime.getTime();
                long scheduledTravelMillis = travelTimeMillis;

                if (scheduledTravelMillis < routeTravelMillis) {
                    long routeTravelHours = routeTravelMillis / (1000 * 60 * 60);
                    long routeTravelMinutes = (routeTravelMillis % (1000 * 60 * 60)) / (1000 * 60);
                    validationErrors.put("arrivalTime",
                            String.format(
                                    "Scheduled travel time is too short. Route requires minimum %d hours and %d minutes.",
                                    routeTravelHours, routeTravelMinutes));
                }
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

            // 4.2. CHECK BUS CURRENT TERMINAL MATCHES ROUTE ORIGIN
            if (bus.currentTerminal != route.originID) {
                // Get terminal names for better error message
                Terminal currentTerm = new Terminal();
                currentTerm.terminalID = bus.currentTerminal;
                currentTerm.getRecord();

                Terminal originTerm = new Terminal();
                originTerm.terminalID = route.originID;
                originTerm.getRecord();

                validationErrors.put("busID",
                        String.format("Bus is currently at %s terminal, but route starts from %s terminal. " +
                                "Bus must be at the origin terminal to be scheduled.",
                                currentTerm.terminalName, originTerm.terminalName));

                List<Bus> availableBuses = getAvailableBuses();
                List<Route> routes2 = Route.getAllRoutes();

                request.setAttribute("validationErrors", validationErrors);
                request.setAttribute("buses", availableBuses);
                request.setAttribute("routes", routes2);
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

            // 4.5. CHECK DRIVER AND CONDUCTOR WITH SHIFT VALIDATION
            Map<String, String> staffShiftValidation = validateStaffShifts(busID, departure, arrival);
            if (!staffShiftValidation.isEmpty()) {
                validationErrors.putAll(staffShiftValidation);

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
     * ENHANCED: Validate staff shifts and assignments for schedule
     * Checks:
     * 1. Bus has both driver and conductor assigned
     * 2. Departure time falls within assigned staff shifts
     * 3. Morning shift: 6:00 AM to 3:00 PM
     * 4. Evening shift: 3:00 PM to 10:00 PM
     */
    @SuppressWarnings("deprecation")
    private Map<String, String> validateStaffShifts(int busID, Timestamp departure, Timestamp arrival) {
        Map<String, String> errors = new HashMap<>();

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT s.staff_name, r.role_name, s.shift FROM Staff s " +
                            "JOIN Role r ON s.role_id = r.role_id " +
                            "WHERE s.assigned_bus = ? AND r.role_name IN ('Driver', 'Conductor')");
            pStmt.setInt(1, busID);
            ResultSet rs = pStmt.executeQuery();

            boolean hasDriver = false;
            boolean hasConductor = false;
            boolean hasMorningDriver = false;
            boolean hasMorningConductor = false;
            boolean hasEveningDriver = false;
            boolean hasEveningConductor = false;
            String driverName = null;
            String conductorName = null;
            String driverShift = null;
            String conductorShift = null;

            while (rs.next()) {
                String roleName = rs.getString("role_name");
                String shift = rs.getString("shift");
                String staffName = rs.getString("staff_name");

                if ("Driver".equalsIgnoreCase(roleName)) {
                    hasDriver = true;
                    driverName = staffName;
                    driverShift = shift;
                    if ("Morning".equalsIgnoreCase(shift)) {
                        hasMorningDriver = true;
                    } else if ("Evening".equalsIgnoreCase(shift)) {
                        hasEveningDriver = true;
                    }
                } else if ("Conductor".equalsIgnoreCase(roleName)) {
                    hasConductor = true;
                    conductorName = staffName;
                    conductorShift = shift;
                    if ("Morning".equalsIgnoreCase(shift)) {
                        hasMorningConductor = true;
                    } else if ("Evening".equalsIgnoreCase(shift)) {
                        hasEveningConductor = true;
                    }
                }
            }

            rs.close();
            pStmt.close();
            conn.close();

            // Check if both driver and conductor are assigned
            if (!hasDriver || !hasConductor) {
                if (!hasDriver && !hasConductor) {
                    errors.put("busID", "Bus must have both a driver and conductor assigned before scheduling");
                } else if (!hasDriver) {
                    errors.put("busID", "Bus must have a driver assigned before scheduling");
                } else {
                    errors.put("busID", "Bus must have a conductor assigned before scheduling");
                }
                return errors;
            }

            // Validate shift coverage for the schedule
            // Morning shift: 6:00 AM (06:00) to 3:00 PM (15:00)
            // Evening shift: 3:00 PM (15:00) to 10:00 PM (22:00)

            int departureHour = departure.getHours();
            int departureMinute = departure.getMinutes();
            int arrivalHour = arrival.getHours();
            int arrivalMinute = arrival.getMinutes();

            // Convert to minutes for easier comparison
            int departureTimeInMinutes = departureHour * 60 + departureMinute;
            int arrivalTimeInMinutes = arrivalHour * 60 + arrivalMinute;

            // Define shift boundaries (in minutes from midnight)
            int morningStart = 6 * 60; // 6:00 AM
            int morningEnd = 15 * 60; // 3:00 PM
            int eveningStart = 15 * 60; // 3:00 PM
            int eveningEnd = 22 * 60; // 10:00 PM

            // Check if departure is in morning shift (6 AM - 3 PM)
            boolean departureInMorning = departureTimeInMinutes >= morningStart && departureTimeInMinutes < morningEnd;
            // Check if departure is in evening shift (3 PM - 10 PM)
            boolean departureInEvening = departureTimeInMinutes >= eveningStart && departureTimeInMinutes < eveningEnd;

            // Check if arrival is in evening shift (for cross-shift validation)
            boolean arrivalInEvening = arrivalTimeInMinutes >= eveningStart && arrivalTimeInMinutes < eveningEnd;

            // Validate departure time is within operating hours (6 AM - 10 PM)
            if (departureTimeInMinutes < morningStart || departureTimeInMinutes >= eveningEnd) {
                errors.put("departureTime",
                        "Departure time must be between 6:00 AM and 10:00 PM");
                return errors;
            }

            // Validate based on departure time and staff availability
            if (departureInMorning) {
                // Departure is in morning shift - need BOTH driver and conductor with morning
                // shift
                if (!hasMorningDriver || !hasMorningConductor) {
                    if (!hasMorningDriver && !hasMorningConductor) {
                        errors.put("busID",
                                String.format("Cannot schedule departure at morning time (6 AM - 3 PM). " +
                                        "Bus staff: Driver (%s - %s shift), Conductor (%s - %s shift). " +
                                        "Need both a driver and conductor with morning shift.",
                                        driverName, driverShift, conductorName, conductorShift));
                    } else if (!hasMorningDriver) {
                        errors.put("busID",
                                String.format("Cannot schedule departure at morning time (6 AM - 3 PM). " +
                                        "Bus has morning conductor but driver is on %s shift. " +
                                        "Need a driver with morning shift.",
                                        driverShift));
                    } else {
                        errors.put("busID",
                                String.format("Cannot schedule departure at morning time (6 AM - 3 PM). " +
                                        "Bus has morning driver but conductor is on %s shift. " +
                                        "Need a conductor with morning shift.",
                                        conductorShift));
                    }
                }

                // If arrival extends into evening, need BOTH driver and conductor with evening
                // shift
                if (arrivalInEvening && (!hasEveningDriver || !hasEveningConductor)) {
                    if (!hasEveningDriver && !hasEveningConductor) {
                        errors.put("arrivalTime",
                                String.format(
                                        "Schedule extends into evening shift (arrival after 3 PM) but bus has no evening shift staff. "
                                                +
                                                "Bus staff: Driver (%s - %s shift), Conductor (%s - %s shift). " +
                                                "Need both driver and conductor with evening shift.",
                                        driverName, driverShift, conductorName, conductorShift));
                    } else if (!hasEveningDriver) {
                        errors.put("arrivalTime",
                                String.format(
                                        "Schedule extends into evening shift (arrival after 3 PM) but driver is on %s shift. "
                                                +
                                                "Need a driver with evening shift.",
                                        driverShift));
                    } else {
                        errors.put("arrivalTime",
                                String.format(
                                        "Schedule extends into evening shift (arrival after 3 PM) but conductor is on %s shift. "
                                                +
                                                "Need a conductor with evening shift.",
                                        conductorShift));
                    }
                }
            } else if (departureInEvening) {
                // Departure is in evening shift - need BOTH driver and conductor with evening
                // shift
                if (!hasEveningDriver || !hasEveningConductor) {
                    if (!hasEveningDriver && !hasEveningConductor) {
                        errors.put("busID",
                                String.format("Cannot schedule departure at evening time (3 PM - 10 PM). " +
                                        "Bus staff: Driver (%s - %s shift), Conductor (%s - %s shift). " +
                                        "Need both a driver and conductor with evening shift.",
                                        driverName, driverShift, conductorName, conductorShift));
                    } else if (!hasEveningDriver) {
                        errors.put("busID",
                                String.format("Cannot schedule departure at evening time (3 PM - 10 PM). " +
                                        "Bus has evening conductor but driver is on %s shift. " +
                                        "Need a driver with evening shift.",
                                        driverShift));
                    } else {
                        errors.put("busID",
                                String.format("Cannot schedule departure at evening time (3 PM - 10 PM). " +
                                        "Bus has evening driver but conductor is on %s shift. " +
                                        "Need a conductor with evening shift.",
                                        conductorShift));
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error validating staff shifts: " + e.getMessage());
            e.printStackTrace();
            errors.put("general", "Error validating staff assignments: " + e.getMessage());
        }

        return errors;
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

                // Check if schedule should be marked as Completed FIRST (both times passed)
                // This handles the case where historical data is loaded with both times in the
                // past
                if (currentTime.after(arrivalTime)) {
                    newStatus = "Completed";
                }
                // Check if schedule should be marked as Departed (only departure time passed)
                else if ("Scheduled".equals(currentStatus) && currentTime.after(departureTime)) {
                    newStatus = "Departed";
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
                } else {
                    System.out.println("Failed to update schedule " + schedule.scheduleID);
                }
            }

            // Sync all bus statuses based on their current schedules
            // This ensures buses are correctly marked as Scheduled/In Transit/Available
            PreparedStatement busStatusStmt = conn.prepareStatement(
                    "SELECT DISTINCT bus_id FROM Schedule WHERE status IN ('Scheduled', 'Departed')");
            ResultSet busRs = busStatusStmt.executeQuery();

            List<Integer> activeBusIDs = new ArrayList<>();
            while (busRs.next()) {
                activeBusIDs.add(busRs.getInt("bus_id"));
            }
            busRs.close();
            busStatusStmt.close();

            // Update buses with active schedules
            for (int busID : activeBusIDs) {
                PreparedStatement scheduleCheckStmt = conn.prepareStatement(
                        "SELECT status FROM Schedule " +
                                "WHERE bus_id = ? AND status IN ('Scheduled', 'Departed') " +
                                "ORDER BY departure_time ASC LIMIT 1");
                scheduleCheckStmt.setInt(1, busID);
                ResultSet schedRs = scheduleCheckStmt.executeQuery();

                String newBusStatus = null;
                if (schedRs.next()) {
                    String scheduleStatus = schedRs.getString("status");
                    if ("Departed".equals(scheduleStatus)) {
                        newBusStatus = "In Transit";
                    } else if ("Scheduled".equals(scheduleStatus)) {
                        newBusStatus = "Scheduled";
                    }
                }
                schedRs.close();
                scheduleCheckStmt.close();

                if (newBusStatus != null) {
                    Bus bus = new Bus();
                    bus.busID = busID;
                    if (bus.getRecord() == 1) {
                        // Only update if status actually changed
                        if (!newBusStatus.equals(bus.status)) {
                            bus.status = newBusStatus;
                            bus.modRecord();
                            System.out.println(
                                    "Synced bus " + bus.busID + " (" + bus.busNumber + ") to " + newBusStatus);
                        }
                    }
                }
            }

            // Set buses with no active schedules to Available (if not in maintenance or out
            // of
            // order)
            PreparedStatement allBusesStmt = conn
                    .prepareStatement("SELECT bus_id FROM Bus WHERE status NOT IN ('Maintenance', 'Out of Order')");
            ResultSet allBusesRs = allBusesStmt.executeQuery();

            while (allBusesRs.next()) {
                int busID = allBusesRs.getInt("bus_id");

                // If bus is not in the active list, it should be Available
                if (!activeBusIDs.contains(busID)) {
                    Bus bus = new Bus();
                    bus.busID = busID;
                    if (bus.getRecord() == 1) {
                        if (!"Available".equals(bus.status) &&
                                !"Maintenance".equals(bus.status) &&
                                !"Out of Order".equals(bus.status)) {
                            bus.status = "Available";
                            bus.modRecord();
                            System.out.println(
                                    "Synced bus " + bus.busID + " (" + bus.busNumber + ") to Available");
                        }
                    }
                }
            }
            allBusesRs.close();
            allBusesStmt.close();

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

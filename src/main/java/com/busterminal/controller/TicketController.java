package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/ticket")
public class TicketController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("list".equals(action)) 
            listTickets(request, response);
        else if ("new".equals(action))
            showNewTicketForm(request, response);
        else if ("view".equals(action))
            viewTicketDetails(request, response);
        else if ("bySchedule".equals(action))
            listTicketsBySchedule(request, response);
        else if ("edit".equals(action))
            showEditForm(request, response);
        else 
            listTickets(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("create".equals(action)) 
            createTicket(request, response);
        else if ("delete".equals(action))
            deleteTicket(request, response);
        else if ("update".equals(action))
            updateTicket(request, response);
    }
    
    private void listTickets(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT t.*, s.departure_time, s.status as schedule_status, " +
                "b.bus_number, r.route_name " +
                "FROM Ticket t " +
                "JOIN Schedule s ON t.schedule_id = s.schedule_id " +
                "JOIN Bus b ON s.bus_id = b.bus_id " +
                "JOIN Route r ON s.route_id = r.route_id " +
                "ORDER BY s.departure_time DESC");
            
            ResultSet rs = pStmt.executeQuery();
            
            List<Map<String, Object>> tickets = new ArrayList<>();
            while(rs.next()) {
                Map<String, Object> ticket = new HashMap<>();
                ticket.put("ticket_id", rs.getInt("ticket_id"));
                ticket.put("ticket_number", rs.getString("ticket_number"));
                ticket.put("schedule_id", rs.getInt("schedule_id"));
                ticket.put("discounted", rs.getBoolean("discounted"));
                ticket.put("departure_time", rs.getTimestamp("departure_time"));
                ticket.put("schedule_status", rs.getString("schedule_status"));
                ticket.put("bus_number", rs.getString("bus_number"));
                ticket.put("route_name", rs.getString("route_name"));
                
                // Get the fare amount
                double fare = calculateFare(rs.getInt("schedule_id"), rs.getBoolean("discounted"));
                ticket.put("fare", fare);
                
                tickets.add(ticket);
            }
            
            rs.close();
            pStmt.close();
            conn.close();
            
            request.setAttribute("tickets", tickets);

            List<Map<String, Object>> schedules = getAvailableSchedules();
            request.setAttribute("scheduleList", schedules);

            request.getRequestDispatcher("/admin/tickets.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving tickets: " + e.getMessage());
            request.getRequestDispatcher("/admin/tickets.jsp")
                .forward(request, response);
        }
    }
    
    private void listTicketsBySchedule(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            
            Schedule schedule = new Schedule();
            schedule.scheduleID = scheduleID;
            schedule.getRecord();
            
            Route route = new Route();
            route.routeID = schedule.routeID;
            route.getRecord();
            
            Bus bus = new Bus();
            bus.busID = schedule.busID;
            bus.getRecord();
            
            List<Ticket> tickets = Ticket.getTicketsByScheduleID(scheduleID);
            
            int totalPassengers = tickets.size();
            int discountedPassengers = 0;
            double totalRevenue = 0.0;
            
            for (Ticket ticket : tickets) {
                if (ticket.discounted) {
                    discountedPassengers++;
                }
                double fare = ticket.calculateFare(schedule.routeID);
                totalRevenue += fare;
            }
            
            request.setAttribute("schedule", schedule);
            request.setAttribute("route", route);
            request.setAttribute("bus", bus);
            request.setAttribute("tickets", tickets);
            request.setAttribute("totalPassengers", totalPassengers);
            request.setAttribute("discountedPassengers", discountedPassengers);
            request.setAttribute("regularPassengers", totalPassengers - discountedPassengers);
            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("baseFare", route.baseFare);
            
            request.getRequestDispatcher("/admin/tickets.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving tickets for schedule: " + e.getMessage());
            response.sendRedirect("schedule?action=list");
        }
    }
    
    private void showNewTicketForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Map<String, Object>> availableSchedules = getAvailableSchedules();
            
            request.setAttribute("schedules", availableSchedules);
            request.setAttribute("ticketNumber", generateTicketNumber());
            request.getRequestDispatcher("/admin/tickets.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading ticket form: " + e.getMessage());
            response.sendRedirect("ticket?action=list");
        }
    }

    private void showEditForm(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Ticket ticket = new Ticket();
            ticket.ticketID = Integer.parseInt(request.getParameter("id"));

            if (ticket.getRecord() == 1){
                request.setAttribute("editTicket", ticket);
                request.setAttribute("scheduleList", getAvailableSchedules());
                request.getRequestDispatcher("/admin/tickets.jsp")
                        .forward(request, response);
            }
            else {
                response.sendRedirect("ticket?action=list");
            }
        }
        catch (Exception e){
            e.printStackTrace();
            response.sendRedirect("ticket?action=list");
        }

    }

    private void viewTicketDetails(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            int ticketID = Integer.parseInt(request.getParameter("id"));
            
            Ticket ticket = new Ticket();
            ticket.ticketID = ticketID;
            
            if(ticket.getRecord() == 1) {
                Schedule schedule = new Schedule();
                schedule.scheduleID = ticket.scheduleID;
                schedule.getRecord();
                
                Route route = new Route();
                route.routeID = schedule.routeID;
                route.getRecord();
                
                Bus bus = new Bus();
                bus.busID = schedule.busID;
                bus.getRecord();
                
                Terminal origin = new Terminal();
                origin.terminalID = route.originID;
                origin.getRecord();
                
                Terminal destination = new Terminal();
                destination.terminalID = route.destinationID;
                destination.getRecord();
                
                double fare = calculateFare(ticket.scheduleID, ticket.discounted);
                
                request.setAttribute("ticket", ticket);
                request.setAttribute("schedule", schedule);
                request.setAttribute("route", route);
                request.setAttribute("bus", bus);
                request.setAttribute("origin", origin);
                request.setAttribute("destination", destination);
                request.setAttribute("fare", fare);
                
                request.getRequestDispatcher("/admin/tickets.jsp")
                    .forward(request, response);
            } else {
                request.setAttribute("error", "Ticket not found");
                response.sendRedirect("ticket?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving ticket details: " + e.getMessage());
            response.sendRedirect("ticket?action=list");
        }
    }
    
    /**
     * Create ticket with transaction locking to prevent overbooking
     * This fixes the critical race condition issue
     */
    private void createTicket(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        
        try {
            // Validate input
            Map<String, String> validationErrors = validateTicketInput(request);
            
            if (!validationErrors.isEmpty()) {
                List<Map<String, Object>> availableSchedules = getAvailableSchedules();
                request.setAttribute("validationErrors", validationErrors);
                request.setAttribute("schedules", availableSchedules);
                request.setAttribute("scheduleID", request.getParameter("scheduleID"));
                request.setAttribute("discounted", request.getParameter("discounted"));
                
                request.getRequestDispatcher("/admin/tickets.jsp")
                    .forward(request, response);
                return;
            }
            
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            boolean discounted = "on".equals(request.getParameter("discounted"));
            
            // START TRANSACTION WITH LOCKING
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Lock the schedule row to prevent concurrent bookings
            PreparedStatement lockStmt = conn.prepareStatement(
                "SELECT s.schedule_id, s.bus_id, s.status, b.capacity " +
                "FROM Schedule s " +
                "JOIN Bus b ON s.bus_id = b.bus_id " +
                "WHERE s.schedule_id = ? FOR UPDATE");
            lockStmt.setInt(1, scheduleID);
            ResultSet lockRs = lockStmt.executeQuery();
            
            if (!lockRs.next()) {
                conn.rollback();
                request.setAttribute("error", "Schedule not found");
                showNewTicketForm(request, response);
                return;
            }
            
            String scheduleStatus = lockRs.getString("status");
            int capacity = lockRs.getInt("capacity");
            lockRs.close();
            lockStmt.close();
            
            // 2. Verify schedule is still bookable
            if (!"Scheduled".equals(scheduleStatus)) {
                conn.rollback();
                request.setAttribute("error", "Schedule is no longer available (Status: " + scheduleStatus + ")");
                showNewTicketForm(request, response);
                return;
            }
            
            // 3. Re-check availability within transaction (CRITICAL!)
            PreparedStatement countStmt = conn.prepareStatement(
                "SELECT COUNT(*) as sold FROM Ticket WHERE schedule_id = ?");
            countStmt.setInt(1, scheduleID);
            ResultSet countRs = countStmt.executeQuery();
            
            int soldTickets = 0;
            if (countRs.next()) {
                soldTickets = countRs.getInt("sold");
            }
            countRs.close();
            countStmt.close();
            
            int availableSeats = capacity - soldTickets;
            
            if (availableSeats <= 0) {
                conn.rollback();
                request.setAttribute("error", "Sorry! This schedule just sold out. Please choose another departure.");
                showNewTicketForm(request, response);
                return;
            }
            
            // 4. Create the ticket
            Ticket ticket = new Ticket();
            ticket.scheduleID = scheduleID;
            ticket.discounted = discounted;
            ticket.ticketNumber = generateTicketNumber();
            
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO Ticket (ticket_number, schedule_id, discounted) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, ticket.ticketNumber);
            insertStmt.setInt(2, ticket.scheduleID);
            insertStmt.setBoolean(3, ticket.discounted);
            
            int affectedRows = insertStmt.executeUpdate();
            
            if (affectedRows == 0) {
                conn.rollback();
                request.setAttribute("error", "Failed to create ticket");
                showNewTicketForm(request, response);
                return;
            }
            
            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                ticket.ticketID = generatedKeys.getInt(1);
            }
            generatedKeys.close();
            insertStmt.close();
            
            // 5. Commit the transaction
            conn.commit();
            
            // Success!
            request.setAttribute("success", "Ticket purchased successfully! Ticket Number: " + ticket.ticketNumber);
            response.sendRedirect("ticket?action=view&id=" + ticket.ticketID);
            
        } catch (NumberFormatException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            }
            e.printStackTrace();
            request.setAttribute("error", "Invalid input format");
            showNewTicketForm(request, response);
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            }
            e.printStackTrace();
            request.setAttribute("error", "Database error: " + e.getMessage());
            showNewTicketForm(request, response);
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            }
            e.printStackTrace();
            request.setAttribute("error", "Error creating ticket: " + e.getMessage());
            showNewTicketForm(request, response);
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

    private void updateTicket(HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException {
        try {
            Ticket ticket = new Ticket();
            ticket.ticketID = Integer.parseInt(request.getParameter("id"));
            ticket.ticketNumber = request.getParameter("ticketNumber");
            ticket.scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            ticket.discounted = "true".equals(request.getParameter("discounted"));

            if (ticket.modRecord() == 1){
                response.sendRedirect(request.getContextPath() + "/ticket?action=list");
            } else {
                request.setAttribute("error", "Failed to update ticket.");
                Map<String, Object> t = new HashMap<>();
                t.put("ticket_id", ticket.ticketID);
                t.put("ticket_number", ticket.ticketNumber);
                t.put("schedule_id", ticket.scheduleID);
                t.put("discounted", ticket.discounted);
                request.setAttribute("editTicket", t);
                request.getRequestDispatcher("/admin/tickets.jsp")
                        .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error updating ticket: " + e.getMessage());
            response.sendRedirect("ticket?action=list");
        }
    }

    private void deleteTicket(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            int ticketID = Integer.parseInt(request.getParameter("id"));
            
            Ticket ticket = new Ticket();
            ticket.ticketID = ticketID;
            
            if (ticket.getRecord() == 1) {
                int scheduleID = ticket.scheduleID;
                
                Schedule schedule = new Schedule();
                schedule.scheduleID = scheduleID;
                schedule.getRecord();
                
                if ("Departed".equals(schedule.status) || "Completed".equals(schedule.status)) {
                    request.setAttribute("error", "Cannot delete ticket for a departed or completed trip");
                    response.sendRedirect("ticket?action=list");
                    return;
                }
                
                if(ticket.delRecord() == 1) {
                    int remainingTickets = getTicketCountForSchedule(scheduleID);
                    
                    if (remainingTickets == 0) {
                        Bus bus = new Bus();
                        bus.busID = schedule.busID;
                        bus.getRecord();
                        
                        if ("Scheduled".equals(bus.status)) {
                            bus.status = "Available";
                            bus.modRecord();
                        }
                    }
                    
                    request.setAttribute("success", "Ticket deleted successfully");
                    request.getRequestDispatcher("ticket?action=list").forward(request, response);
                } else {
                    request.setAttribute("error", "Failed to delete ticket");
                }
            } else {
                request.setAttribute("error", "Ticket not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error deleting ticket: " + e.getMessage());
            response.sendRedirect("ticket?action=list");
        }
    }

    /*private void deleteMaintenance(HttpServletRequest request,
                                   HttpServletResponse response) throws ServletException, IOException {
        try {
            Maintenance m = new Maintenance();
            m.maintenanceID =  Integer.parseInt(request.getParameter("id"));

            if(m.delRecord() == 1) {
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
    }*/
    private Map<String, String> validateTicketInput(HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        
        String scheduleIDStr = request.getParameter("scheduleID");
        if (scheduleIDStr == null || scheduleIDStr.trim().isEmpty()) {
            errors.put("scheduleID", "Schedule is required");
            return errors;
        }
        
        try {
            int scheduleID = Integer.parseInt(scheduleIDStr);
            if (scheduleID <= 0) {
                errors.put("scheduleID", "Invalid schedule");
                return errors;
            }
            
            // Validate schedule exists and is bookable
            Schedule schedule = new Schedule();
            schedule.scheduleID = scheduleID;
            if (schedule.getRecord() != 1) {
                errors.put("scheduleID", "Schedule not found");
                return errors;
            }
            
            // Check schedule status
            if ("Departed".equals(schedule.status) || 
                "Cancelled".equals(schedule.status) || 
                "Completed".equals(schedule.status)) {
                errors.put("scheduleID", "Cannot create ticket for a departed, completed, or cancelled schedule");
                return errors;
            }
            
            // Check if departure is in the past
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (schedule.departureTime.before(now)) {
                errors.put("scheduleID", "Cannot book tickets for past departures");
                return errors;
            }
            
        } catch (NumberFormatException e) {
            errors.put("scheduleID", "Invalid schedule ID format");
        }
        
        return errors;
    }
    
    private List<Map<String, Object>> getAvailableSchedules() {
        List<Map<String, Object>> schedules = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT s.*, r.route_name, b.bus_number, b.capacity, " +
                "t1.terminal_name as origin, t2.terminal_name as destination " +
                "FROM Schedule s " +
                "JOIN Route r ON s.route_id = r.route_id " +
                "JOIN Bus b ON s.bus_id = b.bus_id " +
                "JOIN Terminal t1 ON r.origin_id = t1.terminal_id " +
                "JOIN Terminal t2 ON r.destination_id = t2.terminal_id " +
                "WHERE s.status = 'Scheduled' " +
                "AND s.departure_time > NOW() " +
                "ORDER BY s.departure_time");
            
            ResultSet rs = pStmt.executeQuery();
            
            while(rs.next()) {
                Map<String, Object> schedule = new HashMap<>();
                int scheduleID = rs.getInt("schedule_id");
                
                schedule.put("schedule_id", scheduleID);
                schedule.put("route_name", rs.getString("route_name"));
                schedule.put("bus_number", rs.getString("bus_number"));
                schedule.put("departure_time", rs.getTimestamp("departure_time"));
                schedule.put("arrival_time", rs.getTimestamp("arrival_time"));
                schedule.put("origin", rs.getString("origin"));
                schedule.put("destination", rs.getString("destination"));
                
                int capacity = rs.getInt("capacity");
                int ticketCount = getTicketCountForSchedule(scheduleID);
                
                schedule.put("available_seats", capacity - ticketCount);
                schedule.put("total_capacity", capacity);
                
                if (capacity > ticketCount) {
                    schedules.add(schedule);
                }
            }
            
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return schedules;
    }
    
    private int getTicketCountForSchedule(int scheduleID) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT COUNT(*) as count FROM Ticket WHERE schedule_id = ?");
            pStmt.setInt(1, scheduleID);
            
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
    
    private double calculateFare(int scheduleID, boolean isDiscounted) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT r.base_fare FROM Schedule s " +
                "JOIN Route r ON s.route_id = r.route_id " +
                "WHERE s.schedule_id = ?");
            pStmt.setInt(1, scheduleID);
            
            ResultSet rs = pStmt.executeQuery();
            double fare = 0.0;
            
            if (rs.next()) {
                fare = rs.getDouble("base_fare");
                
                if (isDiscounted) {
                    fare = fare * 0.8; // 20% discount
                }
            }
            
            rs.close();
            pStmt.close();
            conn.close();
            
            return fare;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0.0;
        }
    }
    
    private String generateTicketNumber() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
        String num =  "TKT-" + sdf.format(new java.util.Date()) + "-" + (int)(Math.random() * 1000);
        System.out.println(num);
        return num;
    }
}

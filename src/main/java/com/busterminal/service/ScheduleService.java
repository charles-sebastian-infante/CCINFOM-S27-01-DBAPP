package com.busterminal.service;

import java.sql.*;
import java.util.*;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;

public class ScheduleService {
    
    /**
     * Create a new schedule with validation
     * return Map containing success flag and message or created schedule ID
     */
    public static Map<String, Object> createSchedule(Schedule schedule) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validate bus availability
            if (!isBusAvailableForSchedule(schedule.busID, schedule.departureTime, schedule.arrivalTime)) {
                result.put("success", false);
                result.put("message", "Bus is not available for the selected time period");
                return result;
            }
            
            // Validate route
            Route route = new Route();
            route.routeID = schedule.routeID;
            if (route.getRecord() != 1) {
                result.put("success", false);
                result.put("message", "Invalid route selected");
                return result;
            }
            
            // Validate terminals
            if (route.originID <= 0 || route.destinationID <= 0) {
                result.put("success", false);
                result.put("message", "Route must have valid origin and destination terminals");
                return result;
            }
            
            // Validate time (arrival must be after departure)
            if (schedule.arrivalTime != null && 
                schedule.departureTime != null && 
                schedule.arrivalTime.before(schedule.departureTime)) {
                result.put("success", false);
                result.put("message", "Arrival time must be after departure time");
                return result;
            }
            
            // Set bus status to Scheduled
            Bus bus = new Bus();
            bus.busID = schedule.busID;
            if (bus.getRecord() == 1) {
                if (!"Maintenance".equals(bus.status)) {
                    bus.status = "Scheduled";
                    bus.modRecord();
                } else {
                    result.put("success", false);
                    result.put("message", "Cannot schedule a bus that is under maintenance");
                    return result;
                }
            }
            
            // Add schedule record
            if (schedule.addRecord() == 1) {
                result.put("success", true);
                result.put("schedule_id", schedule.scheduleID);
            } else {
                result.put("success", false);
                result.put("message", "Failed to create schedule");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Update an existing schedule with validation
     * Map containing success flag and message
     */
    public static Map<String, Object> updateSchedule(Schedule schedule) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get existing schedule
            Schedule existingSchedule = new Schedule();
            existingSchedule.scheduleID = schedule.scheduleID;
            if (existingSchedule.getRecord() != 1) {
                result.put("success", false);
                result.put("message", "Schedule not found");
                return result;
            }
            
            // Check if schedule has departed or is completed
            if ("Departed".equals(existingSchedule.status) || 
                "Completed".equals(existingSchedule.status)) {
                result.put("success", false);
                result.put("message", "Cannot update a schedule that has departed or is completed");
                return result;
            }
            
            // If bus is changing, validate new bus availability
            if (schedule.busID != existingSchedule.busID) {
                if (!isBusAvailableForSchedule(schedule.busID, schedule.departureTime, schedule.arrivalTime)) {
                    result.put("success", false);
                    result.put("message", "New bus is not available for the selected time period");
                    return result;
                }
                
                // Update status of old bus if this was its only schedule
                if (!hasBusOtherSchedules(existingSchedule.busID, existingSchedule.scheduleID)) {
                    Bus oldBus = new Bus();
                    oldBus.busID = existingSchedule.busID;
                    if (oldBus.getRecord() == 1) {
                        if ("Scheduled".equals(oldBus.status)) {
                            oldBus.status = "Available";
                            oldBus.modRecord();
                        }
                    }
                }
                
                // Update status of new bus
                Bus newBus = new Bus();
                newBus.busID = schedule.busID;
                if (newBus.getRecord() == 1) {
                    if (!"Maintenance".equals(newBus.status)) {
                        newBus.status = "Scheduled";
                        newBus.modRecord();
                    } else {
                        result.put("success", false);
                        result.put("message", "Cannot schedule a bus that is under maintenance");
                        return result;
                    }
                }
            } else if (!schedule.departureTime.equals(existingSchedule.departureTime) ||
                       !schedule.arrivalTime.equals(existingSchedule.arrivalTime)) {
                // If times are changing for the same bus, validate availability
                if (!isBusAvailableForScheduleExcludingCurrent(schedule.busID, 
                                                              schedule.departureTime, 
                                                              schedule.arrivalTime,
                                                              schedule.scheduleID)) {
                    result.put("success", false);
                    result.put("message", "Bus is not available for the new time period");
                    return result;
                }
            }
            
            // Validate route
            Route route = new Route();
            route.routeID = schedule.routeID;
            if (route.getRecord() != 1) {
                result.put("success", false);
                result.put("message", "Invalid route selected");
                return result;
            }
            
            // Validate time (arrival must be after departure)
            if (schedule.arrivalTime != null && 
                schedule.departureTime != null && 
                schedule.arrivalTime.before(schedule.departureTime)) {
                result.put("success", false);
                result.put("message", "Arrival time must be after departure time");
                return result;
            }
            
            // Update schedule
            if (schedule.modRecord() == 1) {
                result.put("success", true);
                result.put("message", "Schedule updated successfully");
            } else {
                result.put("success", false);
                result.put("message", "Failed to update schedule");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Cancel a schedule
     * Map containing success flag and message
     */
    public static Map<String, Object> cancelSchedule(int scheduleID) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Schedule schedule = new Schedule();
            schedule.scheduleID = scheduleID;
            
            if (schedule.getRecord() != 1) {
                result.put("success", false);
                result.put("message", "Schedule not found");
                return result;
            }
            
            // Check if schedule has departed or is completed
            if ("Departed".equals(schedule.status) || 
                "Completed".equals(schedule.status)) {
                result.put("success", false);
                result.put("message", "Cannot cancel a schedule that has departed or is completed");
                return result;
            }
            
            // Check if there are tickets sold for this schedule
            int ticketCount = getTicketCountForSchedule(scheduleID);
            if (ticketCount > 0) {
                result.put("success", false);
                result.put("message", "Cannot cancel a schedule with " + ticketCount + " tickets sold");
                return result;
            }
            
            // Update schedule status to cancelled
            schedule.status = "Cancelled";
            
            if (schedule.modRecord() == 1) {
                // Update bus status if this was its only schedule
                if (!hasBusOtherSchedules(schedule.busID, scheduleID)) {
                    Bus bus = new Bus();
                    bus.busID = schedule.busID;
                    if (bus.getRecord() == 1) {
                        if ("Scheduled".equals(bus.status)) {
                            bus.status = "Available";
                            bus.modRecord();
                        }
                    }
                }
                
                result.put("success", true);
                result.put("message", "Schedule cancelled successfully");
            } else {
                result.put("success", false);
                result.put("message", "Failed to cancel schedule");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Delete a schedule
     * Map containing success flag and message
     */
    public static Map<String, Object> deleteSchedule(int scheduleID) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Schedule schedule = new Schedule();
            schedule.scheduleID = scheduleID;
            
            if (schedule.getRecord() != 1) {
                result.put("success", false);
                result.put("message", "Schedule not found");
                return result;
            }
            
            // Check if schedule has departed or is completed
            if ("Departed".equals(schedule.status) || 
                "Completed".equals(schedule.status)) {
                result.put("success", false);
                result.put("message", "Cannot delete a schedule that has departed or is completed");
                return result;
            }
            
            // Check if there are tickets sold for this schedule
            int ticketCount = getTicketCountForSchedule(scheduleID);
            if (ticketCount > 0) {
                result.put("success", false);
                result.put("message", "Cannot delete a schedule with " + ticketCount + " tickets sold");
                return result;
            }
            
            int busID = schedule.busID;
            
            // Delete the schedule
            if (schedule.delRecord() == 1) {
                // Update bus status if this was its only schedule
                if (!hasBusOtherSchedules(busID, 0)) {
                    Bus bus = new Bus();
                    bus.busID = busID;
                    if (bus.getRecord() == 1) {
                        if ("Scheduled".equals(bus.status)) {
                            bus.status = "Available";
                            bus.modRecord();
                        }
                    }
                }
                
                result.put("success", true);
                result.put("message", "Schedule deleted successfully");
            } else {
                result.put("success", false);
                result.put("message", "Failed to delete schedule");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get detailed schedule information including route and bus details
     */
    public static Map<String, Object> getScheduleDetails(int scheduleID) {
        Map<String, Object> details = new HashMap<>();
        
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT s.*, b.bus_number, b.capacity, b.status as bus_status, " +
                "r.route_name, r.base_fare, " +
                "t1.terminal_name as origin_terminal, t2.terminal_name as destination_terminal " +
                "FROM Schedule s " +
                "JOIN Bus b ON s.bus_id = b.bus_id " +
                "JOIN Route r ON s.route_id = r.route_id " +
                "JOIN Terminal t1 ON r.origin_id = t1.terminal_id " +
                "JOIN Terminal t2 ON r.destination_id = t2.terminal_id " +
                "WHERE s.schedule_id = ?");
            pStmt.setInt(1, scheduleID);
            
            ResultSet rs = pStmt.executeQuery();
            
            if (rs.next()) {
                details.put("schedule_id", rs.getInt("schedule_id"));
                details.put("bus_id", rs.getInt("bus_id"));
                details.put("bus_number", rs.getString("bus_number"));
                details.put("capacity", rs.getInt("capacity"));
                details.put("bus_status", rs.getString("bus_status"));
                details.put("route_id", rs.getInt("route_id"));
                details.put("route_name", rs.getString("route_name"));
                details.put("departure_time", rs.getTimestamp("departure_time"));
                details.put("arrival_time", rs.getTimestamp("arrival_time"));
                details.put("status", rs.getString("status"));
                details.put("origin_terminal", rs.getString("origin_terminal"));
                details.put("destination_terminal", rs.getString("destination_terminal"));
                details.put("base_fare", rs.getDouble("base_fare"));
                
                // Get ticket count and availability
                int ticketCount = getTicketCountForSchedule(scheduleID);
                int capacity = rs.getInt("capacity");
                
                details.put("ticket_count", ticketCount);
                details.put("available_seats", capacity - ticketCount);
                details.put("occupancy_percentage", (ticketCount / (double)capacity) * 100);
                
                // Check if the schedule is in the past
                Timestamp departureTime = rs.getTimestamp("departure_time");
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                
                details.put("is_past", departureTime.before(currentTime));
                
                // Get assigned staff for the bus
                PreparedStatement staffStmt = conn.prepareStatement(
                    "SELECT s.staff_id, s.staff_name, r.role_name " +
                    "FROM Staff s " +
                    "JOIN Role r ON s.role_id = r.role_id " +
                    "WHERE s.assigned_bus = ?");
                staffStmt.setInt(1, rs.getInt("bus_id"));
                
                ResultSet staffRs = staffStmt.executeQuery();
                List<Map<String, Object>> assignedStaff = new ArrayList<>();
                
                while (staffRs.next()) {
                    Map<String, Object> staff = new HashMap<>();
                    staff.put("staff_id", staffRs.getInt("staff_id"));
                    staff.put("staff_name", staffRs.getString("staff_name"));
                    staff.put("role_name", staffRs.getString("role_name"));
                    
                    assignedStaff.add(staff);
                }
                
                details.put("assigned_staff", assignedStaff);
                
                staffRs.close();
                staffStmt.close();
            }
            
            rs.close();
            pStmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return details;
    }
    
    /**
     * Get upcoming schedules for a specific terminal
     */
    public static List<Map<String, Object>> getUpcomingSchedulesByTerminal(int terminalID) {
        List<Map<String, Object>> schedules = new ArrayList<>();
        
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT s.*, b.bus_number, b.capacity, " +
                "r.route_name, r.base_fare, " +
                "t1.terminal_name as origin_terminal, t2.terminal_name as destination_terminal, " +
                "COUNT(tk.ticket_id) as ticket_count " +
                "FROM Schedule s " +
                "JOIN Bus b ON s.bus_id = b.bus_id " +
                "JOIN Route r ON s.route_id = r.route_id " +
                "JOIN Terminal t1 ON r.origin_id = t1.terminal_id " +
                "JOIN Terminal t2 ON r.destination_id = t2.terminal_id " +
                "LEFT JOIN Ticket tk ON s.schedule_id = tk.schedule_id " +
                "WHERE (r.origin_id = ? OR r.destination_id = ?) " +
                "AND s.departure_time > NOW() " +
                "AND s.status = 'Scheduled' " +
                "GROUP BY s.schedule_id " +
                "ORDER BY s.departure_time ASC " +
                "LIMIT 50");
                
            pStmt.setInt(1, terminalID);
            pStmt.setInt(2, terminalID);
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> schedule = new HashMap<>();
                schedule.put("schedule_id", rs.getInt("schedule_id"));
                schedule.put("bus_id", rs.getInt("bus_id"));
                schedule.put("bus_number", rs.getString("bus_number"));
                schedule.put("route_id", rs.getInt("route_id"));
                schedule.put("route_name", rs.getString("route_name"));
                schedule.put("departure_time", rs.getTimestamp("departure_time"));
                schedule.put("arrival_time", rs.getTimestamp("arrival_time"));
                schedule.put("status", rs.getString("status"));
                schedule.put("origin_terminal", rs.getString("origin_terminal"));
                schedule.put("destination_terminal", rs.getString("destination_terminal"));
                schedule.put("base_fare", rs.getDouble("base_fare"));
                
                int capacity = rs.getInt("capacity");
                int ticketCount = rs.getInt("ticket_count");
                
                schedule.put("capacity", capacity);
                schedule.put("ticket_count", ticketCount);
                schedule.put("available_seats", capacity - ticketCount);
                schedule.put("is_full", ticketCount >= capacity);
                
                // Check if this schedule is departing from the specified terminal
                boolean isDeparting = rs.getInt("origin_id") == terminalID;
                schedule.put("is_departing", isDeparting);
                schedule.put("is_arriving", !isDeparting);
                
                schedules.add(schedule);
            }
            
            rs.close();
            pStmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return schedules;
    }
    
    /**
     * Get today's schedule
     */
    public static List<Map<String, Object>> getTodaySchedule() {
        List<Map<String, Object>> schedules = new ArrayList<>();
        
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT s.*, b.bus_number, b.capacity, " +
                "r.route_name, r.base_fare, " +
                "t1.terminal_name as origin_terminal, t2.terminal_name as destination_terminal, " +
                "COUNT(tk.ticket_id) as ticket_count " +
                "FROM Schedule s " +
                "JOIN Bus b ON s.bus_id = b.bus_id " +
                "JOIN Route r ON s.route_id = r.route_id " +
                "JOIN Terminal t1 ON r.origin_id = t1.terminal_id " +
                "JOIN Terminal t2 ON r.destination_id = t2.terminal_id " +
                "LEFT JOIN Ticket tk ON s.schedule_id = tk.schedule_id " +
                "WHERE DATE(s.departure_time) = CURRENT_DATE " +
                "GROUP BY s.schedule_id " +
                "ORDER BY s.departure_time ASC");
                
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> schedule = new HashMap<>();
                schedule.put("schedule_id", rs.getInt("schedule_id"));
                schedule.put("bus_id", rs.getInt("bus_id"));
                schedule.put("bus_number", rs.getString("bus_number"));
                schedule.put("route_id", rs.getInt("route_id"));
                schedule.put("route_name", rs.getString("route_name"));
                schedule.put("departure_time", rs.getTimestamp("departure_time"));
                schedule.put("arrival_time", rs.getTimestamp("arrival_time"));
                schedule.put("status", rs.getString("status"));
                schedule.put("origin_terminal", rs.getString("origin_terminal"));
                schedule.put("destination_terminal", rs.getString("destination_terminal"));
                schedule.put("base_fare", rs.getDouble("base_fare"));
                
                int capacity = rs.getInt("capacity");
                int ticketCount = rs.getInt("ticket_count");
                
                schedule.put("capacity", capacity);
                schedule.put("ticket_count", ticketCount);
                schedule.put("available_seats", capacity - ticketCount);
                
                // Check if departure time has passed
                Timestamp departureTime = rs.getTimestamp("departure_time");
                Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                boolean hasDeparted = departureTime.before(currentTime);
                
                schedule.put("has_departed", hasDeparted);
                
                schedules.add(schedule);
            }
            
            rs.close();
            pStmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return schedules;
    }
     
    /**
     * Check if a bus is available for scheduling at the given time period
     */
    public static boolean isBusAvailableForSchedule(int busID, Timestamp departureTime, Timestamp arrivalTime) {
        try {
            // Check if bus exists and is not in maintenance
            Bus bus = new Bus();
            bus.busID = busID;
            if (bus.getRecord() != 1 || "Maintenance".equals(bus.status)) {
                return false;
            }
            
            // Check for scheduling conflicts
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT COUNT(*) as conflict_count FROM Schedule " +
                "WHERE bus_id = ? AND status IN ('Scheduled', 'Departed') " +
                "AND ((departure_time BETWEEN ? AND ?) OR " +
                "     (arrival_time BETWEEN ? AND ?) OR " +
                "     (departure_time <= ? AND arrival_time >= ?))");
                
            pStmt.setInt(1, busID);
            pStmt.setTimestamp(2, departureTime);
            pStmt.setTimestamp(3, arrivalTime);
            pStmt.setTimestamp(4, departureTime);
            pStmt.setTimestamp(5, arrivalTime);
            pStmt.setTimestamp(6, departureTime);
            pStmt.setTimestamp(7, arrivalTime);
            
            ResultSet rs = pStmt.executeQuery();
            
            boolean isAvailable = true;
            if (rs.next()) {
                isAvailable = rs.getInt("conflict_count") == 0;
            }
            
            rs.close();
            pStmt.close();
            conn.close();
            
            return isAvailable;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a bus is available for scheduling at the given time period, excluding the current schedule
     */
    public static boolean isBusAvailableForScheduleExcludingCurrent(int busID, Timestamp departureTime, 
                                                                  Timestamp arrivalTime, int scheduleID) {
        try {
            // Check if bus exists and is not in maintenance
            Bus bus = new Bus();
            bus.busID = busID;
            if (bus.getRecord() != 1 || "Maintenance".equals(bus.status)) {
                return false;
            }
            
            // Check for scheduling conflicts
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT COUNT(*) as conflict_count FROM Schedule " +
                "WHERE bus_id = ? AND status IN ('Scheduled', 'Departed') " +
                "AND schedule_id != ? " +
                "AND ((departure_time BETWEEN ? AND ?) OR " +
                "     (arrival_time BETWEEN ? AND ?) OR " +
                "     (departure_time <= ? AND arrival_time >= ?))");
                
            pStmt.setInt(1, busID);
            pStmt.setInt(2, scheduleID);
            pStmt.setTimestamp(3, departureTime);
            pStmt.setTimestamp(4, arrivalTime);
            pStmt.setTimestamp(5, departureTime);
            pStmt.setTimestamp(6, arrivalTime);
            pStmt.setTimestamp(7, departureTime);
            pStmt.setTimestamp(8, arrivalTime);
            
            ResultSet rs = pStmt.executeQuery();
            
            boolean isAvailable = true;
            if (rs.next()) {
                isAvailable = rs.getInt("conflict_count") == 0;
            }
            
            rs.close();
            pStmt.close();
            conn.close();
            
            return isAvailable;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a bus has other scheduled trips
     */
    public static boolean hasBusOtherSchedules(int busID, int excludeScheduleID) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT COUNT(*) as schedule_count FROM Schedule " +
                "WHERE bus_id = ? AND schedule_id != ? " +
                "AND status IN ('Scheduled', 'Departed')");
                
            pStmt.setInt(1, busID);
            pStmt.setInt(2, excludeScheduleID);
            
            ResultSet rs = pStmt.executeQuery();
            
            boolean hasOtherSchedules = false;
            if (rs.next()) {
                hasOtherSchedules = rs.getInt("schedule_count") > 0;
            }
            
            rs.close();
            pStmt.close();
            conn.close();
            
            return hasOtherSchedules;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the count of tickets for a schedule
     */
    public static int getTicketCountForSchedule(int scheduleID) {
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
}

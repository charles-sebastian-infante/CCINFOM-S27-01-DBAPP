package com.busterminal.service;

import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;

public class ReportService {
    
   public static String[] getMonthDateRange(int year, int month) {
        // Input validation
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Invalid year: " + year + ". Year must be between 1900 and 2100.");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month + ". Month must be between 1 and 12.");
        }
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = sdf.format(calendar.getTime());
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = sdf.format(calendar.getTime());
        
        return new String[]{startDate, endDate};
    }
    
   public static Map<String, Object> getDailyScheduleByTerminal(int terminalID, String date) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> schedules = new ArrayList<>();
        int totalDepartures = 0;
        int totalArrivals = 0;
        
        // Input validation
        if (terminalID <= 0) {
            throw new IllegalArgumentException("Terminal ID must be positive.");
        }
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be null or empty.");
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            
            // Get schedules departing from terminal
            String departQuery = 
                "SELECT s.schedule_id, s.bus_id, b.bus_number, r.route_id, r.route_name, " +
                "t2.terminal_name as destination, s.departure_time, s.arrival_time, s.status, " +
                "COUNT(tk.ticket_id) as passenger_count " +
                "FROM Schedule s " +
                "JOIN Bus b ON s.bus_id = b.bus_id " +
                "JOIN Route r ON s.route_id = r.route_id " +
                "JOIN Terminal t1 ON r.origin_id = t1.terminal_id " +
                "JOIN Terminal t2 ON r.destination_id = t2.terminal_id " +
                "LEFT JOIN Ticket tk ON s.schedule_id = tk.schedule_id " +
                "WHERE t1.terminal_id = ? AND DATE(s.departure_time) = ? " +
                "GROUP BY s.schedule_id " +
                "ORDER BY s.departure_time ASC";
            
            try (PreparedStatement departStmt = conn.prepareStatement(departQuery)) {
                departStmt.setInt(1, terminalID);
                departStmt.setString(2, date);
                
                try (ResultSet departRs = departStmt.executeQuery()) {
                    while (departRs.next()) {
                        Map<String, Object> schedule = new HashMap<>();
                        schedule.put("schedule_id", departRs.getInt("schedule_id"));
                        schedule.put("type", "Departure");
                        schedule.put("bus_id", departRs.getInt("bus_id"));
                        schedule.put("bus_number", departRs.getString("bus_number"));
                        schedule.put("route_id", departRs.getInt("route_id"));
                        schedule.put("route_name", departRs.getString("route_name"));
                        schedule.put("destination", departRs.getString("destination"));
                        schedule.put("departure_time", departRs.getString("departure_time"));
                        schedule.put("arrival_time", departRs.getString("arrival_time"));
                        schedule.put("status", departRs.getString("status"));
                        schedule.put("passenger_count", departRs.getInt("passenger_count"));
                        
                        schedules.add(schedule);
                        totalDepartures++;
                    }
                }
            }
            
            // Get schedules arriving at terminal
            String arriveQuery = 
                "SELECT s.schedule_id, s.bus_id, b.bus_number, r.route_id, r.route_name, " +
                "t1.terminal_name as origin, s.departure_time, s.arrival_time, s.status, " +
                "COUNT(tk.ticket_id) as passenger_count " +
                "FROM Schedule s " +
                "JOIN Bus b ON s.bus_id = b.bus_id " +
                "JOIN Route r ON s.route_id = r.route_id " +
                "JOIN Terminal t1 ON r.origin_id = t1.terminal_id " +
                "JOIN Terminal t2 ON r.destination_id = t2.terminal_id " +
                "LEFT JOIN Ticket tk ON s.schedule_id = tk.schedule_id " +
                "WHERE t2.terminal_id = ? AND DATE(s.arrival_time) = ? " +
                "GROUP BY s.schedule_id " +
                "ORDER BY s.arrival_time ASC";
            
            try (PreparedStatement arriveStmt = conn.prepareStatement(arriveQuery)) {
                arriveStmt.setInt(1, terminalID);
                arriveStmt.setString(2, date);
                
                try (ResultSet arriveRs = arriveStmt.executeQuery()) {
                    while (arriveRs.next()) {
                        Map<String, Object> schedule = new HashMap<>();
                        schedule.put("schedule_id", arriveRs.getInt("schedule_id"));
                        schedule.put("type", "Arrival");
                        schedule.put("bus_id", arriveRs.getInt("bus_id"));
                        schedule.put("bus_number", arriveRs.getString("bus_number"));
                        schedule.put("route_id", arriveRs.getInt("route_id"));
                        schedule.put("route_name", arriveRs.getString("route_name"));
                        schedule.put("origin", arriveRs.getString("origin"));
                        schedule.put("departure_time", arriveRs.getString("departure_time"));
                        schedule.put("arrival_time", arriveRs.getString("arrival_time"));
                        schedule.put("status", arriveRs.getString("status"));
                        schedule.put("passenger_count", arriveRs.getInt("passenger_count"));
                        
                        schedules.add(schedule);
                        totalArrivals++;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error in getDailyScheduleByTerminal: " + e.getMessage());
            e.printStackTrace();
        }
        
        report.put("terminal_id", terminalID);
        report.put("date", date);
        report.put("schedules", schedules);
        report.put("total_departures", totalDepartures);
        report.put("total_arrivals", totalArrivals);
        
        return report;
    }
    
   public static Map<String, Object> getRouteUsageAnalysis(int year, int month) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> routeUsage = new ArrayList<>();
        
        // Input validation
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        
        try (Connection conn = DBConnection.getConnection()) {
            
            String query = 
                "SELECT r.route_id, r.route_name, t1.terminal_name as origin, " +
                "t2.terminal_name as destination, " +
                "COUNT(tk.ticket_id) as passenger_count, " +
                "SUM(CASE WHEN tk.discounted = 0 THEN r.base_fare " +
                "     WHEN tk.discounted = 1 THEN r.base_fare * 0.8 END) as total_revenue " +
                "FROM Route r " +
                "JOIN Terminal t1 ON r.origin_id = t1.terminal_id " +
                "JOIN Terminal t2 ON r.destination_id = t2.terminal_id " +
                "LEFT JOIN Schedule s ON r.route_id = s.route_id AND " +
                "   YEAR(s.departure_time) = ? AND MONTH(s.departure_time) = ? " +
                "LEFT JOIN Ticket tk ON s.schedule_id = tk.schedule_id " +
                "GROUP BY r.route_id " +
                "ORDER BY total_revenue DESC";
            
            try (PreparedStatement pStmt = conn.prepareStatement(query)) {
                pStmt.setInt(1, year);
                pStmt.setInt(2, month);
                
                try (ResultSet rs = pStmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> route = new HashMap<>();
                        route.put("route_id", rs.getInt("route_id"));
                        route.put("route_name", rs.getString("route_name"));
                        route.put("origin", rs.getString("origin"));
                        route.put("destination", rs.getString("destination"));
                        route.put("passenger_count", rs.getInt("passenger_count"));
                        
                        double revenue = rs.getDouble("total_revenue");
                        if (rs.wasNull()) {
                            revenue = 0.0;
                        }
                        route.put("total_revenue", revenue);
                        
                        routeUsage.add(route);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error in getRouteUsageAnalysis: " + e.getMessage());
            e.printStackTrace();
        }
        
        report.put("year", year);
        report.put("month", month);
        report.put("route_usage", routeUsage);
        
        return report;
    }
    
   public static Map<String, Object> getMaintenanceCostReport(int year, int month) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> maintenanceByDay = new ArrayList<>();
        
        // Input validation
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        
        String[] dateRange = getMonthDateRange(year, month);
        String startDate = dateRange[0];
        String endDate = dateRange[1];
        
        try (Connection conn = DBConnection.getConnection()) {
            
            String query = 
                "SELECT DATE(m.starting_date) as maintenance_date, " +
                "COUNT(m.maintenance_id) as maintenance_count, " +
                "SUM(mt.maintenance_cost) as daily_cost " +
                "FROM Maintenance m " +
                "JOIN Maintenance_Type mt ON m.maintenance_type_id = mt.maintenance_type_id " +
                "WHERE (DATE(m.starting_date) BETWEEN ? AND ?) OR " +
                "      (DATE(m.completion_time) BETWEEN ? AND ?) OR " +
                "      (m.starting_date <= ? AND (m.completion_time >= ? OR m.completion_time IS NULL)) " +
                "GROUP BY DATE(m.starting_date) " +
                "ORDER BY maintenance_date DESC";
            
            try (PreparedStatement pStmt = conn.prepareStatement(query)) {
                pStmt.setString(1, startDate);
                pStmt.setString(2, endDate);
                pStmt.setString(3, startDate);
                pStmt.setString(4, endDate);
                pStmt.setString(5, endDate);
                pStmt.setString(6, startDate);
                
                try (ResultSet rs = pStmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> day = new HashMap<>();
                        day.put("date", rs.getString("maintenance_date"));
                        day.put("maintenance_count", rs.getInt("maintenance_count"));
                        
                        double dailyCost = rs.getDouble("daily_cost");
                        if (rs.wasNull()) {
                            dailyCost = 0.0;
                        }
                        day.put("daily_cost", dailyCost);
                        
                        maintenanceByDay.add(day);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error in getMaintenanceCostReport: " + e.getMessage());
            e.printStackTrace();
        }
        
        report.put("year", year);
        report.put("month", month);
        report.put("start_date", startDate);
        report.put("end_date", endDate);
        report.put("maintenance_by_day", maintenanceByDay);
        
        return report;
    }
    
   public static Map<String, Object> getBusUtilizationReport(int year, int month) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> busUtilization = new ArrayList<>();
        
        // Input validation
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }
        
        String[] dateRange = getMonthDateRange(year, month);
        String startDate = dateRange[0];
        String endDate = dateRange[1];
        
        try (Connection conn = DBConnection.getConnection()) {
            
            String query = 
                "SELECT b.bus_id, b.bus_number, " +
                "COUNT(s.schedule_id) as trip_count, " +
                "COUNT(tk.ticket_id) as passenger_count " +
                "FROM Bus b " +
                "LEFT JOIN Schedule s ON b.bus_id = s.bus_id AND s.status IN ('Completed', 'Departed') " +
                "   AND DATE(s.departure_time) BETWEEN ? AND ? " +
                "LEFT JOIN Ticket tk ON s.schedule_id = tk.schedule_id " +
                "GROUP BY b.bus_id " +
                "ORDER BY trip_count DESC";
            
            try (PreparedStatement pStmt = conn.prepareStatement(query)) {
                pStmt.setString(1, startDate);
                pStmt.setString(2, endDate);
                
                try (ResultSet rs = pStmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> bus = new HashMap<>();
                        bus.put("bus_id", rs.getInt("bus_id"));
                        bus.put("bus_number", rs.getString("bus_number"));
                        bus.put("trip_count", rs.getInt("trip_count"));
                        bus.put("passenger_count", rs.getInt("passenger_count"));
                        
                        busUtilization.add(bus);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database error in getBusUtilizationReport: " + e.getMessage());
            e.printStackTrace();
        }
        
        report.put("year", year);
        report.put("month", month);
        report.put("start_date", startDate);
        report.put("end_date", endDate);
        report.put("bus_utilization", busUtilization);
        
        return report;
    }
}

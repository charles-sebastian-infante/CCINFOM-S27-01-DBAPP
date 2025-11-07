package com.busterminal.model;
import java.sql.*;
import java.util.*;
import com.busterminal.utils.DBConnection;

public class Schedule {
    public int scheduleID;
    public int busID;
    public int routeID;
    public Timestamp departureTime;
    public Timestamp arrivalTime;
    public String status; // ENUM('Scheduled', 'Departed', 'Completed', 'Cancelled')
    
    public Schedule() {
        scheduleID = 0;
        busID = 0;
        routeID = 0;
        departureTime = null;
        arrivalTime = null;
        status = "Scheduled"; // Default as per DB schema
    }
    
    public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Schedule (bus_id, route_id, departure_time, " +
                "arrival_time, status) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            );
            pStmt.setInt(1, busID);
            pStmt.setInt(2, routeID);
            pStmt.setTimestamp(3, departureTime);
            pStmt.setTimestamp(4, arrivalTime);
            pStmt.setString(5, status);
            
            int affectedRows = pStmt.executeUpdate();
            
            if (affectedRows == 0) {
                return 0;
            }
            
            try (ResultSet generatedKeys = pStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    scheduleID = generatedKeys.getInt(1);
                }
            }
            
            pStmt.close();
            conn.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
    public int modRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "UPDATE Schedule SET bus_id = ?, route_id = ?, departure_time = ?, " +
                "arrival_time = ?, status = ? WHERE schedule_id = ?"
            );
            pStmt.setInt(1, busID);
            pStmt.setInt(2, routeID);
            pStmt.setTimestamp(3, departureTime);
            pStmt.setTimestamp(4, arrivalTime);
            pStmt.setString(5, status);
            pStmt.setInt(6, scheduleID);
            pStmt.executeUpdate();
            pStmt.close();
            conn.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
    public int delRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "DELETE FROM Schedule WHERE schedule_id = ?");
            pStmt.setInt(1, scheduleID);
            pStmt.executeUpdate();
            pStmt.close();
            conn.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
    public int getRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Schedule WHERE schedule_id = ?");
            pStmt.setInt(1, scheduleID);
            ResultSet rs = pStmt.executeQuery();
            
            scheduleID = 0;
            busID = 0;
            routeID = 0;
            departureTime = null;
            arrivalTime = null;
            status = "";
            
            while (rs.next()) {
                scheduleID = rs.getInt("schedule_id");
                busID = rs.getInt("bus_id");
                routeID = rs.getInt("route_id");
                departureTime = rs.getTimestamp("departure_time");
                arrivalTime = rs.getTimestamp("arrival_time");
                status = rs.getString("status");
            }
            rs.close();
            pStmt.close();
            conn.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
    // Get upcoming schedules
    public static List<Schedule> getUpcomingSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Schedule WHERE departure_time > NOW() " +
                "AND status = 'Scheduled' ORDER BY departure_time");
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Schedule s = new Schedule();
                s.scheduleID = rs.getInt("schedule_id");
                s.busID = rs.getInt("bus_id");
                s.routeID = rs.getInt("route_id");
                s.departureTime = rs.getTimestamp("departure_time");
                s.arrivalTime = rs.getTimestamp("arrival_time");
                s.status = rs.getString("status");
                schedules.add(s);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return schedules;
    }
    
    // Get schedules for a specific bus
    public static List<Schedule> getSchedulesByBusID(int busID) {
        List<Schedule> schedules = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Schedule WHERE bus_id = ? ORDER BY departure_time");
            pStmt.setInt(1, busID);
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Schedule s = new Schedule();
                s.scheduleID = rs.getInt("schedule_id");
                s.busID = rs.getInt("bus_id");
                s.routeID = rs.getInt("route_id");
                s.departureTime = rs.getTimestamp("departure_time");
                s.arrivalTime = rs.getTimestamp("arrival_time");
                s.status = rs.getString("status");
                schedules.add(s);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return schedules;
    }
    
    public static List<Schedule> getCompletedTripsByBusID(int busID) {
        List<Schedule> schedules = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Schedule WHERE bus_id = ? AND status = 'Completed' " +
                "ORDER BY departure_time DESC");
            pStmt.setInt(1, busID);
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Schedule s = new Schedule();
                s.scheduleID = rs.getInt("schedule_id");
                s.busID = rs.getInt("bus_id");
                s.routeID = rs.getInt("route_id");
                s.departureTime = rs.getTimestamp("departure_time");
                s.arrivalTime = rs.getTimestamp("arrival_time");
                s.status = rs.getString("status");
                schedules.add(s);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return schedules;
    }

    public static List<Schedule> getSchedulesByRouteID(int routeID) {
        List<Schedule> schedules = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Schedule WHERE route_id = ? " +
                "ORDER BY departure_time ASC");
            pStmt.setInt(1, routeID);
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Schedule s = new Schedule();
                s.scheduleID = rs.getInt("schedule_id");
                s.busID = rs.getInt("bus_id");
                s.routeID = rs.getInt("route_id");
                s.departureTime = rs.getTimestamp("departure_time");
                s.arrivalTime = rs.getTimestamp("arrival_time");
                s.status = rs.getString("status");
                schedules.add(s);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return schedules;
    }
}

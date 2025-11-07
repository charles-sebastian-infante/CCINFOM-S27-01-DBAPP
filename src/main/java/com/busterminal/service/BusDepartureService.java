package com.busterminal.service;

import java.sql.*;
import java.util.*;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;

public class BusDepartureService {
    public Bus departingBus = new Bus();
    public Schedule departureSchedule = new Schedule();
    public Route busRoute = new Route();
    public ArrayList<Ticket> passengerManifest = new ArrayList<>();

    public BusDepartureService() {
        passengerManifest.clear();
    }

    public int loadBus(int busID) {
        departingBus.busID = busID;
        return departingBus.getRecord();
    }
    
    public int loadSchedule(int scheduleID) {
        departureSchedule.scheduleID = scheduleID;
        return departureSchedule.getRecord();
    }

    public int loadRoute() {
        busRoute.routeID = departureSchedule.routeID;
        return busRoute.getRecord();
    }

    public int loadPassengerManifest(int scheduleID) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Ticket WHERE schedule_id = ?");
            pStmt.setInt(1, scheduleID);
            ResultSet rs = pStmt.executeQuery();
            passengerManifest.clear();

            while (rs.next()) {
                Ticket t = new Ticket();
                t.ticketID = rs.getInt("ticket_id");
                t.ticketNumber = rs.getString("ticket_number");
                t.scheduleID = rs.getInt("schedule_id");
                t.discounted = rs.getBoolean("discounted");
                passengerManifest.add(t);
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

    public int updateScheduleStatus() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "UPDATE Schedule SET status = 'Departed' " +
                "WHERE schedule_id = ?");
            pStmt.setInt(1, departureSchedule.scheduleID);
            pStmt.executeUpdate();
            pStmt.close();
            conn.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public int updateBusStatus() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "UPDATE Bus SET status = 'In Transit' WHERE bus_id = ?");
            pStmt.setInt(1, departingBus.busID);
            pStmt.executeUpdate();
            pStmt.close();
            conn.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public int confirmDeparture() {
        if (updateScheduleStatus() == 1 && updateBusStatus() == 1) 
            return 1;

        return 0;
    }
    
    // Get scheduled departures for a terminal
    public static List<Map<String, Object>> getScheduledDepartures(int terminalID) {
        List<Map<String, Object>> departures = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT s.*, b.bus_number, r.route_name, " +
                "t1.terminal_name as origin, t2.terminal_name as destination " +
                "FROM Schedule s " +
                "JOIN Bus b ON s.bus_id = b.bus_id " +
                "JOIN Route r ON s.route_id = r.route_id " +
                "JOIN Terminal t1 ON r.origin_id = t1.terminal_id " +
                "JOIN Terminal t2 ON r.destination_id = t2.terminal_id " +
                "WHERE r.origin_id = ? AND s.status = 'Scheduled' " +
                "AND s.departure_time > NOW() " +
                "ORDER BY s.departure_time");
            pStmt.setInt(1, terminalID);
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> departure = new HashMap<>();
                departure.put("schedule_id", rs.getInt("schedule_id"));
                departure.put("bus_id", rs.getInt("bus_id"));
                departure.put("bus_number", rs.getString("bus_number"));
                departure.put("route_id", rs.getInt("route_id"));
                departure.put("route_name", rs.getString("route_name"));
                departure.put("departure_time", rs.getTimestamp("departure_time"));
                departure.put("arrival_time", rs.getTimestamp("arrival_time"));
                departure.put("status", rs.getString("status"));
                departure.put("origin", rs.getString("origin"));
                departure.put("destination", rs.getString("destination"));
                
                // Get ticket count
                PreparedStatement ticketStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as ticket_count FROM Ticket " +
                    "WHERE schedule_id = ?");
                ticketStmt.setInt(1, rs.getInt("schedule_id"));
                ResultSet ticketRs = ticketStmt.executeQuery();
                
                if (ticketRs.next()) {
                    departure.put("ticket_count", ticketRs.getInt("ticket_count"));
                }
                
                ticketRs.close();
                ticketStmt.close();
                
                departures.add(departure);
            }
            
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return departures;
    }
}

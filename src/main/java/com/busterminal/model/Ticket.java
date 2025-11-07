package com.busterminal.model;
import java.sql.*;
import java.util.*;
import com.busterminal.utils.DBConnection;

public class Ticket {
    public int ticketID;
    public String ticketNumber;
    public int scheduleID;
    public boolean discounted;
    
    public Ticket() {
        ticketID = 0;
        ticketNumber = "";
        scheduleID = 0;
        discounted = false;
    }
    
    public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Ticket (ticket_number, schedule_id, discounted) " +
                "VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            );
            pStmt.setString(1, ticketNumber);
            pStmt.setInt(2, scheduleID);
            pStmt.setBoolean(3, discounted);
            
            int affectedRows = pStmt.executeUpdate();
            
            if (affectedRows == 0) {
                return 0;
            }
            
            try (ResultSet generatedKeys = pStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ticketID = generatedKeys.getInt(1);
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
                "UPDATE Ticket SET ticket_number = ?, schedule_id = ?, " +
                "discounted = ? WHERE ticket_id = ?"
            );
            pStmt.setString(1, ticketNumber);
            pStmt.setInt(2, scheduleID);
            pStmt.setBoolean(3, discounted);
            pStmt.setInt(4, ticketID);
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
                "DELETE FROM Ticket WHERE ticket_id = ?");
            pStmt.setInt(1, ticketID);
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
                "SELECT * FROM Ticket WHERE ticket_id = ?");
            pStmt.setInt(1, ticketID);
            ResultSet rs = pStmt.executeQuery();
            
            ticketID = 0;
            ticketNumber = "";
            scheduleID = 0;
            discounted = false;
            
            while (rs.next()) {
                ticketID = rs.getInt("ticket_id");
                ticketNumber = rs.getString("ticket_number");
                scheduleID = rs.getInt("schedule_id");
                discounted = rs.getBoolean("discounted");
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
    
    // Get tickets by schedule ID
    public static List<Ticket> getTicketsByScheduleID(int scheduleID) {
        List<Ticket> tickets = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Ticket WHERE schedule_id = ?");
            pStmt.setInt(1, scheduleID);
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Ticket t = new Ticket();
                t.ticketID = rs.getInt("ticket_id");
                t.ticketNumber = rs.getString("ticket_number");
                t.scheduleID = rs.getInt("schedule_id");
                t.discounted = rs.getBoolean("discounted");
                tickets.add(t);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return tickets;
    }
    
    // Generate a unique ticket number
    public static String generateTicketNumber() {
        return "TKT-" + System.currentTimeMillis();
    }
    
    // Calculate fare based on route and discount status
    public double calculateFare(int routeID) {
        double fare = 0.0;
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT base_fare FROM Route WHERE route_id = ?");
            pStmt.setInt(1, routeID);
            ResultSet rs = pStmt.executeQuery();
            
            if (rs.next()) {
                fare = rs.getDouble("base_fare");
                
                // Apply discount if applicable
                if (discounted) {
                    fare = fare * 0.8; // 20% discount
                }
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return fare;
    }
}

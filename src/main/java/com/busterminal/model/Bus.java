package com.busterminal.model;
import java.util.*;
import java.sql.*;
import com.busterminal.utils.DBConnection;

public class Bus {
    public int busID;
    public String busNumber;
    public int capacity;
    public String status; // ENUM('Available', 'In Transit', 'Scheduled', 'Maintenance')
    public int currentTerminal;
    
    public Bus() {
        busID = 0;
        busNumber = "";
        capacity = 45; // Default as per DB schema
        status = "Available"; // Default as per DB schema
        currentTerminal = 0;
    }
    
    public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Bus (bus_number, capacity, status, " +
                "current_terminal) VALUES (?,?,?,?)"
            );
            pStmt.setString(1, busNumber);
            pStmt.setInt(2, capacity);
            pStmt.setString(3, status);
            pStmt.setInt(4, currentTerminal);
            pStmt.executeUpdate();
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
                "UPDATE Bus SET bus_number = ?, capacity = ?, status = ?, " +
                "current_terminal = ? WHERE bus_id = ?"
            );
            pStmt.setString(1, busNumber);
            pStmt.setInt(2, capacity);
            pStmt.setString(3, status);
            pStmt.setInt(4, currentTerminal);
            pStmt.setInt(5, busID);
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
                "DELETE FROM Bus WHERE bus_id = ?");
            pStmt.setInt(1, busID);
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
                "SELECT * FROM Bus WHERE bus_id = ?");
            pStmt.setInt(1, busID);
            ResultSet rs = pStmt.executeQuery();
            
            busID = 0;
            busNumber = "";
            capacity = 0;
            status = "";
            currentTerminal = 0;
            
            while (rs.next()) {
                busID = rs.getInt("bus_id");
                busNumber = rs.getString("bus_number");
                capacity = rs.getInt("capacity");
                status = rs.getString("status");
                currentTerminal = rs.getInt("current_terminal");
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
    
    // Get all buses method
    public static List<Bus> getAllBuses() {
        List<Bus> buses = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Bus");
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Bus b = new Bus();
                b.busID = rs.getInt("bus_id");
                b.busNumber = rs.getString("bus_number");
                b.capacity = rs.getInt("capacity");
                b.status = rs.getString("status");
                b.currentTerminal = rs.getInt("current_terminal");
                buses.add(b);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return buses;
    }
    
    // Get available buses method
    public static List<Bus> getAvailableBuses() {
        List<Bus> buses = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Bus WHERE status = 'Available'");
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Bus b = new Bus();
                b.busID = rs.getInt("bus_id");
                b.busNumber = rs.getString("bus_number");
                b.capacity = rs.getInt("capacity");
                b.status = rs.getString("status");
                b.currentTerminal = rs.getInt("current_terminal");
                buses.add(b);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return buses;
    }
}

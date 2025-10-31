package com.busterminal.model;
import java.sql.*;
import com.busterminal.utils.DBConnection;

public class Bus {
    public int busID;
    public String busNumber;
    public int capacity;
    public String status;
    public int currentTerminal;
    
    public Bus() {
        busID = 0;
        busNumber = "";
        capacity = 45;
        status = "Available";
        currentTerminal = 0;
    }
    
    public int addRecord() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Bus (bus_number, capacity, status, " +
                "current_terminal) VALUES (?,?,?,?)"
            )) {
            pStmt.setString(1, busNumber);
            pStmt.setInt(2, capacity);
            pStmt.setString(3, status);
            pStmt.setInt(4, currentTerminal);
            pStmt.executeUpdate();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
    public int modRecord() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(
                "UPDATE Bus SET bus_number = ?, capacity = ?, status = ?, " +
                "current_terminal = ? WHERE bus_id = ?"
            )) {
            pStmt.setString(1, busNumber);
            pStmt.setInt(2, capacity);
            pStmt.setString(3, status);
            pStmt.setInt(4, currentTerminal);
            pStmt.setInt(5, busID);
            pStmt.executeUpdate();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
    public int delRecord() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(
                "DELETE FROM Bus WHERE bus_id = ?")) {
            pStmt.setInt(1, busID);
            pStmt.executeUpdate();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
    public int getRecord() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Bus WHERE bus_id = ?")) {
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
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public static void main(String args[]) {
        Bus a = new Bus();
        a.busNumber = "Bus-111";
        a.capacity = 45;
        a.status = "Available";
        a.currentTerminal = 1;
        int result = a.addRecord();
        if (result == 1)
            System.out.println(" Added");
        else
            System.out.println(" Failed");
    }
    
}

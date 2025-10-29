package com.busterminal.model;
import java.sql.*;
import com.busterminal.utils.DBConnection;

public class Bus {
    public int busID;
    public String busNumber;
    public int capacity;
    public String status;
    public int currentTerminal;
    public int routeID;
    
    public Bus() {
        busID = 0;
        busNumber = "";
        capacity = 45;
        status = "Available";
        currentTerminal = 0;
        routeID = 0;
    }
    
    public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Bus (bus_number, capacity, status, current_terminal, route_id) VALUES (?,?,?,?,?)"
            );
            pStmt.setString(1, busNumber);
            pStmt.setInt(2, capacity);
            pStmt.setString(3, status);
            pStmt.setInt(4, currentTerminal);
            pStmt.setInt(5, routeID);
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
                "UPDATE Bus SET bus_number = ?, capacity = ?, status = ?, current_terminal = ?, route_id = ? WHERE bus_id = ?"
            );
            pStmt.setString(1, busNumber);
            pStmt.setInt(2, capacity);
            pStmt.setString(3, status);
            pStmt.setInt(4, currentTerminal);
            pStmt.setInt(5, routeID);
            pStmt.setInt(6, busID);
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
            PreparedStatement pStmt = conn.prepareStatement("DELETE FROM Bus WHERE bus_id = ?");
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
            PreparedStatement pStmt = conn.prepareStatement("SELECT * FROM Bus WHERE bus_id = ?");
            pStmt.setInt(1, busID);
            ResultSet rs = pStmt.executeQuery();
            
            busID = 0;
            busNumber = "";
            capacity = 0;
            status = "";
            currentTerminal = 0;
            routeID = 0;
            
            while (rs.next()) {
                busID = rs.getInt("bus_id");
                busNumber = rs.getString("bus_number");
                capacity = rs.getInt("capacity");
                status = rs.getString("status");
                currentTerminal = rs.getInt("current_terminal");
                routeID = rs.getInt("route_id");
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

    public static void main(String args[]) {
        // --- TEST CODE (no constructor used) ---
        Bus a = new Bus();

        a.busNumber = "Bus-111";
        a.capacity = 45;
        a.status = "Available"; // must not be NULL
        a.currentTerminal = 1; // must exist in Terminal table
        a.routeID = 1; // must exist in Route table (if foreign key)

        int result = a.addRecord();

        if (result == 1)
            System.out.println(" Added");
        else
            System.out.println(" Failed");

    }
    
}

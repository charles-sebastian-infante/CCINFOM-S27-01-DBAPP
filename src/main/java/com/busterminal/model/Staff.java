package com.busterminal.model;

import java.sql.*;
import com.busterminal.utils.DBConnection;

public class Staff {
    public int staffID;
    public String staffName;
    public String role;
    public int assignedTerminal;
    public int assignedBus;
    public String shift;
    public String contact;
    
   public Staff() {
        staffID = 0;
        staffName = "";
        role = "";
        assignedTerminal = 0;
        assignedBus = 0;
        shift = "";
        contact = "";
    }
    
   public int addRecord() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Staff (staff_name, role, assigned_terminal," +
                " assigned_bus, shift, contact) VALUES (?,?,?,?,?,?)")) {
            pStmt.setString(1, staffName);
            pStmt.setString(2, role);
            pStmt.setInt(3, assignedTerminal);
            pStmt.setInt(4, assignedBus);
            pStmt.setString(5, shift);
            pStmt.setString(6, contact);
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
                "UPDATE Staff SET staff_name = ?, role = ?," +
                " assigned_terminal = ?, assigned_bus = ?, shift = ?, " +
                " contact = ? WHERE staff_id = ?")) {
            pStmt.setString(1, staffName);
            pStmt.setString(2, role);
            pStmt.setInt(3, assignedTerminal);
            pStmt.setInt(4, assignedBus);
            pStmt.setString(5, shift);
            pStmt.setString(6, contact);
            pStmt.setInt(7, staffID);
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
                "DELETE FROM Staff WHERE staff_id = ?")) {
            pStmt.setInt(1, staffID);
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
                "SELECT * FROM Staff WHERE staff_id = ?")) {
            pStmt.setInt(1, staffID);
            ResultSet rs = pStmt.executeQuery();
            
            staffID = 0;
            staffName = "";
            role = "";
            assignedTerminal = 0;
            assignedBus = 0;
            shift = "";
            contact = "";
            
            while (rs.next()) {
                staffID = rs.getInt("staff_id");
                staffName = rs.getString("staff_name");
                role = rs.getString("role");
                assignedTerminal = rs.getInt("assigned_terminal");
                assignedBus = rs.getInt("assigned_bus");
                shift = rs.getString("shift");
                contact = rs.getString("contact");
            }
            rs.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());  
            return 0;
        }
    }
}

package com.busterminal.model;

import java.sql.*;
import com.busterminal.utils.DBConnection;

public class Maintenance {
    public int maintenanceID;
    public int busID;
    public String maintenanceDate;
    public String description;
    public String status;
    
   public Maintenance() {
        maintenanceID = 0;
        busID = 0;
        maintenanceDate = "";
        description = "";
        status = "Pending";
    }
    
   public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Maintenance (bus_id, maintenance_date," + 
                " description, status) VALUES (?,?,?,?)");
            pStmt.setInt(1, busID);
            pStmt.setString(2, maintenanceDate);
            pStmt.setString(3, description);
            pStmt.setString(4, status);
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
                "UPDATE Maintenance SET bus_id = ?, maintenance_date = ?, " +
                "description = ?, status = ? WHERE maintenance_id = ?");
            pStmt.setInt(1, busID);
            pStmt.setString(2, maintenanceDate);
            pStmt.setString(3, description);
            pStmt.setString(4, status);
            pStmt.setInt(5, maintenanceID);
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
                "DELETE FROM Maintenance WHERE maintenance_id = ?");
            pStmt.setInt(1, maintenanceID);
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
                "SELECT * FROM Maintenance WHERE maintenance_id = ?");
            pStmt.setInt(1, maintenanceID);
            ResultSet rs = pStmt.executeQuery();
            
            maintenanceID = 0;
            busID = 0;
            maintenanceDate = "";
            description = "";
            status = "";
            
            while (rs.next()) {
                maintenanceID = rs.getInt("maintenance_id");
                busID = rs.getInt("bus_id");
                maintenanceDate = rs.getString("maintenance_date");
                description = rs.getString("description");
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
}

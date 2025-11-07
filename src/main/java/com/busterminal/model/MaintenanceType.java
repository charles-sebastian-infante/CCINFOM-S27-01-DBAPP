package com.busterminal.model;
import java.sql.*;
import java.util.*;
import com.busterminal.utils.DBConnection;

public class MaintenanceType {
    public int maintenanceTypeID;
    public String typeName;
    public double maintenanceCost;
    
    public MaintenanceType() {
        maintenanceTypeID = 0;
        typeName = "";
        maintenanceCost = 0.0;
    }
    
    public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Maintenance_Type (type_name, maintenance_cost) " +
                "VALUES (?,?)",
                Statement.RETURN_GENERATED_KEYS
            );
            pStmt.setString(1, typeName);
            pStmt.setDouble(2, maintenanceCost);
            
            int affectedRows = pStmt.executeUpdate();
            
            if (affectedRows == 0) {
                return 0;
            }
            
            try (ResultSet generatedKeys = pStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    maintenanceTypeID = generatedKeys.getInt(1);
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
                "UPDATE Maintenance_Type SET type_name = ?, maintenance_cost = ? " +
                "WHERE maintenance_type_id = ?"
            );
            pStmt.setString(1, typeName);
            pStmt.setDouble(2, maintenanceCost);
            pStmt.setInt(3, maintenanceTypeID);
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
            // First check if this type is used in any maintenance records
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT COUNT(*) as count FROM Maintenance " +
                "WHERE maintenance_type_id = ?");
            checkStmt.setInt(1, maintenanceTypeID);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                // This maintenance type is in use, cannot delete
                rs.close();
                checkStmt.close();
                conn.close();
                return 0;
            }
            
            rs.close();
            checkStmt.close();
            
            // If not in use, proceed with deletion
            PreparedStatement pStmt = conn.prepareStatement(
                "DELETE FROM Maintenance_Type WHERE maintenance_type_id = ?");
            pStmt.setInt(1, maintenanceTypeID);
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
                "SELECT * FROM Maintenance_Type WHERE maintenance_type_id = ?");
            pStmt.setInt(1, maintenanceTypeID);
            ResultSet rs = pStmt.executeQuery();
            
            maintenanceTypeID = 0;
            typeName = "";
            maintenanceCost = 0.0;
            
            while (rs.next()) {
                maintenanceTypeID = rs.getInt("maintenance_type_id");
                typeName = rs.getString("type_name");
                maintenanceCost = rs.getDouble("maintenance_cost");
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
    
    // Get all maintenance types method
    public static List<MaintenanceType> getAllMaintenanceTypes() {
        List<MaintenanceType> types = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Maintenance_Type ORDER BY type_name");
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                MaintenanceType mt = new MaintenanceType();
                mt.maintenanceTypeID = rs.getInt("maintenance_type_id");
                mt.typeName = rs.getString("type_name");
                mt.maintenanceCost = rs.getDouble("maintenance_cost");
                types.add(mt);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return types;
    }
}

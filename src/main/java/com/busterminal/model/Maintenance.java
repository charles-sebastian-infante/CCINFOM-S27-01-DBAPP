package com.busterminal.model;
import java.sql.*;
import java.util.*;
import com.busterminal.utils.DBConnection;

public class Maintenance {
    public int maintenanceID;
    public int busID;
    public int assignedMechanic;
    public int maintenanceTypeID;
    public Timestamp startingDate;
    public Timestamp completionTime;
    
    public Maintenance() {
        maintenanceID = 0;
        busID = 0;
        assignedMechanic = 0;
        maintenanceTypeID = 0;
        startingDate = null;
        completionTime = null;
    }
    
    public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Maintenance (bus_id, assigned_mechanic, " +
                "maintenance_type_id, starting_date, completion_time) " +
                "VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            );
            pStmt.setInt(1, busID);
            
            // Handle nullable fields
            if (assignedMechanic > 0) {
                pStmt.setInt(2, assignedMechanic);
            } else {
                pStmt.setNull(2, Types.INTEGER);
            }
            
            if (maintenanceTypeID > 0) {
                pStmt.setInt(3, maintenanceTypeID);
            } else {
                pStmt.setNull(3, Types.INTEGER);
            }
            
            if (startingDate != null) {
                pStmt.setTimestamp(4, startingDate);
            } else {
                pStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            }
            
            if (completionTime != null) {
                pStmt.setTimestamp(5, completionTime);
            } else {
                pStmt.setNull(5, Types.TIMESTAMP);
            }
            
            int affectedRows = pStmt.executeUpdate();
            
            if (affectedRows == 0) {
                return 0;
            }
            
            try (ResultSet generatedKeys = pStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    maintenanceID = generatedKeys.getInt(1);
                }
            }
            
            // Update bus status to Maintenance
            PreparedStatement updateBusStmt = conn.prepareStatement(
                "UPDATE Bus SET status = 'Maintenance' WHERE bus_id = ?");
            updateBusStmt.setInt(1, busID);
            updateBusStmt.executeUpdate();
            updateBusStmt.close();
            
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
                "UPDATE Maintenance SET bus_id = ?, assigned_mechanic = ?, " +
                "maintenance_type_id = ?, starting_date = ?, completion_time = ? " +
                "WHERE maintenance_id = ?"
            );
            pStmt.setInt(1, busID);
            
            if (assignedMechanic > 0) {
                pStmt.setInt(2, assignedMechanic);
            } else {
                pStmt.setNull(2, Types.INTEGER);
            }
            
            if (maintenanceTypeID > 0) {
                pStmt.setInt(3, maintenanceTypeID);
            } else {
                pStmt.setNull(3, Types.INTEGER);
            }
            
            pStmt.setTimestamp(4, startingDate);
            
            if (completionTime != null) {
                pStmt.setTimestamp(5, completionTime);
                
                // If completion time is set, update bus status to Available
                PreparedStatement updateBusStmt = conn.prepareStatement(
                    "UPDATE Bus SET status = 'Available' WHERE bus_id = ?");
                updateBusStmt.setInt(1, busID);
                updateBusStmt.executeUpdate();
                updateBusStmt.close();
            } else {
                pStmt.setNull(5, Types.TIMESTAMP);
            }
            
            pStmt.setInt(6, maintenanceID);
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
            
            // Get bus ID before deleting maintenance record
            PreparedStatement getBusStmt = conn.prepareStatement(
                "SELECT bus_id FROM Maintenance WHERE maintenance_id = ?");
            getBusStmt.setInt(1, maintenanceID);
            ResultSet rs = getBusStmt.executeQuery();
            
            int busIDToUpdate = 0;
            if (rs.next()) {
                busIDToUpdate = rs.getInt("bus_id");
            }
            
            rs.close();
            getBusStmt.close();
            
            // Delete maintenance record
            PreparedStatement pStmt = conn.prepareStatement(
                "DELETE FROM Maintenance WHERE maintenance_id = ?");
            pStmt.setInt(1, maintenanceID);
            pStmt.executeUpdate();
            pStmt.close();
            
            // Check if bus has any other active maintenance
            PreparedStatement checkMaintenanceStmt = conn.prepareStatement(
                "SELECT COUNT(*) as count FROM Maintenance " + 
                "WHERE bus_id = ? AND completion_time IS NULL");
            checkMaintenanceStmt.setInt(1, busIDToUpdate);
            ResultSet countRs = checkMaintenanceStmt.executeQuery();
            
            boolean hasActiveMaintenances = false;
            if (countRs.next()) {
                hasActiveMaintenances = countRs.getInt("count") > 0;
            }
            
            countRs.close();
            checkMaintenanceStmt.close();
            
            // If no active maintenances, update bus status to Available
            if (!hasActiveMaintenances && busIDToUpdate > 0) {
                PreparedStatement updateBusStmt = conn.prepareStatement(
                    "UPDATE Bus SET status = 'Available' WHERE bus_id = ?");
                updateBusStmt.setInt(1, busIDToUpdate);
                updateBusStmt.executeUpdate();
                updateBusStmt.close();
            }
            
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
            assignedMechanic = 0;
            maintenanceTypeID = 0;
            startingDate = null;
            completionTime = null;
            
            while (rs.next()) {
                maintenanceID = rs.getInt("maintenance_id");
                busID = rs.getInt("bus_id");
                assignedMechanic = rs.getInt("assigned_mechanic");
                maintenanceTypeID = rs.getInt("maintenance_type_id");
                startingDate = rs.getTimestamp("starting_date");
                completionTime = rs.getTimestamp("completion_time");
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
    
    // Get all maintenance records
    public static List<Maintenance> getAllMaintenanceRecords() {
        List<Maintenance> maintenanceRecords = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Maintenance ORDER BY starting_date DESC");
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Maintenance m = new Maintenance();
                m.maintenanceID = rs.getInt("maintenance_id");
                m.busID = rs.getInt("bus_id");
                m.assignedMechanic = rs.getInt("assigned_mechanic");
                m.maintenanceTypeID = rs.getInt("maintenance_type_id");
                m.startingDate = rs.getTimestamp("starting_date");
                m.completionTime = rs.getTimestamp("completion_time");
                maintenanceRecords.add(m);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return maintenanceRecords;
    }
    
    // Get active maintenance records (where completion_time is NULL)
    public static List<Maintenance> getActiveMaintenanceRecords() {
        List<Maintenance> maintenanceRecords = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Maintenance WHERE completion_time IS NULL " +
                "ORDER BY starting_date");
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Maintenance m = new Maintenance();
                m.maintenanceID = rs.getInt("maintenance_id");
                m.busID = rs.getInt("bus_id");
                m.assignedMechanic = rs.getInt("assigned_mechanic");
                m.maintenanceTypeID = rs.getInt("maintenance_type_id");
                m.startingDate = rs.getTimestamp("starting_date");
                m.completionTime = rs.getTimestamp("completion_time");
                maintenanceRecords.add(m);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return maintenanceRecords;
    }
    
    // Complete maintenance and update bus status
    public int completeMaintenance() {
        try {
            Connection conn = DBConnection.getConnection();
            
            // Set completion time
            completionTime = new Timestamp(System.currentTimeMillis());
            
            PreparedStatement pStmt = conn.prepareStatement(
                "UPDATE Maintenance SET completion_time = ? " +
                "WHERE maintenance_id = ?");
            pStmt.setTimestamp(1, completionTime);
            pStmt.setInt(2, maintenanceID);
            pStmt.executeUpdate();
            pStmt.close();
            
            // Check if bus has any other active maintenance
            PreparedStatement checkMaintenanceStmt = conn.prepareStatement(
                "SELECT COUNT(*) as count FROM Maintenance " + 
                "WHERE bus_id = ? AND completion_time IS NULL AND maintenance_id != ?");
            checkMaintenanceStmt.setInt(1, busID);
            checkMaintenanceStmt.setInt(2, maintenanceID);
            ResultSet countRs = checkMaintenanceStmt.executeQuery();
            
            boolean hasActiveMaintenances = false;
            if (countRs.next()) {
                hasActiveMaintenances = countRs.getInt("count") > 0;
            }
            
            countRs.close();
            checkMaintenanceStmt.close();
            
            // If no active maintenances, update bus status to Available
            if (!hasActiveMaintenances) {
                PreparedStatement updateBusStmt = conn.prepareStatement(
                    "UPDATE Bus SET status = 'Available' WHERE bus_id = ?");
                updateBusStmt.setInt(1, busID);
                updateBusStmt.executeUpdate();
                updateBusStmt.close();
            }
            
            conn.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
    // Get maintenance details with related information
    public static Map<String, Object> getMaintenanceDetails(int maintenanceID) {
        Map<String, Object> details = new HashMap<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT m.*, b.bus_number, s.staff_name, mt.type_name, mt.maintenance_cost " +
                "FROM Maintenance m " +
                "JOIN Bus b ON m.bus_id = b.bus_id " +
                "LEFT JOIN Staff s ON m.assigned_mechanic = s.staff_id " +
                "LEFT JOIN Maintenance_Type mt ON m.maintenance_type_id = mt.maintenance_type_id " +
                "WHERE m.maintenance_id = ?");
            pStmt.setInt(1, maintenanceID);
            ResultSet rs = pStmt.executeQuery();
            
            if (rs.next()) {
                details.put("maintenance_id", rs.getInt("maintenance_id"));
                details.put("bus_id", rs.getInt("bus_id"));
                details.put("bus_number", rs.getString("bus_number"));
                details.put("assigned_mechanic", rs.getInt("assigned_mechanic"));
                details.put("mechanic_name", rs.getString("staff_name"));
                details.put("maintenance_type_id", rs.getInt("maintenance_type_id"));
                details.put("type_name", rs.getString("type_name"));
                details.put("maintenance_cost", rs.getDouble("maintenance_cost"));
                details.put("starting_date", rs.getTimestamp("starting_date"));
                details.put("completion_time", rs.getTimestamp("completion_time"));
                
                // Calculate duration in hours if maintenance is complete
                if (rs.getTimestamp("completion_time") != null) {
                    long durationMillis = rs.getTimestamp("completion_time").getTime() - 
                                         rs.getTimestamp("starting_date").getTime();
                    double durationHours = durationMillis / (1000.0 * 60 * 60);
                    details.put("duration_hours", durationHours);
                }
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return details;
    }
}

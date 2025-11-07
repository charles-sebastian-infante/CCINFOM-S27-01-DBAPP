package com.busterminal.model;
import java.sql.*;
import java.util.*;
import com.busterminal.utils.DBConnection;

public class Staff {
    public int staffID;
    public String staffName;
    public int roleID;
    public int assignedTerminal;
    public int assignedBus;
    public String shift; // ENUM('Morning', 'Evening')
    public String contact;
    
    public Staff() {
        staffID = 0;
        staffName = "";
        roleID = 0;
        assignedTerminal = 0;
        assignedBus = 0;
        shift = "Morning"; // Default
        contact = "";
    }
    
    public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Staff (staff_name, role_id, assigned_terminal, " +
                "assigned_bus, shift, contact) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            );
            pStmt.setString(1, staffName);
            pStmt.setInt(2, roleID);
            
            // Handle nullable foreign keys
            if (assignedTerminal > 0) {
                pStmt.setInt(3, assignedTerminal);
            } else {
                pStmt.setNull(3, Types.INTEGER);
            }
            
            if (assignedBus > 0) {
                pStmt.setInt(4, assignedBus);
            } else {
                pStmt.setNull(4, Types.INTEGER);
            }
            
            pStmt.setString(5, shift);
            pStmt.setString(6, contact);
            
            int affectedRows = pStmt.executeUpdate();
            
            if (affectedRows == 0) {
                return 0;
            }
            
            try (ResultSet generatedKeys = pStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    staffID = generatedKeys.getInt(1);
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
                "UPDATE Staff SET staff_name = ?, role_id = ?, assigned_terminal = ?, " +
                "assigned_bus = ?, shift = ?, contact = ? WHERE staff_id = ?"
            );
            pStmt.setString(1, staffName);
            
            if (roleID > 0) {
                pStmt.setInt(2, roleID);
            } else {
                pStmt.setNull(2, Types.INTEGER);
            }
            
            if (assignedTerminal > 0) {
                pStmt.setInt(3, assignedTerminal);
            } else {
                pStmt.setNull(3, Types.INTEGER);
            }
            
            if (assignedBus > 0) {
                pStmt.setInt(4, assignedBus);
            } else {
                pStmt.setNull(4, Types.INTEGER);
            }
            
            pStmt.setString(5, shift);
            pStmt.setString(6, contact);
            pStmt.setInt(7, staffID);
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
                "DELETE FROM Staff WHERE staff_id = ?");
            pStmt.setInt(1, staffID);
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
                "SELECT * FROM Staff WHERE staff_id = ?");
            pStmt.setInt(1, staffID);
            ResultSet rs = pStmt.executeQuery();
            
            staffID = 0;
            staffName = "";
            roleID = 0;
            assignedTerminal = 0;
            assignedBus = 0;
            shift = "";
            contact = "";
            
            while (rs.next()) {
                staffID = rs.getInt("staff_id");
                staffName = rs.getString("staff_name");
                roleID = rs.getInt("role_id");
                assignedTerminal = rs.getInt("assigned_terminal");
                assignedBus = rs.getInt("assigned_bus");
                shift = rs.getString("shift");
                contact = rs.getString("contact");
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
    
    // Get all staff method
    public static List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Staff");
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Staff s = new Staff();
                s.staffID = rs.getInt("staff_id");
                s.staffName = rs.getString("staff_name");
                s.roleID = rs.getInt("role_id");
                s.assignedTerminal = rs.getInt("assigned_terminal");
                s.assignedBus = rs.getInt("assigned_bus");
                s.shift = rs.getString("shift");
                s.contact = rs.getString("contact");
                staffList.add(s);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return staffList;
    }
    
    // Get staff by role method
    public static List<Staff> getStaffByRole(int roleID) {
        List<Staff> staffList = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT s.*, r.role_name FROM Staff s " +
                "JOIN Role r ON s.role_id = r.role_id " +
                "WHERE s.role_id = ?");
            pStmt.setInt(1, roleID);
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Staff s = new Staff();
                s.staffID = rs.getInt("staff_id");
                s.staffName = rs.getString("staff_name");
                s.roleID = rs.getInt("role_id");
                s.assignedTerminal = rs.getInt("assigned_terminal");
                s.assignedBus = rs.getInt("assigned_bus");
                s.shift = rs.getString("shift");
                s.contact = rs.getString("contact");
                staffList.add(s);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return staffList;
    }
    
    // Get staff details including role name, terminal name, and bus number
    public static Map<String, Object> getStaffDetails(int staffID) {
        Map<String, Object> staffDetails = new HashMap<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT s.*, r.role_name, t.terminal_name, b.bus_number " +
                "FROM Staff s " +
                "LEFT JOIN Role r ON s.role_id = r.role_id " +
                "LEFT JOIN Terminal t ON s.assigned_terminal = t.terminal_id " +
                "LEFT JOIN Bus b ON s.assigned_bus = b.bus_id " +
                "WHERE s.staff_id = ?");
            pStmt.setInt(1, staffID);
            ResultSet rs = pStmt.executeQuery();
            
            if (rs.next()) {
                staffDetails.put("staff_id", rs.getInt("staff_id"));
                staffDetails.put("staff_name", rs.getString("staff_name"));
                staffDetails.put("role_id", rs.getInt("role_id"));
                staffDetails.put("role_name", rs.getString("role_name"));
                staffDetails.put("assigned_terminal", rs.getInt("assigned_terminal"));
                staffDetails.put("terminal_name", rs.getString("terminal_name"));
                staffDetails.put("assigned_bus", rs.getInt("assigned_bus"));
                staffDetails.put("bus_number", rs.getString("bus_number"));
                staffDetails.put("shift", rs.getString("shift"));
                staffDetails.put("contact", rs.getString("contact"));
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return staffDetails;
    }
}

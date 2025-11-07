package com.busterminal.model;
import java.sql.*;
import java.util.*;
import com.busterminal.utils.DBConnection;

public class Role {
    public int roleID;
    public String roleName;
    
    public Role() {
        roleID = 0;
        roleName = "";
    }
    
    public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Role (role_name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS
            );
            pStmt.setString(1, roleName);
            
            int affectedRows = pStmt.executeUpdate();
            
            if (affectedRows == 0) {
                return 0;
            }
            
            try (ResultSet generatedKeys = pStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    roleID = generatedKeys.getInt(1);
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
                "UPDATE Role SET role_name = ? WHERE role_id = ?"
            );
            pStmt.setString(1, roleName);
            pStmt.setInt(2, roleID);
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
                "DELETE FROM Role WHERE role_id = ?");
            pStmt.setInt(1, roleID);
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
                "SELECT * FROM Role WHERE role_id = ?");
            pStmt.setInt(1, roleID);
            ResultSet rs = pStmt.executeQuery();
            
            roleID = 0;
            roleName = "";
            
            while (rs.next()) {
                roleID = rs.getInt("role_id");
                roleName = rs.getString("role_name");
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
    
    // Get all roles method
    public static List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Role");
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Role r = new Role();
                r.roleID = rs.getInt("role_id");
                r.roleName = rs.getString("role_name");
                roles.add(r);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return roles;
    }
}

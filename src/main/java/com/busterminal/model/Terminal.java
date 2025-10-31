package com.busterminal.model;

import java.sql.*;
import com.busterminal.utils.DBConnection;

public class Terminal {
    public int terminalID;
    public String terminalName;
    public String location;
    public String city;
    public String phone;

    public Terminal() {
        terminalID = 0;
        terminalName = "";
        location = "";
        city = "";
        phone = "";
    }

    public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Terminal (terminal_name, location, city, phone)" +
                " VALUES (?,?,?,?)");
            pStmt.setString(1, terminalName);
            pStmt.setString(2, location);
            pStmt.setString(3, city);
            pStmt.setString(4, phone);
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
                "UPDATE Terminal SET terminal_name = ?,  location = ?, " + 
                "city = ?, phone = ? WHERE terminal_id = ?");
            pStmt.setString(1, terminalName);
            pStmt.setString(2, location);
            pStmt.setString(3, city);
            pStmt.setString(4, phone);
            pStmt.setInt(5, terminalID);
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
                "DELETE FROM Terminal WHERE terminal_id = ?");
            pStmt.setInt(1, terminalID);
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
                "SELECT * FROM Terminal WHERE terminal_id = ?");
            pStmt.setInt(1, terminalID);
            ResultSet rs = pStmt.executeQuery();

            terminalID = 0;
            terminalName = "";
            location = "";
            city = "";
            phone = "";

            while (rs.next()) {
                terminalID = rs.getInt("terminal_id");
                terminalName = rs.getString("terminal_name");
                location = rs.getString("location");
                city = rs.getString("city");
                phone = rs.getString("phone");
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
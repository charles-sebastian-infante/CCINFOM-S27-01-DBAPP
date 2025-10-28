package com.busterminal.model;

import java.sql.*;
import com.busterminal.utils.DBConnection;

public class Schedule {
    public int scheduleID;
    public int busID;
    public String departureTime;
    public String arrivalTime;
    public String status;
    
   public Schedule() {
        scheduleID = 0;
        busID = 0;
        departureTime = "";
        arrivalTime = "";
        status = "Scheduled";
    }
    
   public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Schedule (bus_id, departure_time, arrival_time," +
                " status) VALUES (?,?,?,?)");
            pStmt.setInt(1, busID);
            pStmt.setString(2, departureTime);
            pStmt.setString(3, arrivalTime);
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
                "UPDATE Schedule SET bus_id = ?, departure_time = ?, " +
                "arrival_time = ?, status = ? WHERE schedule_id = ?");
            pStmt.setInt(1, busID);
            pStmt.setString(2, departureTime);
            pStmt.setString(3, arrivalTime);
            pStmt.setString(4, status);
            pStmt.setInt(5, scheduleID);
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
                "DELETE FROM Schedule WHERE schedule_id = ?");
            pStmt.setInt(1, scheduleID);
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
                "SELECT * FROM Schedule WHERE schedule_id = ?");
            pStmt.setInt(1, scheduleID);
            ResultSet rs = pStmt.executeQuery();
            
            scheduleID = 0;
            busID = 0;
            departureTime = "";
            arrivalTime = "";
            status = "";
            
            while (rs.next()) {
                scheduleID = rs.getInt("schedule_id");
                busID = rs.getInt("bus_id");
                departureTime = rs.getString("departure_time");
                arrivalTime = rs.getString("arrival_time");
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

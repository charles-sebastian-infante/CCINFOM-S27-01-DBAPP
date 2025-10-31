package com.busterminal.model;

import java.sql.*;
import com.busterminal.utils.DBConnection;

public class Ticket {
    public int ticketID;
    public String ticketNumber;
    public int busID;
    public int scheduleID;
    public String departureDate;
    public String type;
    public double discount;
    public double finalAmount;
    public int routeID;
    public int staffID;

    public Ticket() {
        ticketID = 0;
        ticketNumber = "";
        busID = 0;
        scheduleID = 0;
        departureDate = "";
        type = "Regular";
        discount = 20.00;
        finalAmount = 0.0;
        routeID = 0;
        staffID = 0;
    }

    public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Ticket (ticket_number, bus_id, schedule_id" +
                ", departure_date, type, discount, final_amount, route_id" +
                ", staff_id) VALUES (?,?,?,?,?,?,?,?,?)");
            pStmt.setString(1, ticketNumber);
            pStmt.setInt(2, busID);
            pStmt.setInt(3, scheduleID);
            pStmt.setString(4, departureDate);
            pStmt.setString(5, type);
            pStmt.setDouble(6, discount);
            pStmt.setDouble(7, finalAmount);
            pStmt.setInt(8, routeID);
            pStmt.setInt(9, staffID);
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
                "UPDATE Ticket SET ticket_number = ?, bus_id = ?," + 
                " schedule_id = ?, departure_date = ?, type = ?," +
                " discount = ?, final_amount = ?, route_id = ?," +
                " staff_id = ? WHERE ticket_id = ?");
            pStmt.setString(1, ticketNumber);
            pStmt.setInt(2, busID);
            pStmt.setInt(3, scheduleID);
            pStmt.setString(4, departureDate);
            pStmt.setString(5, type);
            pStmt.setDouble(6, discount);
            pStmt.setDouble(7, finalAmount);
            pStmt.setInt(8, routeID);
            pStmt.setInt(9, staffID);
            pStmt.setInt(10, ticketID);
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
                "DELETE FROM Ticket WHERE ticket_id = ?");
            pStmt.setInt(1, ticketID);
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
                "SELECT * FROM Ticket WHERE ticket_id = ?");
            pStmt.setInt(1, ticketID);
            ResultSet rs = pStmt.executeQuery();

            ticketID = 0;
            ticketNumber = "";
            busID = 0;
            scheduleID = 0;
            departureDate = "";
            type = "";
            discount = 0.0;
            finalAmount = 0.0;
            routeID = 0;
            staffID = 0;

            while (rs.next()) {
                ticketID = rs.getInt("ticket_id");
                ticketNumber = rs.getString("ticket_number");
                busID = rs.getInt("bus_id");
                scheduleID = rs.getInt("schedule_id");
                departureDate = rs.getString("departure_date");
                type = rs.getString("type");
                discount = rs.getDouble("discount");
                finalAmount = rs.getDouble("final_amount");
                routeID = rs.getInt("route_id");
                staffID = rs.getInt("staff_id");
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
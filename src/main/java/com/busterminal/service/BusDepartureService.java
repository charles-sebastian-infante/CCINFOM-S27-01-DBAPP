package com.busterminal.service;

import java.sql.*;
import java.util.*;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;

public class BusDepartureService {
    public Bus departingBus = new Bus();
    public Schedule departureSchedule = new Schedule();
    public Route busRoute = new Route();
    public ArrayList<Ticket> passengerManifest = new ArrayList<>();

    public BusDepartureService() {
        passengerManifest.clear();
    }

    public int loadRoute() {
        return busRoute.getRecord();
    }

    public int loadPassengerManifest(int scheduleID, int busID, 
        String departureDate) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Ticket " +
                "WHERE schedule_id = ? " +
                "AND bus_id = ? " +
                "AND DATE(departure_date) = ?");
            pStmt.setInt(1, scheduleID);
            pStmt.setInt(2, busID);
            pStmt.setString(3, departureDate);
            ResultSet rs = pStmt.executeQuery();
            passengerManifest.clear();

            while (rs.next()) {
                Ticket t = new Ticket();
                t.ticketID = rs.getInt("ticket_id");
                t.ticketNumber = rs.getString("ticket_number");
                t.busID = rs.getInt("bus_id");
                t.scheduleID = rs.getInt("schedule_id");
                t.departureDate = rs.getString("departure_date");
                t.type = rs.getString("type");
                t.finalAmount = rs.getDouble("final_amount");
                passengerManifest.add(t);
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

    public int updateScheduleStatus() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "UPDATE Schedule SET status = 'Departed' " +
                "WHERE schedule_id = ?");
            pStmt.setInt(1, departureSchedule.scheduleID);
            pStmt.executeUpdate();
            pStmt.close();
            conn.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public int updateBusStatus() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "UPDATE Bus SET status = 'In Transit' WHERE bus_id = ?");
            pStmt.setInt(1, departingBus.busID);
            pStmt.executeUpdate();
            pStmt.close();
            conn.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public int confirmDeparture() {
        if (updateScheduleStatus() == 1 && updateBusStatus() == 1) 
            return 1;

        return 0;
    }
}

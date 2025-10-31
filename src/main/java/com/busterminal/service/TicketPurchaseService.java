package com.busterminal.service;

import java.sql.*;
import java.util.*;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;

public class TicketPurchaseService {
    public ArrayList<Schedule> availableSchedules = new ArrayList<>();
    public Route selectedRoute = new Route();
    public Ticket newTicket = new Ticket();
    public Staff conductor = new Staff();
    
    public TicketPurchaseService() {
        availableSchedules.clear();
    }
    
   public int loadAvailableSchedules(int routeID, String travelDate) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT s.* FROM Schedule s " +
                "JOIN Bus b ON s.bus_id = b.bus_id " +
                "WHERE b.route_id = ? " +
                "AND DATE(s.departure_time) = ? " +
                "AND s.status = 'Scheduled'"
            );
            pStmt.setInt(1, routeID);
            pStmt.setString(2, travelDate);
            ResultSet rs = pStmt.executeQuery();            
            availableSchedules.clear();

            while (rs.next()) {
                Schedule sch = new Schedule();
                sch.scheduleID = rs.getInt("schedule_id");
                sch.busID = rs.getInt("bus_id");
                sch.departureTime = rs.getString("departure_time");
                sch.arrivalTime = rs.getString("arrival_time");
                sch.status = rs.getString("status");
                availableSchedules.add(sch);
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
    
   public int checkAvailability(int scheduleID) {
        try {
            Connection conn = DBConnection.getConnection(); 

            PreparedStatement pStmt1 = conn.prepareStatement(
                "SELECT b.capacity FROM Bus b " +
                "JOIN Schedule s ON b.bus_id = s.bus_id " +
                "WHERE s.schedule_id = ?"
            );
            pStmt1.setInt(1, scheduleID);
            ResultSet rs1 = pStmt1.executeQuery();
            int capacity = 0;

            if (rs1.next()) 
                capacity = rs1.getInt("capacity");

            rs1.close();
            pStmt1.close();
            
            PreparedStatement pStmt2 = conn.prepareStatement(
                "SELECT COUNT(*) as sold FROM Ticket WHERE schedule_id = ?"
            );

            pStmt2.setInt(1, scheduleID);
            ResultSet rs2 = pStmt2.executeQuery();
            int sold = 0;

            if (rs2.next()) 
                sold = rs2.getInt("sold");

            rs2.close();
            pStmt2.close();
            conn.close();
            
            return capacity - sold;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }
    
   public double calculatePrice(double baseFare, String ticketType, 
        double discountPercent) {
        if (ticketType.equals("Free")) 
            return 0.0;
        else if (ticketType.equals("Discounted")) 
            return baseFare * (1 - discountPercent / 100);
        else 
            return baseFare;
    }
    
    public String generateTicketNumber() {
        java.text.SimpleDateFormat sdf = new java.text
            .SimpleDateFormat("yyyyMMdd-HHmmss");
        return "TCK-" + sdf.format(new java.util.Date()) + "-" + 
                (int)(Math.random() * 10000);
    }
    
   public int confirmPurchase() {
        newTicket.ticketNumber = generateTicketNumber();
        return newTicket.addRecord();
    }
}

package com.busterminal.service;

import java.sql.*;
import java.util.*;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;

public class BusMaintenanceAssignment {
    public Bus selectedBus = new Bus();
    public Route assignedRoute = new Route();
    public Maintenance maintenanceRecord = new Maintenance();
    public ArrayList<Bus> availableBuses = new ArrayList<>();
    
    public BusMaintenanceAssignment() {
        availableBuses.clear();
    }
    
   public int loadRoute(int routeID) {
        assignedRoute.routeId = routeID;
        return assignedRoute.getRecord();
    }
    
   public int findAvailableBuses(int requiredCapacity) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Bus " +
                "WHERE status = 'Available' " +
                "AND capacity >= ? " +
                "ORDER BY capacity");
            pStmt.setInt(1, requiredCapacity);
            ResultSet rs = pStmt.executeQuery();            
            availableBuses.clear();

            while (rs.next()) {
                Bus bus = new Bus();
                bus.busID = rs.getInt("bus_id");
                bus.busNumber = rs.getString("bus_number");
                bus.capacity = rs.getInt("capacity");
                bus.status = rs.getString("status");
                bus.currentTerminal = rs.getInt("current_terminal");
                bus.routeID = rs.getInt("route_id");
                availableBuses.add(bus);
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
    
   public boolean checkScheduleConflicts(int busID, String departureTime) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT COUNT(*) as conflicts FROM Schedule " +
                "WHERE bus_id = ? " +
                "AND departure_time = ? " +
                "AND status IN ('Scheduled', 'Departed')");
            pStmt.setInt(1, busID);
            pStmt.setString(2, departureTime);
            ResultSet rs = pStmt.executeQuery();
            
            int conflicts = 0;
            if (rs.next()) 
                conflicts = rs.getInt("conflicts");

            rs.close();
            pStmt.close();
            conn.close();
            
            return conflicts == 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
   public int assignRoute() {
        selectedBus.routeID = assignedRoute.routeId;
        selectedBus.status = "Scheduled";
        return selectedBus.modRecord();
    }
    
   public int createSchedule(String departureTime, String arrivalTime) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Schedule (bus_id, departure_time, arrival_time," +
                " status) VALUES (?,?,?,'Scheduled')"
            );
            pStmt.setInt(1, selectedBus.busID);
            pStmt.setString(2, departureTime);
            pStmt.setString(3, arrivalTime);
            pStmt.executeUpdate();
            pStmt.close();
            conn.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
   public int scheduleMaintenance() {
        maintenanceRecord.busID = selectedBus.busID;
        selectedBus.status = "Maintenance";
        
        if (maintenanceRecord.addRecord() == 1 && selectedBus.modRecord() == 1) 
            return 1;

        return 0;
    }
}

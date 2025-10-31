package com.busterminal.model;

import java.sql.*;
import com.busterminal.utils.DBConnection;

public class Route {
    public int routeID;
    public String routeName;
    public int originID;
    public int destinationID;
    public double distance;
    public String travelTime;
    public double baseFare;

    public Route() {
        routeID = 0;
        routeName = "";
        originID = 0;
        destinationID = 0;
        distance = 0.0;
        travelTime = "";
        baseFare = 0.0;
    }

    public int addRecord() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Route (route_name, origin_id, destination_id," +
                " distance, travel_time, base_fare) VALUES (?,?,?,?,?,?)")) {
            pStmt.setString(1, routeName);
            pStmt.setInt(2, originID);
            pStmt.setInt(3, destinationID);
            pStmt.setDouble(4, distance);
            pStmt.setString(5, travelTime);
            pStmt.setDouble(6, baseFare);
            pStmt.executeUpdate();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public int modRecord() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(
                "UPDATE Route SET route_name = ?, origin_id = ?," +
                " destination_id = ?, distance = ?, travel_time = ?," +
                " base_fare = ? WHERE route_id = ?")) {
            pStmt.setString(1, routeName);
            pStmt.setInt(2, originID);
            pStmt.setInt(3, destinationID);
            pStmt.setDouble(4, distance);
            pStmt.setString(5, travelTime);
            pStmt.setDouble(6, baseFare);
            pStmt.setInt(7, routeID);
            pStmt.executeUpdate();
            return 1; 
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public int delRecord() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(
                "DELETE FROM Route WHERE route_id = ?")) {
            pStmt.setInt(1, routeID);
            pStmt.executeUpdate();
            return 1;  
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }

    }

    public int getRecord() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Route WHERE route_id = ?")) {
            pStmt.setInt(1, routeID);
            ResultSet rs = pStmt.executeQuery();
            
            routeID = 0;
            routeName = "";
            originID = 0;
            destinationID = 0;
            distance = 0.0;
            travelTime = "";
            baseFare = 0.0;

            while (rs.next()) {
                routeID = rs.getInt("route_id");
                routeName = rs.getString("route_name");
                originID = rs.getInt("origin_id");
                destinationID = rs.getInt("destination_id");
                distance = rs.getDouble("distance");
                travelTime = rs.getString("travel_time");
                baseFare = rs.getDouble("base_fare");
            }
            rs.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
}

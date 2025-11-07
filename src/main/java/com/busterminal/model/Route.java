package com.busterminal.model;
import java.sql.*;
import java.util.*;
import com.busterminal.utils.DBConnection;

public class Route {
    public int routeID;
    public String routeName;
    public int originID;
    public int destinationID;
    public double distance;
    public Time travelTime;
    public double baseFare;
    
    public Route() {
        routeID = 0;
        routeName = "";
        originID = 0;
        destinationID = 0;
        distance = 0.0;
        travelTime = null;
        baseFare = 0.0;
    }
    
    public int addRecord() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Route (route_name, origin_id, destination_id, " +
                "distance, travel_time, base_fare) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            );
            pStmt.setString(1, routeName);
            pStmt.setInt(2, originID);
            pStmt.setInt(3, destinationID);
            pStmt.setDouble(4, distance);
            pStmt.setTime(5, travelTime);
            pStmt.setDouble(6, baseFare);
            
            int affectedRows = pStmt.executeUpdate();
            
            if (affectedRows == 0) {
                return 0;
            }
            
            try (ResultSet generatedKeys = pStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    routeID = generatedKeys.getInt(1);
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
                "UPDATE Route SET route_name = ?, origin_id = ?, destination_id = ?, " +
                "distance = ?, travel_time = ?, base_fare = ? WHERE route_id = ?"
            );
            pStmt.setString(1, routeName);
            pStmt.setInt(2, originID);
            pStmt.setInt(3, destinationID);
            pStmt.setDouble(4, distance);
            pStmt.setTime(5, travelTime);
            pStmt.setDouble(6, baseFare);
            pStmt.setInt(7, routeID);
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
                "DELETE FROM Route WHERE route_id = ?");
            pStmt.setInt(1, routeID);
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
                "SELECT * FROM Route WHERE route_id = ?");
            pStmt.setInt(1, routeID);
            ResultSet rs = pStmt.executeQuery();
            
            routeID = 0;
            routeName = "";
            originID = 0;
            destinationID = 0;
            distance = 0.0;
            travelTime = null;
            baseFare = 0.0;
            
            while (rs.next()) {
                routeID = rs.getInt("route_id");
                routeName = rs.getString("route_name");
                originID = rs.getInt("origin_id");
                destinationID = rs.getInt("destination_id");
                distance = rs.getDouble("distance");
                travelTime = rs.getTime("travel_time");
                baseFare = rs.getDouble("base_fare");
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
    
    // Get all routes method
    public static List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Route");
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Route r = new Route();
                r.routeID = rs.getInt("route_id");
                r.routeName = rs.getString("route_name");
                r.originID = rs.getInt("origin_id");
                r.destinationID = rs.getInt("destination_id");
                r.distance = rs.getDouble("distance");
                r.travelTime = rs.getTime("travel_time");
                r.baseFare = rs.getDouble("base_fare");
                routes.add(r);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return routes;
    }
    
    // Get route details including terminal names
    public static Map<String, Object> getRouteDetails(int routeID) {
        Map<String, Object> routeDetails = new HashMap<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT r.*, t1.terminal_name as origin_name, t2.terminal_name as destination_name " +
                "FROM Route r " +
                "JOIN Terminal t1 ON r.origin_id = t1.terminal_id " +
                "JOIN Terminal t2 ON r.destination_id = t2.terminal_id " +
                "WHERE r.route_id = ?");
            pStmt.setInt(1, routeID);
            ResultSet rs = pStmt.executeQuery();
            
            if (rs.next()) {
                routeDetails.put("route_id", rs.getInt("route_id"));
                routeDetails.put("route_name", rs.getString("route_name"));
                routeDetails.put("origin_id", rs.getInt("origin_id"));
                routeDetails.put("destination_id", rs.getInt("destination_id"));
                routeDetails.put("origin_name", rs.getString("origin_name"));
                routeDetails.put("destination_name", rs.getString("destination_name"));
                routeDetails.put("distance", rs.getDouble("distance"));
                routeDetails.put("travel_time", rs.getTime("travel_time"));
                routeDetails.put("base_fare", rs.getDouble("base_fare"));
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return routeDetails;
    }

    // Add this method to Route.java after the existing methods

    // Get all routes operating from a specific terminal
    public static List<Route> getRoutesByOriginTerminal(int terminalID) {
        List<Route> routes = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Route WHERE origin_id = ? ORDER BY route_name");
            pStmt.setInt(1, terminalID);
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Route r = new Route();
                r.routeID = rs.getInt("route_id");
                r.routeName = rs.getString("route_name");
                r.originID = rs.getInt("origin_id");
                r.destinationID = rs.getInt("destination_id");
                r.distance = rs.getDouble("distance");
                r.travelTime = rs.getTime("travel_time");
                r.baseFare = rs.getDouble("base_fare");
                routes.add(r);
            }
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return routes;
    }
}

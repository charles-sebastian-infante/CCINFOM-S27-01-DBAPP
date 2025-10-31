package com.busterminal.reports;

import java.sql.*;
import java.util.*;

import com.busterminal.utils.DBConnection;

public class RoutePerformanceReport {
    public class RoutePerformance {
        public int routeID;
        public String routeName;
        public String originCity;
        public String destinationCity;
        public int passengerCount;
        public double totalAmount;
        public double baseFare;
    }
    
    public ArrayList<RoutePerformance> performanceList = new ArrayList<>();
    public int reportYear;
    public int reportMonth;
    
    public RoutePerformanceReport() {
        performanceList.clear();
        reportYear = 0;
        reportMonth = 0;
    }
    
   public int generateReport() {
        try {
            Connection conn = DBConnection.getConnection();            
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT r.route_id, r.route_name, r.base_fare, " +
                "       t1.city as origin_city, t2.city as destination_city, " +
                "       COUNT(tk.ticket_id) as passenger_count, " +
                "       SUM(tk.final_amount) as total_amount " +
                "FROM Route r " +
                "JOIN Terminal t1 ON r.origin_id = t1.terminal_id " +
                "JOIN Terminal t2 ON r.destination_id = t2.terminal_id " +
                "JOIN Ticket tk ON r.route_id = tk.route_id " +
                "WHERE YEAR(tk.departure_date) = ? " +
                "AND MONTH(tk.departure_date) = ? " +
                "AND tk.type != 'Cancelled' " +
                "GROUP BY r.route_id, r.route_name, r.base_fare, t1.city, t2.city " +
                "ORDER BY passenger_count DESC"
            );
            pStmt.setInt(1, reportYear);
            pStmt.setInt(2, reportMonth);
            ResultSet rs = pStmt.executeQuery(); 
            performanceList.clear();

            while (rs.next()) {
                RoutePerformance rp = new RoutePerformance();
                rp.routeID = rs.getInt("route_id");
                rp.routeName = rs.getString("route_name");
                rp.originCity = rs.getString("origin_city");
                rp.destinationCity = rs.getString("destination_city");
                rp.passengerCount = rs.getInt("passenger_count");
                rp.totalAmount = rs.getDouble("total_amount");
                rp.baseFare = rs.getDouble("base_fare");
                performanceList.add(rp);
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

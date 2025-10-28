package com.busterminal.reports;

import java.sql.*;
import java.util.*;
import com.busterminal.utils.DBConnection;

public class BusUtilizationReport {
    public class BusUtilization {
        public int busID;
        public String busNumber;
        public int capacity;
        public int tripCount;
        public int totalPassengers;
        public double utilizationRate;
        public double totalRevenue;
    }
    
    public ArrayList<BusUtilization> utilizationList = new ArrayList<>();
    public int reportYear;
    public int reportMonth;
    
    public BusUtilizationReport() {
        utilizationList.clear();
        reportYear = 0;
        reportMonth = 0;
    }
    
   public int generateReport() {
        try {
            Connection conn = DBConnection.getConnection();            
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT b.bus_id, b.bus_number, b.capacity, " +
                "       COUNT(DISTINCT s.schedule_id) as trip_count, " +
                "       COUNT(tk.ticket_id) as total_passengers, " +
                "       SUM(tk.final_amount) as total_revenue, " +
                "       (COUNT(tk.ticket_id) / (COUNT(DISTINCT s.schedule_id) * b.capacity)) * 100 as utilization_rate " +
                "FROM Bus b " +
                "JOIN Schedule s ON b.bus_id = s.bus_id " +
                "LEFT JOIN Ticket tk ON s.schedule_id = tk.schedule_id " +
                "WHERE YEAR(s.departure_time) = ? " +
                "AND MONTH(s.departure_time) = ? " +
                "AND s.status IN ('Departed', 'Completed') " +
                "AND (tk.type IS NULL OR tk.type != 'Cancelled') " +
                "GROUP BY b.bus_id, b.bus_number, b.capacity " +
                "ORDER BY utilization_rate DESC"
            );

            pStmt.setInt(1, reportYear);
            pStmt.setInt(2, reportMonth);
            ResultSet rs = pStmt.executeQuery(); 
            utilizationList.clear();

            while (rs.next()) {
                BusUtilization bu = new BusUtilization();
                bu.busID = rs.getInt("bus_id");
                bu.busNumber = rs.getString("bus_number");
                bu.capacity = rs.getInt("capacity");
                bu.tripCount = rs.getInt("trip_count");
                bu.totalPassengers = rs.getInt("total_passengers");
                bu.totalRevenue = rs.getDouble("total_revenue");
                bu.utilizationRate = rs.getDouble("utilization_rate");
                utilizationList.add(bu);
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

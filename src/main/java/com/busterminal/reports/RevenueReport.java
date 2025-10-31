package com.busterminal.reports;

import java.sql.*; 
import java.util.*;
import com.busterminal.utils.DBConnection;

public class RevenueReport {
    public class TerminalRevenue {
        public int terminalID;
        public String terminalName;
        public String city;
        public double totalSales;
        public double averageSales;
        public int ticketCount;
    }
    
    public ArrayList<TerminalRevenue> revenueList = new ArrayList<>();
    public int reportYear;
    public int reportMonth;
    
    public RevenueReport() {
        revenueList.clear();
        reportYear = 0;
        reportMonth = 0;
    }
    
   public int generateReport() {
        try {
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT t.terminal_id, t.terminal_name, t.city, " +
                "       SUM(tk.final_amount) as total_sales, " +
                "       AVG(tk.final_amount) as avg_sales, " +
                "       COUNT(tk.ticket_id) as ticket_count " +
                "FROM Terminal t " +
                "JOIN Bus b ON t.terminal_id = b.current_terminal " +
                "JOIN Ticket tk ON b.bus_id = tk.bus_id " +
                "WHERE YEAR(tk.departure_date) = ? " +
                "AND MONTH(tk.departure_date) = ? " +
                "AND tk.type != 'Cancelled' " +
                "GROUP BY t.terminal_id, t.terminal_name, t.city " +
                "ORDER BY total_sales DESC");
            pStmt.setInt(1, reportYear);
            pStmt.setInt(2, reportMonth);
            ResultSet rs = pStmt.executeQuery(); 
            revenueList.clear();

            while (rs.next()) {
                TerminalRevenue tr = new TerminalRevenue();
                tr.terminalID = rs.getInt("terminal_id");
                tr.terminalName = rs.getString("terminal_name");
                tr.city = rs.getString("city");
                tr.totalSales = rs.getDouble("total_sales");
                tr.averageSales = rs.getDouble("avg_sales");
                tr.ticketCount = rs.getInt("ticket_count");
                revenueList.add(tr);
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

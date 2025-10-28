package com.busterminal.reports;

import java.sql.*;
import java.util.*;
import com.busterminal.utils.DBConnection;

public class DailyPaymentReport {
    public class DailyPayment {
        public String paymentDate;
        public int regularCount;
        public double regularTotal;
        public int discountedCount;
        public double discountedTotal;
        public int freeCount;
        public int totalTransactions;
        public double grandTotal;
    }
    
    public ArrayList<DailyPayment> paymentList = new ArrayList<>();
    public int reportYear;
    public int reportMonth;
    
    public DailyPaymentReport() {
        paymentList.clear();
        reportYear = 0;
        reportMonth = 0;
    }
    
   public int generateReport() {
        try {
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT DATE(departure_date) as payment_date, " +
                "       SUM(CASE WHEN type = 'Regular' THEN 1 ELSE 0 END) as regular_count, " +
                "       SUM(CASE WHEN type = 'Regular' THEN final_amount ELSE 0 END) as regular_total, " +
                "       SUM(CASE WHEN type = 'Discounted' THEN 1 ELSE 0 END) as discounted_count, " +
                "       SUM(CASE WHEN type = 'Discounted' THEN final_amount ELSE 0 END) as discounted_total, " +
                "       SUM(CASE WHEN type = 'Free' THEN 1 ELSE 0 END) as free_count, " +
                "       COUNT(*) as total_transactions, " +
                "       SUM(final_amount) as grand_total " +
                "FROM Ticket " +
                "WHERE YEAR(departure_date) = ? " +
                "AND MONTH(departure_date) = ? " +
                "AND type != 'Cancelled' " +
                "GROUP BY DATE(departure_date) " +
                "ORDER BY payment_date"
            );
            pStmt.setInt(1, reportYear);
            pStmt.setInt(2, reportMonth);
            ResultSet rs = pStmt.executeQuery(); 
            paymentList.clear();

            while (rs.next()) {
                DailyPayment dp = new DailyPayment();
                dp.paymentDate = rs.getString("payment_date");
                dp.regularCount = rs.getInt("regular_count");
                dp.regularTotal = rs.getDouble("regular_total");
                dp.discountedCount = rs.getInt("discounted_count");
                dp.discountedTotal = rs.getDouble("discounted_total");
                dp.freeCount = rs.getInt("free_count");
                dp.totalTransactions = rs.getInt("total_transactions");
                dp.grandTotal = rs.getDouble("grand_total");
                paymentList.add(dp);
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

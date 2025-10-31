package com.busterminal.service;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;

public class TicketCancellation {
    public Ticket originalTicket = new Ticket();
    public double refundAmount = 0.0;
    public boolean eligibleForCancellation = false;
    public String cancellationReason = "";
    
    public TicketCancellation() {
        refundAmount = 0.0;
        eligibleForCancellation = false;
    }
    
   public int loadTicket(int ticketID) {
        originalTicket.ticketID = ticketID;
        return originalTicket.getRecord();
    }
    
   public boolean checkEligibility() {
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement pStmt = conn.prepareStatement(
                "SELECT s.departure_time FROM Schedule s " +
                "WHERE s.schedule_id = ?")) {
            pStmt.setInt(1, originalTicket.scheduleID);
            ResultSet rs = pStmt.executeQuery();
            
            if (rs.next()) {
                String departureTimeStr = rs.getString("departure_time");
                DateTimeFormatter formatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime departureTime = LocalDateTime
                    .parse(departureTimeStr, formatter);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime cutoffTime = departureTime.minusHours(2);
                
                eligibleForCancellation = now.isBefore(cutoffTime);
                
                if (!eligibleForCancellation) {
                    cancellationReason = 
                    "Cannot cancel - less than 2 hours before departure";
                }
            }

            rs.close();
            return eligibleForCancellation;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
   public double calculateRefund() {
        if (originalTicket.type.equals("Free")) 
            refundAmount = 0.0;
        else 
            refundAmount = originalTicket.finalAmount * 0.90;

        return refundAmount;
    }
    
   public int processCancellation() {
        if (!eligibleForCancellation) 
            return 0;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(
                "UPDATE Ticket SET type = 'Cancelled', final_amount = ?" +
                " WHERE ticket_id = ?")) {
            pStmt.setDouble(1, -refundAmount); 
            pStmt.setInt(2, originalTicket.ticketID);
            pStmt.executeUpdate();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }
    
   public String generateConfirmation() {
        return "Ticket " + originalTicket.ticketNumber + 
        " cancelled. Refund: PHP " + String.format("%.2f", refundAmount);
    }
}

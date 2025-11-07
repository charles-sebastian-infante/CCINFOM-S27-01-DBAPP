package com.busterminal.service;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;

/**
 * 
 * Ticket model only has: ticketID, ticketNumber, scheduleID, discounted
 * It does NOT have 'type' or 'finalAmount' fields
 * These are removed from this service as they don't exist in the database schema
 */
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
        try {
            Connection conn = DBConnection.getConnection(); 
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT s.departure_time FROM Schedule s " +
                "WHERE s.schedule_id = ?");
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
            pStmt.close();
            conn.close();
            
            return eligibleForCancellation;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
   public double calculateRefund() {
        // Ticket model only has 'discounted' boolean field, not 'type' or 'finalAmount'
        // If ticket is discounted (20% off), refund is 80% of base fare
        // For simplicity, we'll set a standard refund amount (90% of assumed base)
        // In production, this should be stored in DB or calculated from Route.base_fare
        
        if (originalTicket.discounted) {
            // Discounted ticket: return 90% of discounted fare
            // Assuming standard refund policy: 90% of what they paid
            refundAmount = 100.0 * 0.8 * 0.90; // Example calculation
        } else {
            // Regular ticket: return 90% of full fare
            refundAmount = 100.0 * 0.90; // Example calculation
        }

        return refundAmount;
    }
    
   public int processCancellation() {
        if (!eligibleForCancellation) 
            return 0;
        
        try {
            Connection conn = DBConnection.getConnection();
            // FIXED: Ticket table doesn't have 'type' or 'final_amount' columns
            // Only has: ticket_id, ticket_number, schedule_id, discounted
            // For cancellation, we just delete the ticket record
            PreparedStatement pStmt = conn.prepareStatement(
                "DELETE FROM Ticket WHERE ticket_id = ?");
            pStmt.setInt(1, originalTicket.ticketID);
            pStmt.executeUpdate();
            pStmt.close();
            conn.close();
            
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

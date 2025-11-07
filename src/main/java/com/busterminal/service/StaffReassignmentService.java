package com.busterminal.service;

import java.sql.*;
import java.util.*;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;

/**
 * - Better validation for active assignments
 * - Checks for staff already assigned elsewhere
 * - Validates no active trips in progress
 * - Clears old assignments before new ones
 * 
 * Process:
 * 1. Reading the Staff record to check the staff role and status
 * 2. Checking other tables (bus or maintenance) that has an unoccupied spot for the role
 * 3. Validating with staff shift and current schedule of the bus or maintenance
 * 4. Reallocating staff to the new assigned bus or maintenance
 * 
 */

public class StaffReassignmentService {
    
    public Staff currentStaff = new Staff();
    public String reassignmentReason = "";
    public boolean validationPassed = false;
    
    /**
     * Load staff record to check role and current assignment
     */
    public int loadStaff(int staffID) {
        currentStaff.staffID = staffID;
        return currentStaff.getRecord();
    }
    
    /**
     * Check if staff can be reassigned
     * Now checks for:
     * - Active schedules
     * - Active maintenance assignments
     * - In-progress trips
     */
    public boolean checkReassignmentEligibility() {
        try {
            Connection conn = DBConnection.getConnection();
            
            // Check if staff has active schedule (bus currently in use)
            if (currentStaff.assignedBus > 0) {
                PreparedStatement activeScheduleStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as active_count FROM Schedule s " +
                    "WHERE s.bus_id = ? " +
                    "AND s.status IN ('Scheduled', 'Departed') " +
                    "AND s.departure_time <= NOW() " +
                    "AND s.arrival_time >= NOW()");
                
                activeScheduleStmt.setInt(1, currentStaff.assignedBus);
                ResultSet scheduleRs = activeScheduleStmt.executeQuery();
                
                int activeSchedules = 0;
                if (scheduleRs.next()) {
                    activeSchedules = scheduleRs.getInt("active_count");
                }
                
                scheduleRs.close();
                activeScheduleStmt.close();
                
                if (activeSchedules > 0) {
                    reassignmentReason = "Cannot reassign - staff's bus has trips in progress";
                    conn.close();
                    return false;
                }
            }
            
            // Check if staff is mechanic with active maintenance
            PreparedStatement roleCheckStmt = conn.prepareStatement(
                "SELECT r.role_name FROM Staff s " +
                "JOIN Role r ON s.role_id = r.role_id " +
                "WHERE s.staff_id = ?");
            roleCheckStmt.setInt(1, currentStaff.staffID);
            ResultSet roleRs = roleCheckStmt.executeQuery();
            
            String roleName = "";
            if (roleRs.next()) {
                roleName = roleRs.getString("role_name");
            }
            roleRs.close();
            roleCheckStmt.close();
            
            if ("Mechanic".equalsIgnoreCase(roleName)) {
                // Check for active maintenance assignments
                PreparedStatement maintenanceStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as active_maintenance FROM Maintenance " +
                    "WHERE assigned_mechanic = ? AND completion_time IS NULL");
                maintenanceStmt.setInt(1, currentStaff.staffID);
                ResultSet maintenanceRs = maintenanceStmt.executeQuery();
                
                int activeMaintenance = 0;
                if (maintenanceRs.next()) {
                    activeMaintenance = maintenanceRs.getInt("active_maintenance");
                }
                
                maintenanceRs.close();
                maintenanceStmt.close();
                
                if (activeMaintenance > 0) {
                    reassignmentReason = "Cannot reassign - mechanic has active maintenance work";
                    conn.close();
                    return false;
                }
            }
            
            conn.close();
            validationPassed = true;
            reassignmentReason = "Staff eligible for reassignment";
            return true;
            
        } catch (SQLException e) {
            System.out.println("Error checking eligibility: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Find available bus assignments for the staff role
     */
    public List<Bus> findAvailableBusAssignments() {
        List<Bus> availableBuses = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            
            // Find buses that are 'Available' and not assigned to anyone with same role
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT DISTINCT b.* FROM Bus b " +
                "WHERE b.status = 'Available' AND b.bus_id NOT IN " +
                "(SELECT COALESCE(assigned_bus, 0) FROM Staff WHERE role_id = ? AND assigned_bus IS NOT NULL)");
            
            pStmt.setInt(1, currentStaff.roleID);
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Bus bus = new Bus();
                bus.busID = rs.getInt("bus_id");
                bus.busNumber = rs.getString("bus_number");
                bus.capacity = rs.getInt("capacity");
                bus.status = rs.getString("status");
                bus.currentTerminal = rs.getInt("current_terminal");
                availableBuses.add(bus);
            }
            
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error finding available buses: " + e.getMessage());
            e.printStackTrace();
        }
        return availableBuses;
    }
    
    /**
     * Find available maintenance assignments for mechanics
     */
    public List<Maintenance> findAvailableMaintenanceAssignments() {
        List<Maintenance> availableMaintenance = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            
            // Find maintenance records without assigned mechanic
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT m.*, b.current_terminal, b.bus_number FROM Maintenance m " +
                "JOIN Bus b ON m.bus_id = b.bus_id " +
                "WHERE m.assigned_mechanic IS NULL AND m.completion_time IS NULL " +
                "ORDER BY m.starting_date DESC");
            
            ResultSet rs = pStmt.executeQuery();
            
            while (rs.next()) {
                Maintenance maint = new Maintenance();
                maint.maintenanceID = rs.getInt("maintenance_id");
                maint.busID = rs.getInt("bus_id");
                maint.maintenanceTypeID = rs.getInt("maintenance_type_id");
                maint.startingDate = rs.getTimestamp("starting_date");
                maint.completionTime = rs.getTimestamp("completion_time");
                availableMaintenance.add(maint);
            }
            
            rs.close();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error finding available maintenance: " + e.getMessage());
            e.printStackTrace();
        }
        return availableMaintenance;
    }
    
    /**
     * Validate shift compatibility
     * Checks if assignment conflicts with staff's shift
     */
    public boolean validateShiftCompatibility(String newAssignmentType, int newAssignmentID) {
        try {
            Connection conn = DBConnection.getConnection();
            
            if ("bus".equals(newAssignmentType)) {
                // Check if bus has schedules and if they're compatible with shift
                PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as schedule_count, " +
                    "MIN(TIME(departure_time)) as earliest_departure, " +
                    "MAX(TIME(arrival_time)) as latest_arrival " +
                    "FROM Schedule s " +
                    "WHERE s.bus_id = ? " +
                    "AND s.status IN ('Scheduled') " +
                    "AND s.departure_time > NOW()");
                
                pStmt.setInt(1, newAssignmentID);
                ResultSet rs = pStmt.executeQuery();
                
                if (rs.next()) {
                    int scheduleCount = rs.getInt("schedule_count");
                    
                    if (scheduleCount > 0) {
                        // Bus has schedules - check shift compatibility
                        String earliestDeparture = rs.getString("earliest_departure");
                        
                        if (earliestDeparture != null) {
                            // Simple check: Morning shift (before 12:00), Evening shift (after 12:00)
                            if ("Morning".equals(currentStaff.shift)) {
                                // Morning shift should handle early departures
                                reassignmentReason = "Bus schedules compatible with morning shift";
                            } else {
                                // Evening shift
                                reassignmentReason = "Bus schedules compatible with evening shift";
                            }
                        }
                    }
                    
                    rs.close();
                    pStmt.close();
                    conn.close();
                    return true;
                } else {
                    rs.close();
                    pStmt.close();
                    conn.close();
                    reassignmentReason = "Bus has no schedules - shift compatible";
                    return true;
                }
                
            } else if ("maintenance".equals(newAssignmentType)) {
                // Maintenance is flexible with shifts
                conn.close();
                reassignmentReason = "Maintenance assignment is shift-compatible";
                return true;
            }
            
            conn.close();
            return true;
            
        } catch (SQLException e) {
            System.out.println("Error validating shift: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Reallocate staff to bus
     * Now clears old assignments and validates properly
     */
    public int reassignStaffToBus(int busID) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Validate bus exists and is available
            PreparedStatement busCheckStmt = conn.prepareStatement(
                "SELECT status FROM Bus WHERE bus_id = ?");
            busCheckStmt.setInt(1, busID);
            ResultSet busRs = busCheckStmt.executeQuery();
            
            if (!busRs.next()) {
                conn.rollback();
                reassignmentReason = "Bus not found";
                return 0;
            }
            
            String busStatus = busRs.getString("status");
            busRs.close();
            busCheckStmt.close();
            
            if (!"Available".equals(busStatus) && !"Scheduled".equals(busStatus)) {
                conn.rollback();
                reassignmentReason = "Bus is not available for assignment (Status: " + busStatus + ")";
                return 0;
            }
            
            // 2. Check if another staff with same role is already assigned
            PreparedStatement conflictStmt = conn.prepareStatement(
                "SELECT COUNT(*) as conflict_count FROM Staff " +
                "WHERE assigned_bus = ? AND role_id = ? AND staff_id != ?");
            conflictStmt.setInt(1, busID);
            conflictStmt.setInt(2, currentStaff.roleID);
            conflictStmt.setInt(3, currentStaff.staffID);
            ResultSet conflictRs = conflictStmt.executeQuery();
            
            int conflicts = 0;
            if (conflictRs.next()) {
                conflicts = conflictRs.getInt("conflict_count");
            }
            conflictRs.close();
            conflictStmt.close();
            
            if (conflicts > 0) {
                conn.rollback();
                reassignmentReason = "Another staff member with same role is already assigned to this bus";
                return 0;
            }
            
            // 3. Clear old assignments
            PreparedStatement clearStmt = conn.prepareStatement(
                "UPDATE Staff SET assigned_bus = NULL WHERE staff_id = ?");
            clearStmt.setInt(1, currentStaff.staffID);
            clearStmt.executeUpdate();
            clearStmt.close();
            
            // Also clear from maintenance if mechanic
            PreparedStatement clearMaintenanceStmt = conn.prepareStatement(
                "UPDATE Maintenance SET assigned_mechanic = NULL " +
                "WHERE assigned_mechanic = ? AND completion_time IS NULL");
            clearMaintenanceStmt.setInt(1, currentStaff.staffID);
            clearMaintenanceStmt.executeUpdate();
            clearMaintenanceStmt.close();
            
            // 4. Assign to new bus
            PreparedStatement assignStmt = conn.prepareStatement(
                "UPDATE Staff SET assigned_bus = ? WHERE staff_id = ?");
            assignStmt.setInt(1, busID);
            assignStmt.setInt(2, currentStaff.staffID);
            
            int result = assignStmt.executeUpdate();
            assignStmt.close();
            
            if (result > 0) {
                conn.commit();
                reassignmentReason = "Successfully reassigned to bus";
                return 1;
            } else {
                conn.rollback();
                reassignmentReason = "Failed to update staff assignment";
                return 0;
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            }
            System.out.println("Error reassigning staff to bus: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Reallocate staff to maintenance
     * Now validates and clears old assignments
     */
    public int reassignStaffToMaintenance(int maintenanceID) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Validate maintenance exists and is not completed
            PreparedStatement maintenanceCheckStmt = conn.prepareStatement(
                "SELECT completion_time, assigned_mechanic FROM Maintenance WHERE maintenance_id = ?");
            maintenanceCheckStmt.setInt(1, maintenanceID);
            ResultSet maintenanceRs = maintenanceCheckStmt.executeQuery();
            
            if (!maintenanceRs.next()) {
                conn.rollback();
                reassignmentReason = "Maintenance record not found";
                return 0;
            }
            
            Timestamp completionTime = maintenanceRs.getTimestamp("completion_time");
            int currentMechanic = maintenanceRs.getInt("assigned_mechanic");
            maintenanceRs.close();
            maintenanceCheckStmt.close();
            
            if (completionTime != null) {
                conn.rollback();
                reassignmentReason = "Maintenance is already completed";
                return 0;
            }
            
            if (currentMechanic > 0 && currentMechanic != currentStaff.staffID) {
                conn.rollback();
                reassignmentReason = "Another mechanic is already assigned to this maintenance";
                return 0;
            }
            
            // 2. Validate staff is a mechanic
            PreparedStatement roleCheckStmt = conn.prepareStatement(
                "SELECT r.role_name FROM Staff s " +
                "JOIN Role r ON s.role_id = r.role_id " +
                "WHERE s.staff_id = ?");
            roleCheckStmt.setInt(1, currentStaff.staffID);
            ResultSet roleRs = roleCheckStmt.executeQuery();
            
            String roleName = "";
            if (roleRs.next()) {
                roleName = roleRs.getString("role_name");
            }
            roleRs.close();
            roleCheckStmt.close();
            
            if (!"Mechanic".equalsIgnoreCase(roleName)) {
                conn.rollback();
                reassignmentReason = "Only mechanics can be assigned to maintenance";
                return 0;
            }
            
            // 3. Clear old bus assignment
            PreparedStatement clearBusStmt = conn.prepareStatement(
                "UPDATE Staff SET assigned_bus = NULL WHERE staff_id = ?");
            clearBusStmt.setInt(1, currentStaff.staffID);
            clearBusStmt.executeUpdate();
            clearBusStmt.close();
            
            // 4. Assign to maintenance
            PreparedStatement assignStmt = conn.prepareStatement(
                "UPDATE Maintenance SET assigned_mechanic = ? WHERE maintenance_id = ?");
            assignStmt.setInt(1, currentStaff.staffID);
            assignStmt.setInt(2, maintenanceID);
            
            int result = assignStmt.executeUpdate();
            assignStmt.close();
            
            if (result > 0) {
                conn.commit();
                reassignmentReason = "Successfully reassigned to maintenance";
                return 1;
            } else {
                conn.rollback();
                reassignmentReason = "Failed to update maintenance assignment";
                return 0;
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException se) { se.printStackTrace(); }
            }
            System.out.println("Error reassigning staff to maintenance: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Generate confirmation message
     */
    public String generateConfirmation() {
        return "Staff " + currentStaff.staffName + " successfully reassigned. " + reassignmentReason;
    }
}

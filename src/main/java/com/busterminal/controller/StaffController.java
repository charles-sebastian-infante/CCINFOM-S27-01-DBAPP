package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.Staff;
import com.busterminal.service.StaffReassignmentService;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/staff")
public class StaffController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("list".equals(action)) 
            listStaff(request, response);
        else if ("edit".equals(action)) 
            editStaffForm(request, response);
        else 
            listStaff(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("create".equals(action)) 
            addStaff(request, response);
        else if ("update".equals(action)) 
            updateStaff(request, response);
        else if ("delete".equals(action)) 
            deleteStaff(request, response);
    }
    
    private void listStaff(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Staff");
            ResultSet rs = pStmt.executeQuery();
            
            List<Staff> staffList = new ArrayList<>();
            while(rs.next()) {
                Staff s = new Staff();
                s.staffID = rs.getInt("staff_id");
                s.staffName = rs.getString("staff_name");
                s.roleID = rs.getInt("role_id");
                s.assignedTerminal = rs.getInt("assigned_terminal");
                s.assignedBus = rs.getInt("assigned_bus");
                s.shift = rs.getString("shift");
                s.contact = rs.getString("contact");
                staffList.add(s);
            }
            
            request.setAttribute("staffList", staffList);
            request.getRequestDispatcher("/admin/manage_staff.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void editStaffForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Staff staff = new Staff();
            staff.staffID = Integer.parseInt(request.getParameter("id"));
            
            if(staff.getRecord() == 1) {
                request.setAttribute("staff", staff);
                request.getRequestDispatcher("/admin/manage_staff.jsp")
                    .forward(request, response);
            } else {
                response.sendRedirect("staff?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("staff?action=list");
        }
    }
    
    private void addStaff(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Staff staff = new Staff();
            staff.staffName = request.getParameter("staffName");
            staff.roleID = Integer.parseInt(request.getParameter("roleID"));
            staff.assignedTerminal = Integer.parseInt(request
                .getParameter("assignedTerminal"));
            staff.assignedBus = Integer.parseInt(request
                .getParameter("assignedBus"));
            staff.shift = request.getParameter("shift");
            staff.contact = request.getParameter("contact");
            
            if(staff.addRecord() == 1) {
                response.sendRedirect("staff?action=list");
            } else {
                request.setAttribute("error", "Failed to add staff");
                request.getRequestDispatcher("/admin/manage_staff.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_staff.jsp")
                .forward(request, response);
        }
    }
    
    private void updateStaff(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Staff staff = new Staff();
            staff.staffID = Integer.parseInt(request.getParameter("staffID"));
            staff.staffName = request.getParameter("staffName");
            staff.roleID = Integer.parseInt(request.getParameter("roleID"));
            staff.assignedTerminal = Integer.parseInt(request
                .getParameter("assignedTerminal"));
            staff.assignedBus = Integer.parseInt(request
                .getParameter("assignedBus"));
            staff.shift = request.getParameter("shift");
            staff.contact = request.getParameter("contact");
            
            if(staff.modRecord() == 1) {
                response.sendRedirect("staff?action=list");
            } else {
                request.setAttribute("error", "Failed to update staff");
                request.setAttribute("staff", staff);
                request.getRequestDispatcher("/admin/manage_staff.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_staff.jsp")
                .forward(request, response);
        }
    }
    
    private void deleteStaff(HttpServletRequest request,
        HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Staff staff = new Staff();
            staff.staffID = Integer.parseInt(request.getParameter("id"));
            
            if(staff.delRecord() == 1) {
                response.sendRedirect("staff?action=list");
            } else {
                request.setAttribute("error", "Failed to delete staff");
                response.sendRedirect("staff?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("staff?action=list");
        }
    }

    private void reassignStaffForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int staffID = Integer.parseInt(request.getParameter("staffID"));
            Staff staff = new Staff();
            staff.staffID = staffID;
            staff.getRecord();
            
            request.setAttribute("staff", staff);
            request.getRequestDispatcher("/admin/reassign_staff_form.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("staff");
        }
    }

    private void processReassignment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int staffID = Integer.parseInt(request.getParameter("staffID"));
            String assignmentType = request.getParameter("assignmentType"); // "bus" or "maintenance"
            int assignmentID = Integer.parseInt(request.getParameter("assignmentID"));
            
            StaffReassignmentService reassignmentService = new StaffReassignmentService();
            reassignmentService.loadStaff(staffID);
            
            // Check eligibility
            if (!reassignmentService.checkReassignmentEligibility()) {
                request.setAttribute("error", reassignmentService.reassignmentReason);
                reassignStaffForm(request, response);
                return;
            }
            
            // Validate shift
            if (!reassignmentService.validateShiftCompatibility(assignmentType, assignmentID)) {
                request.setAttribute("error", "Shift incompatible");
                reassignStaffForm(request, response);
                return;
            }
            
            // Perform reassignment
            int result;
            if ("bus".equals(assignmentType)) {
                result = reassignmentService.reassignStaffToBus(assignmentID);
            } else {
                result = reassignmentService.reassignStaffToMaintenance(assignmentID);
            }
            
            if (result == 1) {
                request.setAttribute("success", reassignmentService.generateConfirmation());
            } else {
                request.setAttribute("error", "Reassignment failed");
            }
            
            response.sendRedirect("staff");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            response.sendRedirect("staff");
        }
    }
}

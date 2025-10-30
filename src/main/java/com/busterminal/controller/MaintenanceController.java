package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.Maintenance;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/maintenance")
public class MaintenanceController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("list".equals(action)) 
            listMaintenance(request, response);
        else if ("edit".equals(action)) 
            editMaintenanceForm(request, response);
        else 
            listMaintenance(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("create".equals(action)) 
            addMaintenance(request, response);
        else if ("update".equals(action)) 
            updateMaintenance(request, response);
        else if ("delete".equals(action)) 
            deleteMaintenance(request, response);
    }
    
    private void listMaintenance(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Maintenance");
            ResultSet rs = pStmt.executeQuery();
            
            List<Maintenance> maintenanceList = new ArrayList<>();
            while(rs.next()) {
                Maintenance m = new Maintenance();
                m.maintenanceID = rs.getInt("maintenance_id");
                m.busID = rs.getInt("bus_id");
                m.maintenanceDate = rs.getString("maintenance_date");
                m.description = rs.getString("description");
                m.status = rs.getString("status");
                maintenanceList.add(m);
            }
            
            request.setAttribute("maintenanceList", maintenanceList);
            request.getRequestDispatcher("/admin/maintenance.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void editMaintenanceForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Maintenance maintenance = new Maintenance();
            maintenance.maintenanceID = Integer.parseInt(request
                .getParameter("id"));
            
            if(maintenance.getRecord() == 1) {
                request.setAttribute("maintenance", maintenance);
                request.getRequestDispatcher("/admin/maintenance.jsp")
                    .forward(request, response);
            } else {
                response.sendRedirect("maintenance?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("maintenance?action=list");
        }
    }
    
    private void addMaintenance(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Maintenance maintenance = new Maintenance();
            maintenance.busID = Integer.parseInt(request
                .getParameter("busID"));
            maintenance.maintenanceDate = request
            .getParameter("maintenanceDate");
            maintenance.description = request.getParameter("description");
            maintenance.status = request.getParameter("status");
            
            if(maintenance.addRecord() == 1) {
                response.sendRedirect("maintenance?action=list");
            } else {
                request.setAttribute("error", "Failed to add maintenance");
                request.getRequestDispatcher("/admin/maintenance.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/maintenance.jsp")
                .forward(request, response);
        }
    }
    
    private void updateMaintenance(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Maintenance maintenance = new Maintenance();
            maintenance.maintenanceID = Integer.parseInt(request
                .getParameter("maintenanceID"));
            maintenance.busID = Integer.parseInt(request
                .getParameter("busID"));
            maintenance.maintenanceDate = request
            .getParameter("maintenanceDate");
            maintenance.description = request.getParameter("description");
            maintenance.status = request.getParameter("status");
            
            if(maintenance.modRecord() == 1) {
                response.sendRedirect("maintenance?action=list");
            } else {
                request.setAttribute("error", "Failed to update maintenance");
                request.setAttribute("maintenance", maintenance);
                request.getRequestDispatcher("/admin/maintenance.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/maintenance.jsp")
                .forward(request, response);
        }
    }
    
    private void deleteMaintenance(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Maintenance maintenance = new Maintenance();
            maintenance.maintenanceID = Integer.parseInt(request
                .getParameter("id"));
            
            if(maintenance.delRecord() == 1) {
                response.sendRedirect("maintenance?action=list");
            } else {
                request.setAttribute("error", "Failed to delete maintenance");
                response.sendRedirect("maintenance?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("maintenance?action=list");
        }
    }
}

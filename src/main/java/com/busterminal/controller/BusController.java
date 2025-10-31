package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.Bus;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/bus")
public class BusController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("list".equals(action)) 
            listBuses(request, response);
        else if ("edit".equals(action)) 
            editBusForm(request, response);
        else 
            listBuses(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("create".equals(action)) 
            addBus(request, response);
        else if ("update".equals(action)) 
            updateBus(request, response);
        else if ("delete".equals(action)) 
            deleteBus(request, response);
    }
    
    private void listBuses(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Bus");
            ResultSet rs = pStmt.executeQuery();
            
            List<Bus> buses = new ArrayList<>();
            while(rs.next()) {
                Bus b = new Bus();
                b.busID = rs.getInt("bus_id");
                b.busNumber = rs.getString("bus_number");
                b.capacity = rs.getInt("capacity");
                b.status = rs.getString("status");
                b.currentTerminal = rs.getInt("current_terminal");
                buses.add(b);
            }
            
            request.setAttribute("buses", buses);
            request.getRequestDispatcher("/admin/manage_bus.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void editBusForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Bus bus = new Bus();
            bus.busID = Integer.parseInt(request.getParameter("id"));
            
            if(bus.getRecord() == 1) {
                request.setAttribute("bus", bus);
                request.getRequestDispatcher("/admin/manage_bus.jsp")
                    .forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/bus?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/bus?action=list");
        }
    }
    
    private void addBus(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Bus bus = new Bus();
            bus.busNumber = request.getParameter("busNumber");
            bus.capacity = Integer.parseInt(request.getParameter("capacity"));
            bus.status = request.getParameter("status");
            bus.currentTerminal = Integer.parseInt(request
                .getParameter("currentTerminal"));
            
            if(bus.addRecord() == 1) {
                response.sendRedirect(request.getContextPath() + "/bus?action=list");
            } else {
                request.setAttribute("error", "Failed to add bus");
                request.getRequestDispatcher("/admin/manage_bus.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_bus.jsp")
                .forward(request, response);
        }
    }
    
    private void updateBus(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Bus bus = new Bus();
            bus.busID = Integer.parseInt(request.getParameter("busID"));
            bus.busNumber = request.getParameter("busNumber");
            bus.capacity = Integer.parseInt(request.getParameter("capacity"));
            bus.status = request.getParameter("status");
            bus.currentTerminal = Integer.parseInt(request
                .getParameter("currentTerminal"));
            
            if(bus.modRecord() == 1) {
                response.sendRedirect(request.getContextPath() + "/bus?action=list");
            } else {
                request.setAttribute("error", "Failed to update bus");
                request.setAttribute("bus", bus);
                request.getRequestDispatcher("/admin/manage_bus.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_bus.jsp")
                .forward(request, response);
        }
    }
    
    private void deleteBus(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Bus bus = new Bus();
            bus.busID = Integer.parseInt(request.getParameter("id"));
            
            if(bus.delRecord() == 1) {
                response.sendRedirect(request.getContextPath() + "/bus?action=list");
            } else {
                request.setAttribute("error", "Failed to delete bus");
                response.sendRedirect(request.getContextPath() + "/bus?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/bus?action=list");
        }
    }
}

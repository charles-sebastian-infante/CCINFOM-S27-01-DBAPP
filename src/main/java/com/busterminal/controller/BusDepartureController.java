package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.service.BusDepartureService;
import java.io.IOException;

@WebServlet("/departure")
public class BusDepartureController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("form".equals(action)) {
            showDepartureForm(request, response);
        } else {
            showDepartureForm(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("process".equals(action)) {
            processDeparture(request, response);
        }
    }
    
    private void showDepartureForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/admin/bus_departure.jsp")
            .forward(request, response);
    }
    
    private void processDeparture(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            BusDepartureService service = new BusDepartureService();
            
            // Get parameters
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            int busID = Integer.parseInt(request.getParameter("busID"));
            String departureDate = request.getParameter("departureDate");
            
            // Load schedule and bus
            service.departureSchedule.scheduleID = scheduleID;
            service.departureSchedule.getRecord();
            
            service.departingBus.busID = busID;
            service.departingBus.getRecord();
            
            // Load route
            service.busRoute.routeID = service.departureSchedule.routeID;
            service.loadRoute();
            
            // Load passenger manifest
            if (service.loadPassengerManifest(scheduleID, busID, departureDate) == 1) {
                // Confirm departure (updates schedule and bus status)
                if (service.confirmDeparture() == 1) {
                    request.setAttribute("success", "Bus departed successfully!");
                    request.setAttribute("bus", service.departingBus);
                    request.setAttribute("schedule", service.departureSchedule);
                    request.setAttribute("route", service.busRoute);
                    request.setAttribute("manifest", service.passengerManifest);
                    request.getRequestDispatcher("/admin/departure_success.jsp")
                        .forward(request, response);
                } else {
                    request.setAttribute("error", "Failed to confirm departure");
                    request.getRequestDispatcher("/admin/bus_departure.jsp")
                        .forward(request, response);
                }
            } else {
                request.setAttribute("error", "Failed to load passenger manifest");
                request.getRequestDispatcher("/admin/bus_departure.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/bus_departure.jsp")
                .forward(request, response);
        }
    }
}

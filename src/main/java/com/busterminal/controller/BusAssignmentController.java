package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.service.BusMaintenanceAssignment;
import java.io.IOException;

@WebServlet("/bus-assignment")
public class BusAssignmentController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("maintenance-form".equals(action)) {
            showMaintenanceForm(request, response);
        } else if ("assignment-form".equals(action)) {
            showAssignmentForm(request, response);
        } else if ("find-buses".equals(action)) {
            findAvailableBuses(request, response);
        } else {
            showAssignmentForm(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("schedule-maintenance".equals(action)) {
            scheduleMaintenance(request, response);
        } else if ("assign-route".equals(action)) {
            assignBusToRoute(request, response);
        }
    }
    
    private void showMaintenanceForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/admin/schedule_maintenance.jsp")
            .forward(request, response);
    }
    
    private void showAssignmentForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/admin/assign_bus.jsp")
            .forward(request, response);
    }
    
    private void findAvailableBuses(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            BusMaintenanceAssignment service = new BusMaintenanceAssignment();
            
            int routeID = Integer.parseInt(request.getParameter("routeID"));
            int requiredCapacity = Integer.parseInt(request.getParameter("capacity"));
            
            // Load route
            if (service.loadRoute(routeID) == 1) {
                // Find available buses
                if (service.findAvailableBuses(requiredCapacity) == 1) {
                    request.setAttribute("route", service.assignedRoute);
                    request.setAttribute("availableBuses", service.availableBuses);
                    request.getRequestDispatcher("/admin/assign_bus.jsp")
                        .forward(request, response);
                } else {
                    request.setAttribute("error", "No available buses found");
                    request.getRequestDispatcher("/admin/assign_bus.jsp")
                        .forward(request, response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/assign_bus.jsp")
                .forward(request, response);
        }
    }
    
    private void scheduleMaintenance(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            BusMaintenanceAssignment service = new BusMaintenanceAssignment();
            
            // Get bus
            service.selectedBus.busID = Integer.parseInt(
                request.getParameter("busID"));
            service.selectedBus.getRecord();
            
            // Set maintenance details
            service.maintenanceRecord.busID = service.selectedBus.busID;
            service.maintenanceRecord.maintenanceDate = 
                request.getParameter("maintenanceDate");
            service.maintenanceRecord.description = 
                request.getParameter("description");
            service.maintenanceRecord.status = "Pending";
            
            // Schedule maintenance
            if (service.scheduleMaintenance() == 1) {
                request.setAttribute("success", 
                    "Maintenance scheduled successfully for Bus " + 
                    service.selectedBus.busNumber);
                response.sendRedirect(request.getContextPath() + 
                    "/maintenance?action=list");
            } else {
                request.setAttribute("error", "Failed to schedule maintenance");
                request.getRequestDispatcher("/admin/schedule_maintenance.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/schedule_maintenance.jsp")
                .forward(request, response);
        }
    }
    
    private void assignBusToRoute(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            BusMaintenanceAssignment service = new BusMaintenanceAssignment();
            
            // Get parameters
            int routeID = Integer.parseInt(request.getParameter("routeID"));
            int busID = Integer.parseInt(request.getParameter("busID"));
            String departureTime = request.getParameter("departureTime");
            String arrivalTime = request.getParameter("arrivalTime");
            
            // Load route
            if (service.loadRoute(routeID) == 1) {
                // Select bus
                service.selectedBus.busID = busID;
                service.selectedBus.getRecord();
                
                // Check for conflicts
                if (service.checkScheduleConflicts(busID, departureTime)) {
                    // Create schedule
                    if (service.createSchedule(departureTime, arrivalTime) == 1) {
                        request.setAttribute("success", 
                            "Bus " + service.selectedBus.busNumber + 
                            " assigned to route " + service.assignedRoute.routeName);
                        response.sendRedirect(request.getContextPath() + 
                            "/schedule?action=list");
                    } else {
                        request.setAttribute("error", "Failed to create schedule");
                        request.getRequestDispatcher("/admin/assign_bus.jsp")
                            .forward(request, response);
                    }
                } else {
                    request.setAttribute("error", 
                        "Schedule conflict detected for this bus at the selected time");
                    request.getRequestDispatcher("/admin/assign_bus.jsp")
                        .forward(request, response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/assign_bus.jsp")
                .forward(request, response);
        }
    }
}

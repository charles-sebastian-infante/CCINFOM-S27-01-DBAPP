package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.Schedule;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/schedule")
public class ScheduleController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("list".equals(action)) {
            listSchedules(request, response);
        } else if ("edit".equals(action)) {
            editScheduleForm(request, response);
        } else {
            listSchedules(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("create".equals(action)) {
            addSchedule(request, response);
        } else if ("update".equals(action)) {
            updateSchedule(request, response);
        } else if ("delete".equals(action)) {
            deleteSchedule(request, response);
        }
    }
    
    private void listSchedules(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Schedule");
            ResultSet rs = pStmt.executeQuery();
            
            List<Schedule> schedules = new ArrayList<>();
            while(rs.next()) {
                Schedule sch = new Schedule();
                sch.scheduleID = rs.getInt("schedule_id");
                sch.busID = rs.getInt("bus_id");
                sch.departureTime = rs.getString("departure_time");
                sch.arrivalTime = rs.getString("arrival_time");
                sch.status = rs.getString("status");
                sch.routeID = rs.getInt("route_id");
                schedules.add(sch);
            }
            
            request.setAttribute("schedules", schedules);
            request.getRequestDispatcher("/admin/schedule.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void editScheduleForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Schedule schedule = new Schedule();
            schedule.scheduleID = Integer.parseInt(request.getParameter("id"));
            
            if(schedule.getRecord() == 1) {
                request.setAttribute("schedule", schedule);
                request.getRequestDispatcher("/admin/schedule.jsp")
                    .forward(request, response);
            } else {
                response.sendRedirect("schedule?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("schedule?action=list");
        }
    }
    
    private void addSchedule(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Schedule schedule = new Schedule();
            schedule.busID = Integer.parseInt(request.getParameter("busID"));
            schedule.departureTime = request.getParameter("departureTime");
            schedule.arrivalTime = request.getParameter("arrivalTime");
            schedule.status = request.getParameter("status");
            schedule.routeID = Integer.parseInt(request
                .getParameter("routeID"));
            
            if(schedule.addRecord() == 1) {
                response.sendRedirect("schedule?action=list");
            } else {
                request.setAttribute("error", "Failed to add schedule");
                request.getRequestDispatcher("/admin/schedule.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/schedule.jsp")
                .forward(request, response);
        }
    }
    
    private void updateSchedule(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Schedule schedule = new Schedule();
            schedule.scheduleID = Integer.parseInt(request
                .getParameter("scheduleID"));
            schedule.busID = Integer.parseInt(request.getParameter("busID"));
            schedule.departureTime = request.getParameter("departureTime");
            schedule.arrivalTime = request.getParameter("arrivalTime");
            schedule.status = request.getParameter("status");
            schedule.routeID = Integer.parseInt(request
                .getParameter("routeID"));
            
            if(schedule.modRecord() == 1) {
                response.sendRedirect("schedule?action=list");
            } else {
                request.setAttribute("error", "Failed to update schedule");
                request.setAttribute("schedule", schedule);
                request.getRequestDispatcher("/admin/schedule.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/schedule.jsp")
                .forward(request, response);
        }
    }
    
    private void deleteSchedule(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Schedule schedule = new Schedule();
            schedule.scheduleID = Integer.parseInt(request.getParameter("id"));
            
            if(schedule.delRecord() == 1) {
                response.sendRedirect("schedule?action=list");
            } else {
                request.setAttribute("error", "Failed to delete schedule");
                response.sendRedirect("schedule?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("schedule?action=list");
        }
    }
}

package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.Route;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/route")
public class RouteController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("list".equals(action)) 
            listRoutes(request, response);
        else if ("edit".equals(action)) 
            editRouteForm(request, response);
        else 
            listRoutes(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("create".equals(action)) 
            addRoute(request, response);
        else if ("update".equals(action)) 
            updateRoute(request, response);
        else if ("delete".equals(action)) 
            deleteRoute(request, response);
    }
    
    private void listRoutes(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Route");
            ResultSet rs = pStmt.executeQuery();
            
            List<Route> routes = new ArrayList<>();
            while(rs.next()) {
                Route r = new Route();
                r.routeID = rs.getInt("route_id");
                r.routeName = rs.getString("route_name");
                r.originID = rs.getInt("origin_id");
                r.destinationID = rs.getInt("destination_id");
                r.distance = rs.getDouble("distance");
                r.travelTime = rs.getString("travel_time");
                r.baseFare = rs.getDouble("base_fare");
                routes.add(r);
            }
            
            request.setAttribute("routes", routes);
            request.getRequestDispatcher("/admin/manage_routes.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void editRouteForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Route route = new Route();
            route.routeID = Integer.parseInt(request.getParameter("id"));
            
            if(route.getRecord() == 1) {
                request.setAttribute("route", route);
                request.getRequestDispatcher("/admin/manage_routes.jsp")
                    .forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/route?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/route?action=list");
        }
    }
    
    private void addRoute(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Route route = new Route();
            route.routeName = request.getParameter("routeName");
            route.originID = Integer.parseInt(request
                .getParameter("originID"));
            route.destinationID = Integer.parseInt(request
                .getParameter("destinationID"));
            route.distance = Double.parseDouble(request
                .getParameter("distance"));
            route.travelTime = request.getParameter("travelTime");
            route.baseFare = Double.parseDouble(request
                .getParameter("baseFare"));
            
            if(route.addRecord() == 1) {
                response.sendRedirect(request.getContextPath() + "/route?action=list");
            } else {
                request.setAttribute("error", "Failed to add route");
                request.getRequestDispatcher("/admin/manage_routes.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_routes.jsp")
                .forward(request, response);
        }
    }
    
    private void updateRoute(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Route route = new Route();
            route.routeID = Integer.parseInt(request.getParameter("routeID"));
            route.routeName = request.getParameter("routeName");
            route.originID = Integer.parseInt(request
                .getParameter("originID"));
            route.destinationID = Integer.parseInt(request
                .getParameter("destinationID"));
            route.distance = Double.parseDouble(request
                .getParameter("distance"));
            route.travelTime = request.getParameter("travelTime");
            route.baseFare = Double.parseDouble(request
                .getParameter("baseFare"));
            
            if(route.modRecord() == 1) {
                response.sendRedirect(request.getContextPath() + "/route?action=list");
            } else {
                request.setAttribute("error", "Failed to update route");
                request.setAttribute("route", route);
                request.getRequestDispatcher("/admin/manage_routes.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_routes.jsp")
                .forward(request, response);
        }
    }
    
    private void deleteRoute(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Route route = new Route();
            route.routeID = Integer.parseInt(request.getParameter("id"));
            
            if(route.delRecord() == 1) {
                response.sendRedirect(request.getContextPath() + "/route?action=list");
            } else {
                request.setAttribute("error", "Failed to delete route");
                response.sendRedirect(request.getContextPath() + "/route?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/route?action=list");
        }
    }
}

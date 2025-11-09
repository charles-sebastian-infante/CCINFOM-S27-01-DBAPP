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
            List<Route> routes = Route.getAllRoutes();

            // Get all terminals for the dropdown in the form
            List<com.busterminal.model.Terminal> terminals = com.busterminal.model.Terminal.getAllTerminals();

            request.setAttribute("routes", routes);
            request.setAttribute("terminals", terminals);
            request.getRequestDispatcher("/admin/manage_routes.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error retrieving routes: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_routes.jsp")
                    .forward(request, response);
        }
    }

    private void editRouteForm(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            Route route = new Route();
            route.routeID = Integer.parseInt(request.getParameter("id"));

            if (route.getRecord() == 1) {
                // Get all terminals for the dropdown
                List<com.busterminal.model.Terminal> terminals = com.busterminal.model.Terminal.getAllTerminals();

                request.setAttribute("route", route);
                request.setAttribute("terminals", terminals);
                request.getRequestDispatcher("/admin/manage_routes.jsp")
                        .forward(request, response);
            } else {
                response.sendRedirect("route?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("route?action=list");
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
            String travelTimeStr = request.getParameter("travelTime");

            if (travelTimeStr != null && !travelTimeStr.isEmpty()) {
                route.travelTime = Time.valueOf(travelTimeStr);
            }

            route.baseFare = Double.parseDouble(request
                    .getParameter("baseFare"));

            // Validate origin ≠ destination
            if (route.originID == route.destinationID) {
                request.setAttribute("error",
                        "Route cannot have the same origin and destination terminal");
                request.setAttribute("route", route);

                // Get all terminals for the dropdown
                List<com.busterminal.model.Terminal> terminals = com.busterminal.model.Terminal.getAllTerminals();
                request.setAttribute("terminals", terminals);

                request.getRequestDispatcher("/admin/manage_routes.jsp")
                        .forward(request, response);
                return;
            }

            if (route.addRecord() == 1) {
                response.sendRedirect("route?action=list");
            } else {
                request.setAttribute("error", "Failed to add route");

                // Get all terminals for the dropdown
                List<com.busterminal.model.Terminal> terminals = com.busterminal.model.Terminal.getAllTerminals();
                request.setAttribute("terminals", terminals);

                request.getRequestDispatcher("/admin/manage_routes.jsp")
                        .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());

            // Get all terminals for the dropdown
            List<com.busterminal.model.Terminal> terminals = com.busterminal.model.Terminal.getAllTerminals();
            request.setAttribute("terminals", terminals);

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
            String travelTimeStr = request.getParameter("travelTime");

            if (travelTimeStr != null && !travelTimeStr.isEmpty()) {
                route.travelTime = Time.valueOf(travelTimeStr);
            }
            route.baseFare = Double.parseDouble(request
                    .getParameter("baseFare"));

            // Validate origin ≠ destination
            if (route.originID == route.destinationID) {
                request.setAttribute("error",
                        "Route cannot have the same origin and destination terminal");
                request.setAttribute("route", route);

                // Get all terminals for the dropdown
                List<com.busterminal.model.Terminal> terminals = com.busterminal.model.Terminal.getAllTerminals();
                request.setAttribute("terminals", terminals);

                request.getRequestDispatcher("/admin/manage_routes.jsp")
                        .forward(request, response);
                return;
            }

            if (route.modRecord() == 1) {
                response.sendRedirect("route?action=list");
            } else {
                request.setAttribute("error", "Failed to update route");
                request.setAttribute("route", route);

                // Get all terminals for the dropdown
                List<com.busterminal.model.Terminal> terminals = com.busterminal.model.Terminal.getAllTerminals();
                request.setAttribute("terminals", terminals);

                request.getRequestDispatcher("/admin/manage_routes.jsp")
                        .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());

            // Get all terminals for the dropdown
            List<com.busterminal.model.Terminal> terminals = com.busterminal.model.Terminal.getAllTerminals();
            request.setAttribute("terminals", terminals);

            request.getRequestDispatcher("/admin/manage_routes.jsp")
                    .forward(request, response);
        }
    }

    private void deleteRoute(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            Route route = new Route();
            route.routeID = Integer.parseInt(request.getParameter("id"));

            if (route.delRecord() == 1) {
                response.sendRedirect("route?action=list");
            } else {
                request.setAttribute("error", "Failed to delete route");
                response.sendRedirect("route?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("route?action=list");
        }
    }
}

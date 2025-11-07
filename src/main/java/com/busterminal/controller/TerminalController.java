package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.*;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/terminal")
public class TerminalController extends HttpServlet {
    
    @Override
    protected void doGet (HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("list".equals(action)) 
            listTerminals(request, response);
        else if ("edit".equals(action)) 
            editTerminalForm(request, response);
        else 
            listTerminals(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("create".equals(action)) 
            addTerminal(request, response);
        else if ("update".equals(action)) 
            updateTerminal(request, response);
        else if ("delete".equals(action)) 
            deleteTerminal(request, response);
    }
    
    private void listTerminals(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                "SELECT * FROM Terminal");
            ResultSet rs = pStmt.executeQuery();
            
            List<Terminal> terminals = new ArrayList<>();
            while(rs.next()) {
                Terminal t = new Terminal();
                t.terminalID = rs.getInt("terminal_id");
                t.terminalName = rs.getString("terminal_name");
                t.address = rs.getString("address");
                t.phone = rs.getString("phone");
                terminals.add(t);
            }
            
            request.setAttribute("terminals", terminals);
            request.getRequestDispatcher("/admin/manage_terminals.jsp")
                .forward(request, response); 
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void editTerminalForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Terminal terminal = new Terminal();
            terminal.terminalID = Integer.parseInt(request.getParameter("id"));
            
            if(terminal.getRecord() == 1) {
                request.setAttribute("terminal", terminal);
                request.getRequestDispatcher("/admin/manage_terminals.jsp")
                    .forward(request, response);
            } else {
                response.sendRedirect("terminal?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();    
            response.sendRedirect("terminal?action=list");
        }
    }
    
    private void addTerminal(HttpServletRequest request, 
            HttpServletResponse response) throws ServletException, IOException {
            try {
                Terminal terminal = new Terminal();
                terminal.terminalName = request.getParameter("terminalName");
                terminal.address = request.getParameter("address");
                terminal.phone = request.getParameter("phone");
               
                if(terminal.addRecord() == 1) {
                    response.sendRedirect(request.getContextPath() + "/terminal?action=list");
                } else {
                    request.setAttribute("error", "Failed to add terminal");
                    request.getRequestDispatcher("/admin/manage_terminals.jsp")
                        .forward(request, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("error", "Error: " + e.getMessage());
                request.getRequestDispatcher("/admin/manage_terminals.jsp")
                    .forward(request, response);
            }
        }
    
    private void updateTerminal(HttpServletRequest request, 
            HttpServletResponse response) throws ServletException, IOException {
            try {
                Terminal terminal = new Terminal();
                terminal.terminalID = Integer.parseInt(request
                    .getParameter("terminalID"));
                terminal.terminalName = request.getParameter("terminalName");
                terminal.address = request.getParameter("address");
                terminal.phone = request.getParameter("phone");
               
                if(terminal.modRecord() == 1) {
                    response.sendRedirect(request.getContextPath() + "/terminal?action=list");
                } else {
                    request.setAttribute("error", "Failed to update terminal");
                    request.setAttribute("terminal", terminal);
                    request.getRequestDispatcher("/admin/manage_terminals.jsp")
                        .forward(request, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("error", "Error: " + e.getMessage());
                request.getRequestDispatcher("/admin/manage_terminals.jsp")
                    .forward(request, response);
            }
        }
    
    private void deleteTerminal(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            Terminal terminal = new Terminal();
            terminal.terminalID = Integer.parseInt(request.getParameter("id"));
            
            if(terminal.delRecord() == 1) {
                response.sendRedirect("terminal?action=list");
            } else {
                request.setAttribute("error", "Failed to delete terminal");
                response.sendRedirect("terminal?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("terminal?action=list");
        }
    }

    private void viewTerminalDetails(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int terminalID = Integer.parseInt(request.getParameter("terminalID"));
            Terminal terminal = new Terminal();
            terminal.terminalID = terminalID;
            terminal.getRecord();
            
            List<Route> routes = Route.getRoutesByOriginTerminal(terminalID);
            
            request.setAttribute("terminal", terminal);
            request.setAttribute("routes", routes);
            request.getRequestDispatcher("/admin/terminal_details.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("terminals");
        }
    }
}

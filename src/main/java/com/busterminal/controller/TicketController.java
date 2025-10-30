package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.Ticket;
import com.busterminal.service.TicketPurchaseService;
import com.busterminal.service.TicketCancellation;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/ticket")
public class TicketController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("purchase".equals(action)) {
            purchaseForm(request, response);
        } else if ("cancel".equals(action)) {
            cancelForm(request, response);
        } else {
            purchaseForm(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("buy".equals(action)) {
            buyTicket(request, response);
        } else if ("cancelTicket".equals(action)) {
            cancelTicket(request, response);
        }
    }
    
    private void purchaseForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/user/book_ticket.jsp")
            .forward(request, response);
    }
    
    private void buyTicket(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            TicketPurchaseService service = new TicketPurchaseService();
            
            int routeID = Integer.parseInt(request.getParameter("routeID"));
            int scheduleID = Integer.parseInt(request
                .getParameter("scheduleID"));
            String ticketType = request.getParameter("ticketType");
            
            service.selectedRoute.routeId = routeID;
            service.selectedRoute.getRecord();
            
            Ticket ticket = new Ticket();
            ticket.routeID = routeID;
            ticket.scheduleID = scheduleID;
            ticket.type = ticketType;
            ticket.finalAmount = service.calculatePrice(
                service.selectedRoute.baseFare, 
                ticketType, 
                20.0
            );
            
            if(service.confirmPurchase() == 1) {
                request.setAttribute(
                    "success", "Ticket purchased successfully!");
                request.setAttribute("ticket", ticket);
                request.getRequestDispatcher("/user/ticket_details.jsp")
                    .forward(request, response);
            } else {
                request.setAttribute("error", "Failed to purchase ticket");
                request.getRequestDispatcher("/user/book_ticket.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/user/book_ticket.jsp")
                .forward(request, response);
        }
    }
    
    private void cancelForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/user/cancel_ticket.jsp")
            .forward(request, response);
    }
    
    private void cancelTicket(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            TicketCancellation service = new TicketCancellation ();
            
            int ticketID = Integer.parseInt(request.getParameter("ticketID"));
            service.loadTicket(ticketID);
            
            if(service.checkEligibility()) {
                service.calculateRefund();
                
                if(service.processCancellation() == 1) {
                    request.setAttribute("success", service
                        .generateConfirmation());
                    request.getRequestDispatcher("/user/ticket_details.jsp")
                        .forward(request, response);
                } else {
                    request.setAttribute("error", "Failed to cancel ticket");
                    request.getRequestDispatcher("/user/cancel_ticket.jsp")
                        .forward(request, response);
                }
            } else {
                request.setAttribute("error", service.cancellationReason);
                request.getRequestDispatcher("/user/cancel_ticket.jsp")
                    .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/user/cancel_ticket.jsp")
                .forward(request, response);
        }
    }
}

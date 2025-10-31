package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.Ticket;
import com.busterminal.model.Schedule;
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
            
            service.selectedRoute.routeID = routeID;
            service.selectedRoute.getRecord();
            
            // Get busID from schedule
            Schedule schedule = new Schedule();
            schedule.scheduleID = scheduleID;
            schedule.getRecord();
            
            // Set up the service's ticket with ALL required fields
            service.newTicket.routeID = routeID;
            service.newTicket.scheduleID = scheduleID;
            service.newTicket.busID = schedule.busID;  // FIX: Get from schedule
            service.newTicket.type = ticketType;
            service.newTicket.departureDate = request.getParameter("departureDate"); // FIX: Get from form
            service.newTicket.staffID = 1; // FIX: Default staff or get from session
            service.newTicket.discount = ticketType.equals("Discounted") ? 20.0 : 0.0; // FIX: Set discount
            service.newTicket.finalAmount = service.calculatePrice(
                service.selectedRoute.baseFare, 
                ticketType, 
                service.newTicket.discount
            );
            
            if(service.confirmPurchase() == 1) {
                request.setAttribute(
                    "success", "Ticket purchased successfully!");
                request.setAttribute("ticket", service.newTicket);
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

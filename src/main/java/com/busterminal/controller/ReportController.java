package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.reports.*;
import java.io.IOException;

@WebServlet("/report")
public class ReportController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type");
        
        if ("revenue".equals(type)) 
            revenueForm(request, response);
        else if ("route".equals(type)) 
            routePerformanceForm(request, response);
        else if ("payment".equals(type)) 
            dailyPaymentForm(request, response);
        else if ("utilization".equals(type)) 
            busUtilizationForm(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type");
        
        if ("revenue".equals(type)) 
            generateRevenueReport(request, response);
        else if ("route".equals(type)) 
            generateRoutePerformanceReport(request, response);
        else if ("payment".equals(type)) 
            generateDailyPaymentReport(request, response);
        else if ("utilization".equals(type)) 
            generateBusUtilizationReport(request, response);
    }
    
    private void revenueForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/admin/dashboard.jsp")
            .forward(request, response);
    }
    
    private void generateRevenueReport(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            int year = Integer.parseInt(request.getParameter("year"));
            int month = Integer.parseInt(request.getParameter("month"));
            
            RevenueReport report = new RevenueReport();
            report.reportYear = year;
            report.reportMonth = month;
            report.generateReport();
            
            request.setAttribute("report", report);
            request.setAttribute("reportType", "revenue");
            request.getRequestDispatcher("/admin/dashboard.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error generating report: " + 
                e.getMessage());
            request.getRequestDispatcher("/admin/dashboard.jsp")
                .forward(request, response);
        }
    }
    
    private void routePerformanceForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/admin/dashboard.jsp")
            .forward(request, response);
    }
    
    private void generateRoutePerformanceReport(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
        try {
            int year = Integer.parseInt(request.getParameter("year"));
            int month = Integer.parseInt(request.getParameter("month"));
            
            RoutePerformanceReport report = new RoutePerformanceReport();
            report.reportYear = year;
            report.reportMonth = month;
            report.generateReport();
            
            request.setAttribute("report", report);
            request.setAttribute("reportType", "route");
            request.getRequestDispatcher("/admin/dashboard.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error generating report: " + 
                e.getMessage());
            request.getRequestDispatcher("/admin/dashboard.jsp")
                .forward(request, response);
        }
    }
    
    private void dailyPaymentForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/admin/dashboard.jsp")
            .forward(request, response);
    }
    
    private void generateDailyPaymentReport(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            int year = Integer.parseInt(request.getParameter("year"));
            int month = Integer.parseInt(request.getParameter("month"));
            
            DailyPaymentReport report = new DailyPaymentReport();
            report.reportYear = year;
            report.reportMonth = month;
            report.generateReport();
            
            request.setAttribute("report", report);
            request.setAttribute("reportType", "payment");
            request.getRequestDispatcher("/admin/dashboard.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error generating report: " + 
                e.getMessage());
            request.getRequestDispatcher("/admin/dashboard.jsp")
                .forward(request, response);
        }
    }
    
    private void busUtilizationForm(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/admin/dashboard.jsp")
            .forward(request, response);
    }
    
    private void generateBusUtilizationReport(HttpServletRequest request, 
        HttpServletResponse response) throws ServletException, IOException {
        try {
            int year = Integer.parseInt(request.getParameter("year"));
            int month = Integer.parseInt(request.getParameter("month"));
            
            BusUtilizationReport report = new BusUtilizationReport();
            report.reportYear = year;
            report.reportMonth = month;
            report.generateReport();
            
            request.setAttribute("report", report);
            request.setAttribute("reportType", "utilization");
            request.getRequestDispatcher("/admin/dashboard.jsp")
                .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error generating report: " + 
                e.getMessage());
            request.getRequestDispatcher("/admin/dashboard.jsp")
                .forward(request, response);
        }
    }
}

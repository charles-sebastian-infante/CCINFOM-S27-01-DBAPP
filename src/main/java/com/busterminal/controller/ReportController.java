package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.service.ReportService;
import com.busterminal.model.*;
import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

@WebServlet("/reports")
public class ReportController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String reportType = request.getParameter("type");
        
        try {
            if ("tripSchedule".equals(reportType)) {
                tripScheduleReportForm(request, response);
            } else if ("routeUsage".equals(reportType)) {
                routeUsageReportForm(request, response);
            } else if ("maintenanceSummary".equals(reportType)) {
                maintenanceSummaryReportForm(request, response);
            } else if ("busUtilization".equals(reportType)) {
                busUtilizationReportForm(request, response);
            } else {
                reportsDashboard(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading report form: " + e.getMessage());
            request.getRequestDispatcher("/admin/reports_dashboard.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String reportType = request.getParameter("type");
        
        try {
            if ("tripSchedule".equals(reportType)) {
                generateTripScheduleReport(request, response);
            } else if ("routeUsage".equals(reportType)) {
                generateRouteUsageReport(request, response);
            } else if ("maintenanceSummary".equals(reportType)) {
                generateMaintenanceSummaryReport(request, response);
            } else if ("busUtilization".equals(reportType)) {
                generateBusUtilizationReport(request, response);
            } else {
                reportsDashboard(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error generating report: " + e.getMessage());
            request.getRequestDispatcher("/admin/reports_dashboard.jsp").forward(request, response);
        }
    }
    
    /**
     * Main reports dashboard
     */
    private void reportsDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH) + 1;
            int currentYear = cal.get(Calendar.YEAR);
            
            request.setAttribute("currentMonth", currentMonth);
            request.setAttribute("currentYear", currentYear);
            
            request.getRequestDispatcher("/admin/reports_dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading dashboard: " + e.getMessage());
            request.getRequestDispatcher("/admin/reports_dashboard.jsp").forward(request, response);
        }
    }
       
    private void tripScheduleReportForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String today = sdf.format(new Date());
            
            request.setAttribute("date", today);
            request.getRequestDispatcher("/admin/trip_schedule_report_form.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading form: " + e.getMessage());
            request.getRequestDispatcher("/admin/reports_dashboard.jsp").forward(request, response);
        }
    }
    
    private void generateTripScheduleReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String terminalIDParam = request.getParameter("terminalID");
            String date = request.getParameter("date");
            
            // Validate inputs
            if (terminalIDParam == null || terminalIDParam.isEmpty() || 
                date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new IllegalArgumentException("Invalid terminal ID or date format");
            }
            
            int terminalID = Integer.parseInt(terminalIDParam);
            
            // Generate report
            Map<String, Object> reportData = ReportService.getDailyScheduleByTerminal(terminalID, date);
            
            request.setAttribute("reportData", reportData);
            request.setAttribute("date", date);
            request.getRequestDispatcher("/admin/trip_schedule_report.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error generating report: " + e.getMessage());
            request.getRequestDispatcher("/admin/trip_schedule_report_form.jsp").forward(request, response);
        }
    }
     
    private void routeUsageReportForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH) + 1;
            int currentYear = cal.get(Calendar.YEAR);
            
            request.setAttribute("month", currentMonth);
            request.setAttribute("year", currentYear);
            request.getRequestDispatcher("/admin/route_usage_report_form.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading form: " + e.getMessage());
            request.getRequestDispatcher("/admin/reports_dashboard.jsp").forward(request, response);
        }
    }
    
    private void generateRouteUsageReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String yearStr = request.getParameter("year");
            String monthStr = request.getParameter("month");
            
            // Validate and parse parameters
            int year, month;
            try {
                year = Integer.parseInt(yearStr);
                month = Integer.parseInt(monthStr);
                
                if (year < 1900 || year > 2100 || month < 1 || month > 12) {
                    throw new IllegalArgumentException("Invalid year or month");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Year and month must be numeric");
            }
            
            // Generate report
            Map<String, Object> reportData = ReportService.getRouteUsageAnalysis(year, month);
            
            request.setAttribute("reportData", reportData);
            request.setAttribute("year", year);
            request.setAttribute("month", month);
            request.getRequestDispatcher("/admin/route_usage_report.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error generating report: " + e.getMessage());
            request.getRequestDispatcher("/admin/route_usage_report_form.jsp").forward(request, response);
        }
    }
     
    private void maintenanceSummaryReportForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH) + 1;
            int currentYear = cal.get(Calendar.YEAR);
            
            request.setAttribute("month", currentMonth);
            request.setAttribute("year", currentYear);
            request.getRequestDispatcher("/admin/maintenance_summary_report_form.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading form: " + e.getMessage());
            request.getRequestDispatcher("/admin/reports_dashboard.jsp").forward(request, response);
        }
    }
    
    private void generateMaintenanceSummaryReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String yearStr = request.getParameter("year");
            String monthStr = request.getParameter("month");
            
            // Validate and parse parameters
            int year, month;
            try {
                year = Integer.parseInt(yearStr);
                month = Integer.parseInt(monthStr);
                
                if (year < 1900 || year > 2100 || month < 1 || month > 12) {
                    throw new IllegalArgumentException("Invalid year or month");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Year and month must be numeric");
            }
            
            // Generate report
            Map<String, Object> reportData = ReportService.getMaintenanceCostReport(year, month);
            
            request.setAttribute("reportData", reportData);
            request.setAttribute("year", year);
            request.setAttribute("month", month);
            request.getRequestDispatcher("/admin/maintenance_summary_report.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error generating report: " + e.getMessage());
            request.getRequestDispatcher("/admin/maintenance_summary_report_form.jsp").forward(request, response);
        }
    }
     
    private void busUtilizationReportForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            Calendar cal = Calendar.getInstance();
            int currentMonth = cal.get(Calendar.MONTH) + 1;
            int currentYear = cal.get(Calendar.YEAR);
            
            request.setAttribute("month", currentMonth);
            request.setAttribute("year", currentYear);
            request.getRequestDispatcher("/admin/bus_utilization_report_form.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading form: " + e.getMessage());
            request.getRequestDispatcher("/admin/reports_dashboard.jsp").forward(request, response);
        }
    }
    
    private void generateBusUtilizationReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String yearStr = request.getParameter("year");
            String monthStr = request.getParameter("month");
            
            // Validate and parse parameters
            int year, month;
            try {
                year = Integer.parseInt(yearStr);
                month = Integer.parseInt(monthStr);
                
                if (year < 1900 || year > 2100 || month < 1 || month > 12) {
                    throw new IllegalArgumentException("Invalid year or month");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Year and month must be numeric");
            }
            
            // Generate report
            Map<String, Object> reportData = ReportService.getBusUtilizationReport(year, month);
            
            request.setAttribute("reportData", reportData);
            request.setAttribute("year", year);
            request.setAttribute("month", month);
            request.getRequestDispatcher("/admin/bus_utilization_report.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error generating report: " + e.getMessage());
            request.getRequestDispatcher("/admin/bus_utilization_report_form.jsp").forward(request, response);
        }
    }
}

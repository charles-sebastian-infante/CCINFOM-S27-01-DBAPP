<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="com.busterminal.model.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Reports - Bus Terminal Management</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/style/global.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/style/manage_reports.css">
    <style>
        .error { color: red; background: #ffe6e6; padding: 10px; margin: 10px 0; border-radius: 5px; }
        .success { color: green; background: #e6ffe6; padding: 10px; margin: 10px 0; border-radius: 5px; }
    </style>
</head>
<body>
    <h1>Reports Dashboard</h1>

    <% if(request.getAttribute("error") != null) { %>
        <div class="error"><%= request.getAttribute("error") %></div>
    <% } %>

    <% if(request.getAttribute("success") != null) { %>
        <div class="success"><%= request.getAttribute("success") %></div>
    <% } %>

    <% 
        String reportType = request.getParameter("type");
        Map<String, Object> reportData = (Map<String, Object>) request.getAttribute("reportData");
        
        // Show report form or results based on context
        if (reportData != null && "tripSchedule".equals(reportType)) {
            // Display Trip Schedule Report Results for All Terminals
            Map<String, Map<String, Object>> terminals = (Map<String, Map<String, Object>>) reportData.get("terminals");
    %>
        <h2>Trip Schedule Report - All Terminals</h2>
        <a href="<%= request.getContextPath() %>/reports" class="back-btn">← Back to Reports</a>
        
        <div class="summary-box">
            <strong>Date:</strong> <%= reportData.get("date") %>
        </div>

        <% 
            if (terminals != null && !terminals.isEmpty()) {
                for (Map.Entry<String, Map<String, Object>> entry : terminals.entrySet()) {
                    String terminalName = entry.getKey();
                    Map<String, Object> terminalData = entry.getValue();
                    List<Map<String, Object>> schedules = (List<Map<String, Object>>) terminalData.get("schedules");
                    
                    if (schedules != null && !schedules.isEmpty()) {
        %>
                        <h3 style="margin-top:30px; color:#2c3e50;"><%= terminalName %></h3>
                        <div class="summary-box" style="margin-bottom:15px;">
                            <strong>Departed:</strong> <%= terminalData.get("total_departures") %> | 
                            <strong>Arrived:</strong> <%= terminalData.get("total_arrivals") %>
                        </div>
                        
                        <table>
                            <thead>
                                <tr>
                                    <th>Type</th>
                                    <th>Time</th>
                                    <th>Bus Number</th>
                                    <th>Route</th>
                                    <th>Origin/Destination</th>
                                    <th>Status</th>
                                    <th>Passengers</th>
                                </tr>
                            </thead>
                            <tbody>
                            <% for (Map<String, Object> schedule : schedules) { 
                                String type = (String) schedule.get("type");
                                String status = (String) schedule.get("status");
                                String statusClass = "status-" + status.toLowerCase().replace(" ", "-");
                            %>
                                <tr>
                                    <td><%= type %></td>
                                    <td>
                                        <% if ("Departure".equals(type)) { %>
                                            <strong>Departs:</strong> <%= schedule.get("departure_time") %>
                                        <% } else { %>
                                            <strong>Arrives:</strong> <%= schedule.get("arrival_time") %>
                                        <% } %>
                                    </td>
                                    <td><%= schedule.get("bus_number") %></td>
                                    <td><%= schedule.get("route_name") %></td>
                                    <td>
                                        <% if ("Departure".equals(type)) { %>
                                            → <%= schedule.get("destination") %>
                                        <% } else { %>
                                            ← <%= schedule.get("origin") %>
                                        <% } %>
                                    </td>
                                    <td class="<%= statusClass %>"><%= status %></td>
                                    <td><%= schedule.get("passenger_count") %></td>
                                </tr>
                            <% } %>
                            </tbody>
                        </table>
        <%          }
                }
            } else { %>
                <p>No schedules found for the selected date.</p>
        <%  } %>

    <% } else if ("tripSchedule".equals(reportType)) {
        // Show Trip Schedule Report Form
    %>
        <h2>Trip Schedule Report - Select Date</h2>
        <a href="<%= request.getContextPath() %>/reports" class="back-btn">← Back to Reports</a>
        
        <div class="form-block">
            <form method="POST" action="<%= request.getContextPath() %>/reports">
                <input type="hidden" name="type" value="tripSchedule">
                
                <label>Date:</label>
                <input type="date" name="date" value="<%= request.getAttribute("date") != null ? request.getAttribute("date") : "" %>" required>
                
                <br><button type="submit">Generate Report</button>
            </form>
        </div>

    <% } else if (reportData != null && "routeUsage".equals(reportType)) {
        // Display Route Usage Report Results
    %>
        <h2>Route Usage Analysis Report</h2>
        <a href="<%= request.getContextPath() %>/reports" class="back-btn">← Back to Reports</a>
        
        <div class="summary-box">
            <strong>Period:</strong> <%= reportData.get("month") %>/<%= reportData.get("year") %>
        </div>

        <% 
            List<Map<String, Object>> routeUsage = (List<Map<String, Object>>) reportData.get("route_usage");
            if (routeUsage != null && !routeUsage.isEmpty()) {
        %>
            <table>
                <thead>
                    <tr>
                        <th>Route Name</th>
                        <th>Origin</th>
                        <th>Destination</th>
                        <th>Passengers</th>
                        <th>Total Revenue</th>
                    </tr>
                </thead>
                <tbody>
                <% for (Map<String, Object> route : routeUsage) { %>
                    <tr>
                        <td><%= route.get("route_name") %></td>
                        <td><%= route.get("origin") %></td>
                        <td><%= route.get("destination") %></td>
                        <td><%= route.get("passenger_count") %></td>
                        <td>₱<%= String.format("%.2f", route.get("total_revenue")) %></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } else { %>
            <p>No route usage data found for this period.</p>
        <% } %>

    <% } else if ("routeUsage".equals(reportType)) {
        // Show Route Usage Report Form
    %>
        <h2>Route Usage Analysis - Select Period</h2>
        <a href="<%= request.getContextPath() %>/reports" class="back-btn">← Back to Reports</a>
        
        <div class="form-block">
            <form method="POST" action="<%= request.getContextPath() %>/reports">
                <input type="hidden" name="type" value="routeUsage">
                
                <label>Year:</label>
                <input type="number" name="year" min="2020" max="2100" value="<%= request.getAttribute("year") != null ? request.getAttribute("year") : java.time.Year.now().getValue() %>" required>
                
                <label>Month:</label>
                <select name="month" required>
                    <% 
                        String[] months = {"January", "February", "March", "April", "May", "June", 
                                          "July", "August", "September", "October", "November", "December"};
                        int currentMonth = request.getAttribute("month") != null ? (Integer)request.getAttribute("month") : java.time.LocalDate.now().getMonthValue();
                        for (int i = 1; i <= 12; i++) {
                    %>
                        <option value="<%= i %>" <%= i == currentMonth ? "selected" : "" %>><%= months[i-1] %></option>
                    <% } %>
                </select>
                
                <br><button type="submit">Generate Report</button>
            </form>
        </div>

    <% } else if (reportData != null && "maintenanceSummary".equals(reportType)) {
        // Display Maintenance Summary Report Results
    %>
        <h2>Maintenance Cost Summary Report</h2>
        <a href="<%= request.getContextPath() %>/reports" class="back-btn">← Back to Reports</a>
        
        <div class="summary-box">
            <strong>Period:</strong> <%= reportData.get("month") %>/<%= reportData.get("year") %><br>
        </div>

        <% 
            List<Map<String, Object>> maintenanceByDay = (List<Map<String, Object>>) reportData.get("maintenance_by_day");
            if (maintenanceByDay != null && !maintenanceByDay.isEmpty()) {
                double totalCost = 0;
                int totalCount = 0;
                for (Map<String, Object> day : maintenanceByDay) {
                    totalCost += (Double) day.get("daily_cost");
                    totalCount += (Integer) day.get("maintenance_count");
                }
        %>
            <div class="summary-box">
                <strong>Total Maintenance Activities:</strong> <%= totalCount %><br>
                <strong>Total Cost:</strong> ₱<%= String.format("%.2f", totalCost) %>
            </div>

            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Maintenance Count</th>
                        <th>Total Cost</th>
                    </tr>
                </thead>
                <tbody>
                <% for (Map<String, Object> day : maintenanceByDay) { %>
                    <tr>
                        <td><%= day.get("date") %></td>
                        <td><%= day.get("maintenance_count") %></td>
                        <td>₱<%= String.format("%.2f", day.get("daily_cost")) %></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } else { %>
            <p>No maintenance activities found for this period.</p>
        <% } %>

    <% } else if ("maintenanceSummary".equals(reportType)) {
        // Show Maintenance Summary Report Form
    %>
        <h2>Maintenance Cost Summary - Select Period</h2>
        <a href="<%= request.getContextPath() %>/reports" class="back-btn">← Back to Reports</a>
        
        <div class="form-block">
            <form method="POST" action="<%= request.getContextPath() %>/reports">
                <input type="hidden" name="type" value="maintenanceSummary">
                
                <label>Year:</label>
                <input type="number" name="year" min="2020" max="2100" value="<%= request.getAttribute("year") != null ? request.getAttribute("year") : java.time.Year.now().getValue() %>" required>
                
                <label>Month:</label>
                <select name="month" required>
                    <% 
                        String[] months = {"January", "February", "March", "April", "May", "June", 
                                          "July", "August", "September", "October", "November", "December"};
                        int currentMonth = request.getAttribute("month") != null ? (Integer)request.getAttribute("month") : java.time.LocalDate.now().getMonthValue();
                        for (int i = 1; i <= 12; i++) {
                    %>
                        <option value="<%= i %>" <%= i == currentMonth ? "selected" : "" %>><%= months[i-1] %></option>
                    <% } %>
                </select>
                
                <br><button type="submit">Generate Report</button>
            </form>
        </div>

    <% } else if (reportData != null && "busUtilization".equals(reportType)) {
        // Display Bus Utilization Report Results
    %>
        <h2>Bus Utilization Report</h2>
        <a href="<%= request.getContextPath() %>/reports" class="back-btn">← Back to Reports</a>
        
        <div class="summary-box">
            <strong>Period:</strong> <%= reportData.get("month") %>/<%= reportData.get("year") %><br>
        </div>

        <% 
            List<Map<String, Object>> busUtilization = (List<Map<String, Object>>) reportData.get("bus_utilization");
            if (busUtilization != null && !busUtilization.isEmpty()) {
        %>
            <table>
                <thead>
                    <tr>
                        <th>Bus Number</th>
                        <th>Total Trips</th>
                        <th>Total Passengers</th>
                        <th>Avg Passengers/Trip</th>
                    </tr>
                </thead>
                <tbody>
                <% for (Map<String, Object> bus : busUtilization) { 
                    int tripCount = (Integer) bus.get("trip_count");
                    int passengerCount = (Integer) bus.get("passenger_count");
                    double avgPassengers = tripCount > 0 ? (double) passengerCount / tripCount : 0;
                %>
                    <tr>
                        <td><%= bus.get("bus_number") %></td>
                        <td><%= tripCount %></td>
                        <td><%= passengerCount %></td>
                        <td><%= String.format("%.1f", avgPassengers) %></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        <% } else { %>
            <p>No bus utilization data found for this period.</p>
        <% } %>

    <% } else if ("busUtilization".equals(reportType)) {
        // Show Bus Utilization Report Form
    %>
        <h2>Bus Utilization Report - Select Period</h2>
        <a href="<%= request.getContextPath() %>/reports" class="back-btn">← Back to Reports</a>
        
        <div class="form-block">
            <form method="POST" action="<%= request.getContextPath() %>/reports">
                <input type="hidden" name="type" value="busUtilization">
                
                <label>Year:</label>
                <input type="number" name="year" min="2020" max="2100" value="<%= request.getAttribute("year") != null ? request.getAttribute("year") : java.time.Year.now().getValue() %>" required>
                
                <label>Month:</label>
                <select name="month" required>
                    <% 
                        String[] months = {"January", "February", "March", "April", "May", "June", 
                                          "July", "August", "September", "October", "November", "December"};
                        int currentMonth = request.getAttribute("month") != null ? (Integer)request.getAttribute("month") : java.time.LocalDate.now().getMonthValue();
                        for (int i = 1; i <= 12; i++) {
                    %>
                        <option value="<%= i %>" <%= i == currentMonth ? "selected" : "" %>><%= months[i-1] %></option>
                    <% } %>
                </select>
                
                <br><button type="submit">Generate Report</button>
            </form>
        </div>

    <% } else {
        // Show Reports Dashboard
    %>
        <div class="report-grid">
            <div class="report-card">
                <h3>📅 Trip Schedule Report</h3>
                <p>View all departures and arrivals for a specific terminal on a given day. Shows bus information, routes, times, and passenger counts.</p>
                <a href="<%= request.getContextPath() %>/reports?type=tripSchedule">Generate Report</a>
            </div>

            <div class="report-card">
                <h3>🚌 Route Usage Report</h3>
                <p>Analyze route utilization and performance metrics for a specific month.</p>
                <a href="<%= request.getContextPath() %>/reports?type=routeUsage">Generate Report</a>
            </div>

            <div class="report-card">
                <h3>🔧 Maintenance Summary</h3>
                <p>View maintenance costs and activities summary for a specific month.</p>
                <a href="<%= request.getContextPath() %>/reports?type=maintenanceSummary">Generate Report</a>
            </div>

            <div class="report-card">
                <h3>📊 Bus Utilization Report</h3>
                <p>Track bus utilization and efficiency metrics for a specific month.</p>
                <a href="<%= request.getContextPath() %>/reports?type=busUtilization">Generate Report</a>
            </div>
        </div>
    <% } %>

</body>

</html>

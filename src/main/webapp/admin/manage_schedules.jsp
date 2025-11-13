<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.busterminal.model.Bus" %>
<%@ page import="com.busterminal.model.Route" %>
<%@ page import="com.busterminal.model.Schedule" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Manage Schedules</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
        th { background: #f4f4f4; }
        .form-block { margin: 20px 0; padding: 12px; border: 1px solid #ddd; background:#fafafa; }
        .btn { padding:6px 10px; margin-right:6px; text-decoration:none; display:inline-block; }
        .btn-delete { background:#e74c3c; color:#fff; border:none; cursor:pointer; }
        .btn-edit { background:#3498db; color:#fff; border:none; }
        .btn-view { background:#2ecc71; color:#fff; border:none; }
        .status-scheduled { color: #3498db; font-weight: bold; }
        .status-departed { color: #f39c12; font-weight: bold; }
        .status-completed { color: #2ecc71; font-weight: bold; }
        .status-cancelled { color: #e74c3c; font-weight: bold; }
        .filter-section { background: #f9f9f9; padding: 10px; margin: 10px 0; border: 1px solid #ddd; }
    </style>
</head>
<body>
    <h1>Manage Schedules</h1>

    <% if(request.getAttribute("success") != null) { %>
        <p style="color:green;"><%= request.getAttribute("success") %></p>
    <% } %>

    <% if(request.getAttribute("error") != null) { %>
        <p style="color:red;"><%= request.getAttribute("error") %></p>
    <% } %>

    <!-- Create Schedule Form -->
    <div class="form-block">
        <h2>Create New Schedule</h2>
        <% 
            List<Bus> buses = (List<Bus>) request.getAttribute("buses");
            List<Route> routes = (List<Route>) request.getAttribute("routes");
            
            // Fallback: Load if not provided
            if (buses == null) {
                buses = Bus.getAvailableBuses();
            }
            if (routes == null) {
                routes = Route.getAllRoutes();
            }
        %>
        <form method="POST" action="<%= request.getContextPath() %>/schedule">
            <input type="hidden" name="action" value="create">

            <label>Bus:<br>
                <select name="busID" required style="width:300px;">
                    <option value="">-- Select Bus --</option>
                    <% if (buses != null) {
                        for (Bus bus : buses) { %>
                            <option value="<%= bus.busID %>">
                                <%= bus.busNumber %> (Capacity: <%= bus.capacity %>) - <%= bus.status %>
                            </option>
                    <%  }
                    } %>
                </select>
            </label><br><br>

            <label>Route:<br>
                <select name="routeID" required style="width:300px;">
                    <option value="">-- Select Route --</option>
                    <% if (routes != null) {
                        for (Route route : routes) { %>
                            <option value="<%= route.routeID %>">
                                <%= route.routeName %> (<%= route.distance %> km)
                            </option>
                    <%  }
                    } %>
                </select>
            </label><br><br>

            <label>Departure Date:<br>
                <input type="date" name="departureDate" required>
            </label>
            <label>Departure Time:<br>
                <input type="time" name="departureTime" required>
            </label><br><br>

            <label>Arrival Date:<br>
                <input type="date" name="arrivalDate" required>
            </label>
            <label>Arrival Time:<br>
                <input type="time" name="arrivalTime" required>
            </label><br><br>

            <button type="submit" class="btn" style="background:#2ecc71;color:#fff;">Create Schedule</button>
            <a href="<%= request.getContextPath() %>/schedule?action=list" class="btn" style="background:#95a5a6;color:#fff;">View All Schedules</a>
        </form>
    </div>

    <!-- Schedules List -->
    <% if(request.getAttribute("schedules") != null) {
        List<Map<String, Object>> schedules = (List<Map<String, Object>>) request.getAttribute("schedules");
        
        if(schedules.isEmpty()) {
    %>
        <p>No schedules found.</p>
    <%  } else { %>
        <h2>All Schedules</h2>
        
        <!-- Filter Section -->
        <div class="filter-section">
            <form method="GET" action="<%= request.getContextPath() %>/schedule" style="display:inline;">
                <input type="hidden" name="action" value="list">
                <label>Filter by Status:
                    <select name="status" onchange="this.form.submit()">
                        <option value="">All Statuses</option>
                        <option value="Scheduled" <%= "Scheduled".equals(request.getAttribute("statusFilter")) ? "selected" : "" %>>Scheduled</option>
                        <option value="Departed" <%= "Departed".equals(request.getAttribute("statusFilter")) ? "selected" : "" %>>Departed</option>
                        <option value="Completed" <%= "Completed".equals(request.getAttribute("statusFilter")) ? "selected" : "" %>>Completed</option>
                        <option value="Cancelled" <%= "Cancelled".equals(request.getAttribute("statusFilter")) ? "selected" : "" %>>Cancelled</option>
                    </select>
                </label>
                
                <label style="margin-left:20px;">Filter by Date:
                    <input type="date" name="date" value="<%= request.getAttribute("dateFilter") != null ? request.getAttribute("dateFilter") : "" %>" onchange="this.form.submit()">
                </label>
                
                <button type="submit" class="btn" style="background:#3498db;color:#fff;">Apply Filter</button>
                <a href="<%= request.getContextPath() %>/schedule?action=list" class="btn" style="background:#95a5a6;color:#fff;">Clear Filter</a>
            </form>
        </div>
        
        <table>
            <thead>
                <tr>
                    <th>Schedule ID</th>
                    <th>Bus Number</th>
                    <th>Route</th>
                    <th>Origin</th>
                    <th>Destination</th>
                    <th>Departure</th>
                    <th>Arrival</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
            <% for(Map<String, Object> schedule : schedules) { 
                String status = (String) schedule.get("status");
                String statusClass = "status-" + status.toLowerCase();
            %>
                <tr>
                    <td><%= schedule.get("schedule_id") %></td>
                    <td><%= schedule.get("bus_number") %></td>
                    <td><%= schedule.get("route_name") %></td>
                    <td><%= schedule.get("origin_terminal") %></td>
                    <td><%= schedule.get("destination_terminal") %></td>
                    <td><%= schedule.get("departure_time") %></td>
                    <td><%= schedule.get("arrival_time") %></td>
                    <td class="<%= statusClass %>"><%= status %></td>
                    <td>
                        <% if (!"Departed".equals(status) && !"Completed".equals(status)) { %>
                            <a class="btn btn-edit" href="<%= request.getContextPath() %>/schedule?action=edit&id=<%= schedule.get("schedule_id") %>">Edit</a>
                        <% } %>
                        
                        <% if ("Scheduled".equals(status)) { %>
                            <form method="POST" action="<%= request.getContextPath() %>/schedule" style="display:inline;" 
                                  onsubmit="return confirm('Delete this schedule?') ? true : false;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="<%= schedule.get("schedule_id") %>">
                                <button type="submit" class="btn btn-delete">Delete</button>
                            </form>
                        <% } %>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    <%  }
    } %>

</body>
</html>

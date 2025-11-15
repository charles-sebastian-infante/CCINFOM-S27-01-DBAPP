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

</head>
<body>
    <h1>Manage Schedules</h1>
    <link rel="stylesheet" href="style/manage_schedules.css">
    <link rel="stylesheet" href="style/global.css">
    <% if(request.getAttribute("success") != null) { %>
        <p style="color:green;"><%= request.getAttribute("success") %></p>
    <% } %>

    <% if(request.getAttribute("error") != null) { %>
        <p style="color:red;"><%= request.getAttribute("error") %></p>
    <% } %>

    <% 
        // Display validation errors
        Map<String, String> validationErrors = (Map<String, String>) request.getAttribute("validationErrors");
        if (validationErrors != null && !validationErrors.isEmpty()) {
    %>
        <div style="color:red; background-color:#ffe6e6; padding:10px; margin:10px 0; border-radius:5px;">
            <strong>Validation Errors:</strong>
            <ul>
            <% for (Map.Entry<String, String> error : validationErrors.entrySet()) { %>
                <li><strong><%= error.getKey() %>:</strong> <%= error.getValue() %></li>
            <% } %>
            </ul>
        </div>
    <% } %>

    <!-- Create Schedule Form -->
    <div class="form-block">
        <h2>Create New Schedule</h2>
        <% 
            List<Bus> buses = (List<Bus>) request.getAttribute("buses");
            List<Route> routes = (List<Route>) request.getAttribute("routes");
            com.busterminal.model.Terminal terminal = null;
            
            // Fallback: Load if not provided
            if (buses == null) {
                buses = Bus.getAvailableBuses();
            }
            if (routes == null) {
                routes = Route.getAllRoutes();
            }
        %>
        <form method="POST" action="<%= request.getContextPath() %>/schedule" id="scheduleForm">
            <input type="hidden" name="action" value="create">

            <label>Bus:<br>
                <select name="busID" required style="width:400px;">
                    <option value="">-- Select Bus --</option>
                    <% if (buses != null) {
                        for (Bus bus : buses) { 
                            terminal = new com.busterminal.model.Terminal();
                            terminal.terminalID = bus.currentTerminal;
                            terminal.getRecord();
                    %>
                            <option value="<%= bus.busID %>">
                                <%= bus.busNumber %> (Capacity: <%= bus.capacity %>) - <%= bus.status %> - Terminal: <%= terminal.terminalName %>
                            </option>
                    <%  }
                    } %>
                </select>
            </label><br><br>

            <label>Route:<br>
                <select name="routeID" id="routeSelect" required style="width:400px;">
                    <option value="">-- Select Route --</option>
                    <% if (routes != null) {
                        for (Route route : routes) { %>
                            <option value="<%= route.routeID %>" data-travel-time="<%= route.travelTime %>">
                                <%= route.routeName %> (<%= route.distance %> km, Travel: <%= route.travelTime %>)
                            </option>
                    <%  }
                    } %>
                </select>
            </label><br><br>

            <label>Departure Date:<br>
                <input type="date" name="departureDate" id="departureDate" required>
            </label>
            <label>Departure Time:<br>
                <input type="time" name="departureTime" id="departureTime" required>
            </label><br><br>

            <label>Arrival Date: <span style="color:#666;">(Auto)</span><br>
                <input type="date" name="arrivalDate" id="arrivalDate" required readonly style="background:#f0f0f0;">
            </label>
            <label>Arrival Time: <span style="color:#666;">(Auto)</span><br>
                <input type="time" name="arrivalTime" id="arrivalTime" required readonly style="background:#f0f0f0;">
            </label><br><br>

            <button type="submit" class="btn" style="background:#2ecc71;color:#fff;">Create Schedule</button>
            <a href="<%= request.getContextPath() %>/schedule?action=list" class="btn" style="background:#95a5a6;color:#fff;">View All Schedules</a>
        </form>

        <script>
            // Auto-calculate arrival date/time based on route's travel time
            function calculateArrival() {
                const routeSelect = document.getElementById('routeSelect');
                const departureDate = document.getElementById('departureDate').value;
                const departureTime = document.getElementById('departureTime').value;
                
                if (!routeSelect.value || !departureDate || !departureTime) {
                    return;
                }
                
                // Get selected route's travel time
                const selectedOption = routeSelect.options[routeSelect.selectedIndex];
                const travelTimeStr = selectedOption.getAttribute('data-travel-time');
                
                if (!travelTimeStr) {
                    return;
                }
                
                // Parse travel time (format: HH:mm:ss)
                const travelTimeParts = travelTimeStr.split(':');
                const travelHours = parseInt(travelTimeParts[0]);
                const travelMinutes = parseInt(travelTimeParts[1]);
                
                // Parse departure datetime
                const departureDateTime = new Date(departureDate + 'T' + departureTime);
                
                // Add travel time
                departureDateTime.setHours(departureDateTime.getHours() + travelHours);
                departureDateTime.setMinutes(departureDateTime.getMinutes() + travelMinutes);
                
                // Format arrival date and time
                const arrivalDate = departureDateTime.toISOString().split('T')[0];
                const arrivalTime = departureDateTime.toTimeString().split(' ')[0].substring(0, 5);
                
                // Set arrival fields
                document.getElementById('arrivalDate').value = arrivalDate;
                document.getElementById('arrivalTime').value = arrivalTime;
            }
            
            // Add event listeners
            document.getElementById('routeSelect').addEventListener('change', calculateArrival);
            document.getElementById('departureDate').addEventListener('change', calculateArrival);
            document.getElementById('departureTime').addEventListener('change', calculateArrival);
        </script>
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

    <a href="<%= request.getContextPath() %>/" class="fixed-button">Back to Home</a>

</body>
</html>

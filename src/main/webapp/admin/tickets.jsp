<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.busterminal.model.Ticket" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Manage Tickets</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/style/tickets.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/style/global.css">

    <script>
        function confirmDelete(form) {
            if (confirm('Are you sure you want to delete this ticket?')) {
                form.submit();
            }
            return false;
        }
    </script>
</head>
<body>
<h1>Manage Tickets</h1>

    <% if(request.getAttribute("message") != null) { %>
    <p style="color:green;"><%= request.getAttribute("message") %></p>
    <% } %>

    <% if(request.getAttribute("error") != null) { %>
    <p style="color:red;"><%= request.getAttribute("error") %></p>
    <% } %>

    <% if(request.getAttribute("editTicket") != null) {
    Map<String, Object> ticket = (Map<String, Object>) request.getAttribute("editTicket");
    List<Map<String, Object>> schedules = (List<Map<String, Object>>) request.getAttribute("scheduleList");
%>
    <div class="form-block">
        <h2>Edit Ticket #<%= ticket.get("ticket_number") %></h2>
        <form method="POST" action="<%= request.getContextPath() %>/ticket">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="<%= ticket.get("ticket_id") %>">

            <label>Ticket Number:<br>
                <input type="text" name="ticketNumber" value="<%= ticket.get("ticket_number") %>" readonly>
            </label><br><br>

            <label>Schedule:<br>
                <select name="scheduleID" required>
                    <option value="">-- Select Schedule --</option>
                    <% if (schedules != null) {
                    for (Map<String, Object> s : schedules) {
                    int availableSeats = (Integer) s.get("available_seats");
                    boolean isFull = availableSeats <= 0 && !s.get("schedule_id").equals(ticket.get("schedule_id"));
                    boolean isSelected = s.get("schedule_id").equals(ticket.get("schedule_id"));
                    %>
                    <option value="<%= s.get("schedule_id") %>"
                    <%= isSelected ? "selected" : (isFull ? "disabled" : "") %>>
                    Schedule #<%= s.get("schedule_id") %> - Bus <%= s.get("bus_number") %> - Route <%= s.get("route_name") %> - <%= s.get("departure_time") %>
                    (<%= isFull ? "Full" : availableSeats + " seats available" %>)
                    </option>
                    <%  }
                    } %>
                </select>
            </label><br><br>

            <label>Discounted:<br>
                <select name="discounted">
                    <option value="false" <%= !(Boolean) ticket.get("discounted") ? "selected" : "" %>>No</option>
                    <option value="true" <%= (Boolean) ticket.get("discounted") ? "selected" : "" %>>Yes</option>
                </select>
            </label><br><br>

            <button type="submit" class="btn btn-edit">Update Ticket</button>
            <a href="<%= request.getContextPath() %>/ticket?action=list">View Tickets</a>
        </form>
    </div>
<% } else { %>
<!-- Create form (default) -->
<div class="form-block">
    <h2>Create New Ticket</h2>
    <%
    List<Map<String, Object>> schedulesForCreate = (List<Map<String, Object>>) request.getAttribute("scheduleList");
    %>
    <form method="POST" action="<%= request.getContextPath() %>/ticket">
        <input type="hidden" name="action" value="create">

        <label>Ticket Number:<br>
            <input type="text" name="ticketNumber" value="<%= request.getAttribute("ticketNumber") %>" readonly>
        </label><br><br>

        <label>Schedule:<br>
            <select name="scheduleID" required>
                <option value="">-- Select Schedule --</option>
                <% if (schedulesForCreate != null) {
                for (Map<String, Object> s : schedulesForCreate) {
                int availableSeats = (Integer) s.get("available_seats");
                boolean isFull = availableSeats <= 0;
                %>
                <option value="<%= s.get("schedule_id") %>" <%= isFull ? "disabled" : "" %>>
                Schedule #<%= s.get("schedule_id") %> - Bus <%= s.get("bus_number") %> - Route <%= s.get("route_name") %> - <%= s.get("departure_time") %>
                (<%= isFull ? "Full" : availableSeats + " seats available" %>)
                </option>
                <%  }
                } %>
            </select>
        </label><br><br>

        <label>Discounted:<br>
            <select name="discounted">
                <option value="false" selected>No</option>
                <option value="true">Yes</option>
            </select>
        </label><br><br>

        <button type="submit" class="btn">Create Ticket</button>
        <a href="<%= request.getContextPath() %>/ticket?action=list">View Ticket List</a>
    </form>
</div>
<% } %>

<!-- Tickets list -->
<% if(request.getAttribute("tickets") != null) {
List<Map<String, Object>> list = (List<Map<String, Object>>) request.getAttribute("tickets");
if(list.size() == 0) {
%>
<p>No tickets found.</p>
<%  } else { %>
<h2>All Tickets</h2>
<table>
    <thead>
    <tr>
        <th>ID</th>
        <th>Ticket Number</th>
        <th>Schedule ID</th>
        <th>Departure Time</th>
        <th>Schedule Status</th>
        <th>Bus Number</th>
        <th>Route Name</th>
        <th>Discounted</th>
        <th>Fare</th>
        <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <% for(Map<String, Object> t : list) { %>
    <tr>
        <td><%= t.get("ticket_id") %></td>
        <td><%= t.get("ticket_number") %></td>
        <td><%= t.get("schedule_id") %></td>
        <td><%= t.get("departure_time") %></td>
        <td><%= t.get("schedule_status") %></td>
        <td><%= t.get("bus_number") %></td>
        <td><%= t.get("route_name") %></td>
        <td><%= (Boolean) t.get("discounted") ? "Yes" : "No" %></td>
        <td><%= t.get("fare") %></td>
        <td>
            <a class="btn btn-edit" href="<%= request.getContextPath() %>/ticket?action=edit&id=<%= t.get("ticket_id") %>">Edit</a>

            <form method="POST" action="<%= request.getContextPath() %>/ticket" style="display:inline;" onsubmit="return confirmDelete(this);">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="<%= t.get("ticket_id") %>">
                <button type="submit" class="btn btn-delete">Delete</button>
            </form>
            </form>
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
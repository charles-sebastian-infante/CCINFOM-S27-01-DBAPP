<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.busterminal.model.Maintenance" %>
<%@ page import="com.busterminal.model.Bus" %>
<%@ page import="com.busterminal.model.Staff" %>
<%@ page import="com.busterminal.model.MaintenanceType" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Manage Maintenance</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/style/global.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/style/maintenance.css">

    <script>
        function confirmDelete(maintenanceID) {
            return confirm('Are you sure you want to delete maintenance record #' + maintenanceID + '?');
        }
    </script>
</head>
<body>
<h1>Manage Maintenance Records</h1>

<%
String message = (String) request.getAttribute("message");
if (message == null) message = (String) session.getAttribute("message");
if (message != null) {
session.removeAttribute("message");
%>
<p style="color:green;"><%= message %></p>
<% } %>

<%
String error = (String) request.getAttribute("error");
if (error == null) error = (String) session.getAttribute("error");
if (error != null) {
session.removeAttribute("error");
%>
<p style="color:red;"><%= error %></p>
<% } %>

<!-- Edit Form -->
<%-- Edit Form --%>
<%
Maintenance edit = (Maintenance) request.getAttribute("editMaintenance");
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
if (edit != null) {
%>

<div id="editForm" class="form-block">
    <h2>Edit Maintenance Record</h2>
    <form method="POST" action="<%= request.getContextPath() %>/maintenance">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="maintenanceID" value="<%= edit.maintenanceID %>">

        <label>Bus:<br>
            <select name="busID" required>
                <option value="">-- Select Bus --</option>
                <%
                List<Bus> buses = (List<Bus>) request.getAttribute("buses");
                if (buses != null) {
                for (Bus bus : buses) { 
                    if (!"In Transit".equals(bus.status)) {
                %>
                <option value="<%= bus.busID %>" <%= (bus.busID == edit.busID) ? "selected" : "" %>>
                <%= bus.busNumber %> - <%= bus.status %>
                </option>
                <%  }
                    }
                } %>
            </select>
        </label><br><br>

        <label>Mechanic:<br>
            <select name="assignedMechanic" required>
                <option value="">-- Select Mechanic --</option>
                <%
                List<Staff> mechanics = (List<Staff>) request.getAttribute("mechanics");
                if (mechanics != null) {
                for (Staff mech : mechanics) { %>
                <option value="<%= mech.staffID %>" <%= (mech.staffID == edit.assignedMechanic) ? "selected" : "" %>>
                <%= mech.staffName %>
                </option>
                <%  }
                } %>
            </select>
        </label><br><br>

        <label>Maintenance Type:<br>
            <select name="maintenanceTypeID" required>
                <option value="">-- Select Type --</option>
                <%
                List<MaintenanceType> types = (List<MaintenanceType>) request.getAttribute("maintenanceTypes");
                if (types != null) {
                for (MaintenanceType type : types) { %>
                <option value="<%= type.maintenanceTypeID %>" <%= (type.maintenanceTypeID == edit.maintenanceTypeID) ? "selected" : "" %>>
                <%= type.typeName %>
                </option>
                <%  }
                } %>
            </select>
        </label><br><br>

        <label>Starting Date:<br>
            <input type="datetime-local" name="startingDate"
                   value="<%= (edit.startingDate != null) ? sdf.format(edit.startingDate) : "" %>" required>
        </label><br><br>

        <label>Completion Time:<br>
            <input type="datetime-local" name="completionTime"
                   value="<%= (edit.completionTime != null) ? sdf.format(edit.completionTime) : "" %>">
            <small>Leave blank if maintenance is ongoing</small>
        </label><br><br>

        <button type="submit" class="btn">Update Maintenance Record</button>
        <a href="<%= request.getContextPath() %>/maintenance?action=list">Cancel</a>
    </form>
</div>
<% } else { %>

<!-- Create form (default) -->
<div class="form-block">
    <h2>Create New Maintenance Record</h2>
    <%
    List<Bus> busesForCreate = (List<Bus>) request.getAttribute("buses");
    List<Staff> mechanicsForCreate = (List<Staff>) request.getAttribute("mechanics");
        List<MaintenanceType> maintenanceTypesForCreate = (List<MaintenanceType>) request.getAttribute("maintenanceTypes");

            // If not loaded from controller, fetch them
            if (busesForCreate == null) {
            busesForCreate = Bus.getAllBuses();
            }
            if (mechanicsForCreate == null) {
            mechanicsForCreate = Staff.getStaffByRole(4);
            }
            if (maintenanceTypesForCreate == null) {
            maintenanceTypesForCreate = MaintenanceType.getAllMaintenanceTypes();
            }
            %>
            <form method="POST" action="<%= request.getContextPath() %>/maintenance">
                <input type="hidden" name="action" value="create">

                <label>Bus:<br>
                    <select name="busID" required>
                        <option value="">-- Select Bus --</option>
                        <% if (busesForCreate != null) {
                        for (Bus bus : busesForCreate) { 
                            if (!"In Transit".equals(bus.status)) {
                        %>
                        <option value="<%= bus.busID %>">
                            <%= bus.busNumber %> - <%= bus.status %>
                        </option>
                        <%  }
                            }
                        } %>
                    </select>
                </label><br><br>

                <label>Mechanic:<br>
                    <select name="mechanicID" required>
                        <option value="">-- Select --</option>
                        <% if (mechanicsForCreate != null) {
                        for (Staff mechanic : mechanicsForCreate) { %>
                        <option value="<%= mechanic.staffID %>">
                            <%= mechanic.staffName %>
                        </option>
                        <%  }
                        } %>
                    </select>
                </label><br><br>

                <label>Maintenance Type:<br>
                    <select name="maintenanceTypeID" required>
                        <option value="">-- Select Type --</option>
                        <% if (maintenanceTypesForCreate != null) {
                        for (MaintenanceType type : maintenanceTypesForCreate) { %>
                        <option value="<%= type.maintenanceTypeID %>">
                            <%= type.typeName %>
                        </option>
                        <%  }
                        } %>
                    </select>
                </label><br><br>

                <label>Starting Date:<br>
                    <input type="date" name="startingDate" required>
                </label><br><br>

                <label>Starting Time:<br>
                    <input type="time" name="startingTime" required>
                </label><br><br>

                <button type="submit" class="btn">Create Maintenance Record</button>
                <a href="<%= request.getContextPath() %>/maintenance?action=list">View All Records</a>
            </form>
</div>
<% } %>



<!-- Maintenance Records List -->
<%
List<Map<String, Object>> maintenanceRecords =
(List<Map<String, Object>>) request.getAttribute("maintenanceRecords");

if(maintenanceRecords != null) {

if(maintenanceRecords.size() == 0) {
%>
<p>No maintenance records found.</p>
<%  } else { %>
<h2>All Maintenance Records</h2>
<table>
    <thead>
    <tr>
        <th>Maintenance ID</th>
        <th>Bus Number</th>
        <th>Assigned Mechanic</th>
        <th>Maintenance Type</th>
        <th>Starting Date</th>
        <th>Completion Time</th>
        <th>Cost</th>
        <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <%
    for(Map<String, Object> record : maintenanceRecords) {
    int maintenanceID = (Integer) record.get("maintenance_id");
    String busNumber = (String) record.get("bus_number");
    String mechanicName = (String) record.get("mechanic_name");
    String typeName = (String) record.get("type_name");
    java.sql.Timestamp startingDate = (java.sql.Timestamp) record.get("starting_date");
    java.sql.Timestamp completionTime = (java.sql.Timestamp) record.get("completion_time");
    Double maintenanceCost = (Double) record.get("maintenance_cost");
    Double durationHours = (Double) record.get("duration_hours");

    // Determine status based on completion time
    String status = "In Progress";
    String statusClass = "status-in-progress";
    boolean isCompleted = false;
    if (completionTime != null) {
    status = "Completed";
    statusClass = "status-completed";
    isCompleted = true;
    }
    %>
    <tr>
        <td><%= maintenanceID %></td>
        <td><%= busNumber != null ? busNumber : "N/A" %></td>
        <td><%= mechanicName != null ? mechanicName : "Unassigned" %></td>
        <td><%= typeName != null ? typeName : "N/A" %></td>
        <td><%= startingDate != null ? startingDate.toString() : "N/A" %></td>
        <td><%= completionTime != null ? completionTime.toString() : "Ongoing" %></td>
        <td><%= maintenanceCost != null ? "₱" + String.format("%.2f", maintenanceCost) : "N/A" %></td>
        <td>
            <% if (!isCompleted) { %>
            <form method="POST"
                  action="<%= request.getContextPath() %>/maintenance"
                  style="display:inline;">
                <input type="hidden" name="action" value="complete">
                <input type="hidden" name="maintenanceID" value="<%= maintenanceID %>">
                <button type="submit" class="btn btn-edit">Complete</button>
            </form>
            <% } %>

            <a href="<%= request.getContextPath() %>/maintenance?action=edit&id=<%= maintenanceID %>" class="btn btn-edit">Edit</a>

            <form method="POST"
                  action="<%= request.getContextPath() %>/maintenance"
                  style="display:inline;"
                  onsubmit="return confirmDelete('<%= maintenanceID %>');">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="<%= maintenanceID %>">
                <button type="submit" class="btn btn-delete">Delete</button>
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

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Manage Buses</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
        th { background: #f4f4f4; }
        .form-block { margin: 20px 0; padding: 12px; border: 1px solid #ddd; background:#fafafa; }
        .btn { padding:6px 10px; margin-right:6px; }
        .btn-delete { background:#e74c3c; color:#fff; border:none; }
        .btn-edit { background:#3498db; color:#fff; border:none; }
    </style>
    <script>
        function confirmDelete(busId) {
            return confirm('Delete bus ID ' + busId + '?');
        }

        function validateBusForm(form) {
            var capacity = parseInt(form.capacity.value);
            if (isNaN(capacity) || capacity <= 0) {
                alert('Please enter a valid capacity (positive number).');
                return false;
            }
            return true;
        }
    </script>
</head>
<body>
    <h1>Manage Buses</h1>

    <% if(request.getAttribute("message") != null) { %>
        <p style="color:green;"><%= request.getAttribute("message") %></p>
    <% } %>

    <% if(request.getAttribute("error") != null) { %>
        <p style="color:red;"><%= request.getAttribute("error") %></p>
    <% } %>

    <!-- Edit form -->
    <% if(request.getAttribute("bus") != null) {
        com.busterminal.model.Bus b = (com.busterminal.model.Bus) request.getAttribute("bus");
    %>
        <div class="form-block">
            <h2>Edit Bus</h2>
            <form method="POST" action="<%= request.getContextPath() %>/bus" onsubmit="return validateBusForm(this);">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="busID" value="<%= b.busID %>">

                <label>Bus Number:<br>
                    <input type="text" name="busNumber" value="<%= b.busNumber != null ? b.busNumber : "" %>" required>
                </label><br><br>

                <label>Capacity:<br>
                    <input type="number" name="capacity" value="<%= b.capacity %>" required>
                </label><br><br>

                <label>Status:<br>
                    <select name="status" required>
                        <option value="Available" <%= "Available".equals(b.status) ? "selected" : "" %>>Available</option>
                        <option value="In Transit" <%= "In Transit".equals(b.status) ? "selected" : "" %>>In Transit</option>
                        <option value="Maintenance" <%= "Maintenance".equals(b.status) ? "selected" : "" %>>Maintenance</option>
                    </select>
                </label><br><br>

                <label>Current Terminal:<br>
                    <input type="number" name="currentTerminal" value="<%= b.currentTerminal %>" required>
                </label><br><br>

                <button type="submit" class="btn btn-edit">Update Bus</button>
                <a href="<%= request.getContextPath() %>/bus?action=list">Cancel</a>
            </form>
        </div>
    <% } else { %>
    <!-- Create form -->
        <div class="form-block">
            <h2>Register New Bus</h2>
            <form method="POST" action="<%= request.getContextPath() %>/bus" onsubmit="return validateBusForm(this);">
                <input type="hidden" name="action" value="create">

                <label>Bus Number:<br>
                    <input type="text" name="busNumber" required>
                </label><br><br>

                <label>Capacity:<br>
                    <input type="number" name="capacity" required>
                </label><br><br>

                <label>Status:<br>
                    <select name="status" required>
                        <option value="Available">Available</option>
                        <option value="In Transit">In Transit</option>
                        <option value="Maintenance">Maintenance</option>
                    </select>
                </label><br><br>

                <label>Current Terminal:<br>
                    <input type="number" name="currentTerminal" required>
                </label><br><br>

                <button type="submit" class="btn">Register Bus</button>
                <a href="<%= request.getContextPath() %>/bus?action=list">View Bus List</a>
            </form>
        </div>
    <% } %>

    <!-- Buses list -->
    <% if(request.getAttribute("buses") != null) {
        java.util.List<com.busterminal.model.Bus> list = (java.util.List<com.busterminal.model.Bus>) request.getAttribute("buses");
        if(list.size() == 0) {
    %>
        <p>No buses found.</p>
    <%  } else { %>
        <h2>All Buses</h2>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Bus Number</th>
                    <th>Capacity</th>
                    <th>Status</th>
                    <th>Current Terminal</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
            <% for(com.busterminal.model.Bus bus : list) { %>
                <tr>
                    <td><%= bus.busID %></td>
                    <td><%= bus.busNumber %></td>
                    <td><%= bus.capacity %></td>
                    <td><%= bus.status %></td>
                    <td><%= bus.currentTerminal %></td>
                    <td>
                        <a class="btn btn-edit" href="<%= request.getContextPath() %>/bus?action=edit&id=<%= bus.busID %>">Edit</a>
                        
                        <form method="POST" action="<%= request.getContextPath() %>/bus" style="display:inline;" 
                              onsubmit="return confirmDelete(<%= bus.busID %>)">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= bus.busID %>">
                            <button type="submit" class="btn btn-delete">Delete</button>
                        </form>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    <%  }
    } %>

</body>
</html>
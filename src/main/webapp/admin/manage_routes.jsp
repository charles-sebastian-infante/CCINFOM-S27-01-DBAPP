<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Manage Routes</title>
    <link rel="stylesheet" href="../assets/css/admin.css">
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
        function confirmDelete(form) {
            if (confirm('Are you sure you want to remove this route?')) {
                form.submit();
            }
            return false;
        }
    </script>
</head>
<body>
    <h1>Manage Routes</h1>

    <% if(request.getAttribute("message") != null) { %>
        <p style="color:green;"><%= request.getAttribute("message") %></p>
    <% } %>

    <% if(request.getAttribute("error") != null) { %>
        <p style="color:red;"><%= request.getAttribute("error") %></p>
    <% } %>

    <!-- Edit form (shown when a single 'route' attribute is present) -->
    <% if(request.getAttribute("route") != null) {
        com.busterminal.model.Route r = (com.busterminal.model.Route) request.getAttribute("route");
    %>
        <div class="form-block">
            <h2>Edit Route</h2>
            <form method="POST" action="<%= request.getContextPath() %>/route">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="routeID" value="<%= r.routeID %>">

                <label>Route Name:<br>
                    <input type="text" name="routeName" value="<%= r.routeName != null ? r.routeName : "" %>" required>
                </label><br><br>

                <label>Origin ID:<br>
                    <input type="number" name="originID" value="<%= r.originID %>" required>
                </label><br><br>

                <label>Destination ID:<br>
                    <input type="number" name="destinationID" value="<%= r.destinationID %>" required>
                </label><br><br>

                <label>Distance:<br>
                    <input type="text" name="distance" value="<%= r.distance %>" required>
                </label><br><br>

                <label>Travel Time:<br>
                    <input type="text" name="travelTime" value="<%= r.travelTime %>" required>
                </label><br><br>

                <label>Base Fare:<br>
                    <input type="text" name="baseFare" value="<%= r.baseFare %>" required>
                </label><br><br>

                <button type="submit" class="btn btn-edit">Update Route</button>
                <a href="<%= request.getContextPath() %>/route?action=list">Cancel</a>
            </form>
        </div>
    <% } else { %>
    <!-- Create form (default) -->
        <div class="form-block">
            <h2>Register New Route</h2>
            <form method="POST" action="<%= request.getContextPath() %>/route">
                <input type="hidden" name="action" value="create">

                <label>Route Name:<br>
                    <input type="text" name="routeName" required>
                </label><br><br>

                <label>Origin ID:<br>
                    <input type="number" name="originID" required>
                </label><br><br>

                <label>Destination ID:<br>
                    <input type="number" name="destinationID" required>
                </label><br><br>

                <label>Distance:<br>
                    <input type="text" name="distance" required>
                </label><br><br>

                <label>Travel Time:<br>
                    <input type="text" name="travelTime" required>
                </label><br><br>

                <label>Base Fare:<br>
                    <input type="text" name="baseFare" required>
                </label><br><br>

                <button type="submit" class="btn">Register Route</button>
                <a href="<%= request.getContextPath() %>/route?action=list">View Route List</a>
            </form>
        </div>
    <% } %>

    <!-- Routes list -->
    <% if(request.getAttribute("routes") != null) {
        java.util.List<com.busterminal.model.Route> list = (java.util.List<com.busterminal.model.Route>) request.getAttribute("routes");
        if(list.size() == 0) {
    %>
        <p>No routes found.</p>
    <%  } else { %>
        <h2>All Routes</h2>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Route Name</th>
                    <th>Origin ID</th>
                    <th>Destination ID</th>
                    <th>Distance</th>
                    <th>Travel Time</th>
                    <th>Base Fare</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
            <% for(com.busterminal.model.Route rr : list) { %>
                <tr>
                    <td><%= rr.routeID %></td>
                    <td><%= rr.routeName %></td>
                    <td><%= rr.originID %></td>
                    <td><%= rr.destinationID %></td>
                    <td><%= rr.distance %></td>
                    <td><%= rr.travelTime %></td>
                    <td><%= rr.baseFare %></td>
                    <td>
                        <a class="btn btn-edit" href="<%= request.getContextPath() %>/route?action=edit&id=<%= rr.routeID %>">Edit</a>

                        <form method="POST" action="<%= request.getContextPath() %>/route" style="display:inline;" onsubmit="return confirm('Delete route ID <%= rr.routeID %>?') ? true : false;">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= rr.routeID %>">
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
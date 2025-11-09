<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.busterminal.model.Terminal" %>
<%@ page import="com.busterminal.model.Route" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Manage Routes</title>
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
        <p style="color:red;"><%= request.getAttribute("error: Invalid value inputted") %></p>
    <% } %>

    <!-- Edit form (shown when a single 'route' attribute is present) -->
    <% if(request.getAttribute("route") != null) {
        Route r = (Route) request.getAttribute("     route");
        List<Terminal> terminals = (List<Terminal>) request.getAttribute("terminals");
    %>
        <div class="form-block">
            <h2>Edit Route</h2>
            <form method="POST" action="<%= request.getContextPath() %>/route">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="routeID" value="<%= r.routeID %>">

                <label>Route Name:<br>
                    <input type="text" name="routeName" value="<%= r.routeName != null ? r.routeName : "" %>" required>
                </label><br><br>

                <label>Origin Terminal:<br>
                    <select name="originID" required>
                        <option value="">-- Select Origin --</option>
                        <% if (terminals != null) {
                            for (Terminal terminal : terminals) { %>
                                <option value="<%= terminal.terminalID %>" 
                                    <%= terminal.terminalID == r.originID ? "selected" : "" %>>
                                    <%= terminal.terminalName %>
                                </option>
                        <%  }
                        } %>
                    </select>
                </label><br><br>

                <label>Destination Terminal:<br>
                    <select name="destinationID" required>
                        <option value="">-- Select Destination --</option>
                        <% if (terminals != null) {
                            for (Terminal terminal : terminals) { %>
                                <option value="<%= terminal.terminalID %>" 
                                    <%= terminal.terminalID == r.destinationID ? "selected" : "" %>>
                                    <%= terminal.terminalName %>
                                </option>
                        <%  }
                        } %>
                    </select>
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
            <% 
                List<Terminal> terminalsForCreate = (List<Terminal>) request.getAttribute("terminals"); 
                // If terminals not loaded from controller, fetch them
                if (terminalsForCreate == null) {
                    terminalsForCreate = Terminal.getAllTerminals();
                }
            %>
            <form method="POST" action="<%= request.getContextPath() %>/route">
                <input type="hidden" name="action" value="create">

                <label>Route Name:<br>
                    <input type="text" name="routeName" required>
                </label><br><br>

                <label>Origin Terminal:<br>
                    <select name="originID" required>
                        <option value="">-- Select Origin --</option>
                        <% if (terminalsForCreate != null) {
                            for (Terminal terminal : terminalsForCreate) { %>
                                <option value="<%= terminal.terminalID %>">
                                    <%= terminal.terminalName %>
                                </option>
                        <%  }
                        } %>
                    </select>
                </label><br><br>

                <label>Destination Terminal:<br>
                    <select name="destinationID" required>
                        <option value="">-- Select Destination --</option>
                        <% if (terminalsForCreate != null) {
                            for (Terminal terminal : terminalsForCreate) { %>
                                <option value="<%= terminal.terminalID %>">
                                    <%= terminal.terminalName %>
                                </option>
                        <%  }
                        } %>
                    </select>
                </label><br><br>

                <label>Distance:<br>
                    <input type="text" name="distance" required>
                </label><br><br>

                <label>Travel Time:<br>
                    <input type="text" name="travelTime" placeholder="hh:mm:ss" required> 
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
                    <th>Origin Terminal</th>
                    <th>Destination Terminal</th>
                    <th>Distance</th>
                    <th>Travel Time</th>
                    <th>Base Fare</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
            <% 
                // Get terminals list and create a map for easy terminal name lookup
                List<Terminal> terminals = (List<Terminal>) request.getAttribute("terminals");
                // If terminals not loaded from controller, fetch them
                if (terminals == null) {
                    terminals = Terminal.getAllTerminals();
                }
                Map<Integer, String> terminalMap = new HashMap<>();
                if (terminals != null) {
                    for (Terminal t : terminals) {
                        terminalMap.put(t.terminalID, t.terminalName);
                    }
                }
                
                for(com.busterminal.model.Route rr : list) { 
                    String originName = terminalMap.get(rr.originID);
                    String destName = terminalMap.get(rr.destinationID);
            %>
                <tr>
                    <td><%= rr.routeID %></td>
                    <td><%= rr.routeName %></td>
                    <td><%= originName != null ? originName : "ID: " + rr.originID %></td>
                    <td><%= destName != null ? destName : "ID: " + rr.destinationID %></td>
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

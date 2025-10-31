<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Manage Terminals</title>
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
            if (confirm('Are you sure you want to remove this terminal?')) {
                form.submit();
            }
            return false;
        }

        function validateTerminalForm(form) {
            var name = (form.terminalName && form.terminalName.value) ? form.terminalName.value.trim() : "";
            var phone = (form.phone && form.phone.value) ? form.phone.value.trim() : "";
            if (name === phone && name !== "") {
                alert('Terminal name and phone cannot be the same.');
                return false;
            }
            return true;
        }
    </script>
</head>
<body>
    <h1>Manage Terminals</h1>

    <% if(request.getAttribute("message") != null) { %>
        <p style="color:green;"><%= request.getAttribute("message") %></p>
    <% } %>

    <% if(request.getAttribute("error") != null) { %>
        <p style="color:red;"><%= request.getAttribute("error") %></p>
    <% } %>

    <!-- Edit form (shown when a single 'terminal' attribute is present) -->
    <% if(request.getAttribute("terminal") != null) {
        com.busterminal.model.Terminal t = (com.busterminal.model.Terminal) request.getAttribute("terminal");
    %>
        <div class="form-block">
            <h2>Edit Terminal</h2>
            <form method="POST" action="<%= request.getContextPath() %>/terminal">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="terminalID" value="<%= t.terminalID %>">

                <label>Terminal Name:<br>
                    <input type="text" name="terminalName" value="<%= t.terminalName != null ? t.terminalName : "" %>" required>
                </label><br><br>

                <label>Location:<br>
                    <input type="text" name="location" value="<%= t.location != null ? t.location : "" %>" required>
                </label><br><br>

                <label>City:<br>
                    <input type="text" name="city" value="<%= t.city != null ? t.city : "" %>" required>
                </label><br><br>

                <label>Phone:<br>
                    <input type="text" name="phone" value="<%= t.phone != null ? t.phone : "" %>">
                </label><br><br>

                <button type="submit" class="btn btn-edit">Update Terminal</button>
                <a href="<%= request.getContextPath() %>/terminal?action=list">Cancel</a>
            </form>
        </div>
    <% } else { %>
    <!-- Create form (default) -->
        <div class="form-block">
            <h2>Register New Terminal</h2>
            <form method="POST" action="<%= request.getContextPath() %>/terminal">
                <input type="hidden" name="action" value="create">

                <label>Terminal Name:<br>
                    <input type="text" name="terminalName" required>
                </label><br><br>

                <label>Location:<br>
                    <input type="text" name="location" required>
                </label><br><br>

                <label>City:<br>
                    <input type="text" name="city" required>
                </label><br><br>

                <label>Phone:<br>
                    <input type="text" name="phone">
                </label><br><br>

                <button type="submit" class="btn">Register Terminal</button>
                <a href="<%= request.getContextPath() %>/terminal?action=list">View Terminal List</a>
            </form>
        </div>
    <% } %>

    <!-- Terminals list -->
    <% if(request.getAttribute("terminals") != null) {
        java.util.List<com.busterminal.model.Terminal> list = (java.util.List<com.busterminal.model.Terminal>) request.getAttribute("terminals");
        if(list.size() == 0) {
    %>
        <p>No terminals found.</p>
    <%  } else { %>
        <h2>All Terminals</h2>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Terminal Name</th>
                    <th>Location</th>
                    <th>City</th>
                    <th>Phone</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
            <% for(com.busterminal.model.Terminal tt : list) { %>
                <tr>
                    <td><%= tt.terminalID %></td>
                    <td><%= tt.terminalName %></td>
                    <td><%= tt.location %></td>
                    <td><%= tt.city %></td>
                    <td><%= tt.phone %></td>
                    <td>
                        <a class="btn btn-edit" href="<%= request.getContextPath() %>/terminal?action=edit&id=<%= tt.terminalID %>">Edit</a>

                        <form method="POST" action="<%= request.getContextPath() %>/terminal" style="display:inline;" onsubmit="return confirm('Delete terminal ID <%= tt.terminalID %>?') ? true : false;">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= tt.terminalID %>">
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
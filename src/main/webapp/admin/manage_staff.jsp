<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Manage Staff</title>
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
        function confirmDelete() {
            return confirm('Are you sure you want to delete this staff member?');
        }
    </script>
</head>
<body>
    <h1>Manage Staff</h1>

    <% if(request.getAttribute("message") != null) { %>
        <p style="color:green;"><%= request.getAttribute("message") %></p>
    <% } %>

    <% if(request.getAttribute("error") != null) { %>
        <p style="color:red;"><%= request.getAttribute("error") %></p>
    <% } %>

    <!-- Edit form -->
    <% if(request.getAttribute("staff") != null) {
        com.busterminal.model.Staff s = (com.busterminal.model.Staff) request.getAttribute("staff");
    %>
        <div class="form-block">
            <h2>Edit Staff</h2>
            <form method="POST" action="<%= request.getContextPath() %>/staff">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="staffID" value="<%= s.staffID %>">

                <label>Staff Name:<br>
                    <input type="text" name="staffName" value="<%= s.staffName != null ? s.staffName : "" %>" required>
                </label><br><br>

                <label>Role:<br>
                    <input type="text" name="role" value="<%= s.role != null ? s.role : "" %>" required>
                </label><br><br>

                <label>Assigned Terminal:<br>
                    <input type="number" name="assignedTerminal" value="<%= s.assignedTerminal %>" required>
                </label><br><br>

                <label>Assigned Bus:<br>
                    <input type="number" name="assignedBus" value="<%= s.assignedBus %>" required>
                </label><br><br>

                <label>Shift:<br>
                    <input type="text" name="shift" value="<%= s.shift != null ? s.shift : "" %>" required>
                </label><br><br>

                <label>Contact:<br>
                    <input type="text" name="contact" value="<%= s.contact != null ? s.contact : "" %>" required>
                </label><br><br>

                <button type="submit" class="btn btn-edit">Update Staff</button>
                <a href="<%= request.getContextPath() %>/staff?action=list">Cancel</a>
            </form>
        </div>
    <% } else { %>

    <!-- Create form -->
        <div class="form-block">
            <h2>Register New Staff</h2>
            <form method="POST" action="<%= request.getContextPath() %>/staff">
                <input type="hidden" name="action" value="create">

                <label>Staff Name:<br>
                    <input type="text" name="staffName" required>
                </label><br><br>

                <label>Role:<br>
                    <input type="text" name="role" required>
                </label><br><br>

                <label>Assigned Terminal:<br>
                    <input type="number" name="assignedTerminal" required>
                </label><br><br>

                <label>Assigned Bus:<br>
                    <input type="number" name="assignedBus" required>
                </label><br><br>

                <label>Shift:<br>
                    <input type="text" name="shift" required>
                </label><br><br>

                <label>Contact:<br>
                    <input type="text" name="contact" required>
                </label><br><br>

                <button type="submit" class="btn">Create Staff</button>
            </form>
        </div>
    <% } %>

    <!-- Staff list -->
    <% if(request.getAttribute("staffList") != null) {
        java.util.List<com.busterminal.model.Staff> list = (java.util.List<com.busterminal.model.Staff>) request.getAttribute("staffList");
        if(list.size() == 0) {
    %>
        <p>No staff found.</p>
    <%  } else { %>
        <h2>All Staff</h2>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Role</th>
                    <th>Assigned Terminal</th>
                    <th>Assigned Bus</th>
                    <th>Shift</th>
                    <th>Contact</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
            <% for(com.busterminal.model.Staff st : list) { %>
                <tr>
                    <td><%= st.staffID %></td>
                    <td><%= st.staffName %></td>
                    <td><%= st.role %></td>
                    <td><%= st.assignedTerminal %></td>
                    <td><%= st.assignedBus %></td>
                    <td><%= st.shift %></td>
                    <td><%= st.contact %></td>
                    <td>
                        <a class="btn btn-edit" href="<%= request.getContextPath() %>/staff?action=edit&id=<%= st.staffID %>">Edit</a>
                        <form method="POST" action="<%= request.getContextPath() %>/staff" style="display:inline;" onsubmit="return confirmDelete();">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= st.staffID %>">
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
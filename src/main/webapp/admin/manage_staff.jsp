<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map, java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Manage Staff</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/style/manage_staff.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/style/global.css">

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
                    <input type="text" name="roleID" value="<%= s.roleID != 0 ? s.roleID : "" %>" required>
                </label><br><br>

                <label>Assigned Terminal:<br>
                    <input type="number" name="assignedTerminal" value="<%= s.assignedTerminal %>" required>
                </label><br><br>

                <label>Assigned Bus:<br>
                    <input type="number" name="assignedBus" value="<%= s.assignedBus %>" required>
                </label><br><br>

                <label>Shift:<br>
                    <select name="shift" required>
                        <option value="Morning" <%= "Morning".equals(s.shift) ? "selected" : "" %>>Morning</option>
                        <option value="Evening" <%= "Evening".equals(s.shift) ? "selected" : "" %>>Evening</option>
                    </select>
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
                    <input type="text" name="roleID" required>
                </label><br><br>

                <label>Assigned Terminal:<br>
                    <input type="number" name="assignedTerminal" required>
                </label><br><br>

                <label>Assigned Bus:<br>
                    <input type="number" name="assignedBus" required>
                </label><br><br>

                <label>Shift:<br>
                    <select name="shift" required>
                        <option value="Morning"> Morning </option>
                        <option value="Evening"> Evening </option>
                    </select>
                </label><br><br>

                <label>Contact:<br>
                    <input type="text" name="contact" required>
                </label><br><br>

                <button type="submit" class="btn">Create Staff</button>
            </form>
        </div>
    <% } %>

    <!-- Staff list -->
    <div class="staffList">
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
            <% for(Map<String,String> st : (List<Map<String,String>>) request.getAttribute("staffList")) { %>
            <tr>
                <td><%= st.get("staffID") %></td>
                <td><%= st.get("staffName") %></td>
                <td><%= st.get("role") %></td>
                <td><%= st.get("assigned_terminal") %></td>
                <td><%= st.get("assigned_bus") != null ? st.get("assigned_bus") : "No Assigned Bus"%></td>
                <td><%= st.get("shift") %></td>
                <td><%= st.get("contact") %></td>
                <td>
                    <a class="btn btn-edit" href="<%= request.getContextPath() %>/staff?action=edit&id=<%= st.get("staffID") %>">Edit</a>
                    <form method="POST" action="<%= request.getContextPath() %>/staff" style="display:inline;" onsubmit="return confirmDelete();">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="<%= st.get("staffID") %>">
                        <button type="submit" class="btn btn-delete">Delete</button>
                    </form>
                </td>
            </tr>
            <% } %>
            </tbody>
        </table>
    <%  }
    } %>
    </div>

    <a href="<%= request.getContextPath() %>/" class="fixed-button">Back to Home</a>

</body>
</html>
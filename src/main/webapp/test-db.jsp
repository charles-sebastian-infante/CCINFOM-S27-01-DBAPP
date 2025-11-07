<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="com.busterminal.utils.DBConnection" %>
<!DOCTYPE html>
<html>
<head>
    <title>Database Connection Test</title>
    <style>
        body { font-family: Arial; margin: 20px; }
        .success { color: green; font-weight: bold; }
        .error { color: red; font-weight: bold; }
        table { border-collapse: collapse; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even) { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h1>Database Connection Test</h1>
    
    <%
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                out.println("<p class='success'>✓ Successfully connected to database!</p>");
                
                // Array of all tables
                String[] tables = {
                    "Terminal", "Route", "Bus", "Role", "Staff", 
                    "Maintenance_Type", "Maintenance", "Schedule", "Ticket"
                };
                
                out.println("<h2>Row Counts:</h2>");
                out.println("<table>");
                out.println("<tr><th>Table Name</th><th>Row Count</th></tr>");
                
                int totalRows = 0;
                
                for (String table : tables) {
                    PreparedStatement pStmt = conn.prepareStatement("SELECT COUNT(*) as count FROM " + table);
                    ResultSet rs = pStmt.executeQuery();
                    
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        totalRows += count;
                        out.println("<tr><td>" + table + "</td><td>" + count + "</td></tr>");
                    }
                    
                    rs.close();
                    pStmt.close();
                }
                
                out.println("<tr style='font-weight: bold; background-color: #4CAF50; color: white;'>");
                out.println("<td>TOTAL</td><td>" + totalRows + "</td></tr>");
                out.println("</table>");
                
                conn.close();
            } else {
                out.println("<p class='error'>✗ Connection is null</p>");
            }
        } catch (Exception e) {
            out.println("<p class='error'>✗ Database Error: " + e.getMessage() + "</p>");
            e.printStackTrace(new java.io.PrintWriter(out));
        }
    %>
    
    <p><a href="index.jsp">Back to Home</a></p>
</body>
</html>

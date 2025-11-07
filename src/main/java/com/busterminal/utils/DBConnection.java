package com.busterminal.utils;
import java.sql.*;

public class DBConnection {
    private static final String URL = 
        "jdbc:mysql://localhost:3306/bus_terminal_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "passcode";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Explicitly load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection
            conn = DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQL Connection Error: " + e.getMessage());
            e.printStackTrace();
        }

        return conn;
    }
}

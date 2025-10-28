package com.busterminal.utils;
import java.sql.*;

public class DBConnection {
    private static final String URL = 
        "jdbc:mysql://localhost:3306/bus_terminal_management";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            URL + "?useTimezone=true&serverTimezone=UTC&useSSL=false",
            USER,
            PASSWORD
        );
    }
}

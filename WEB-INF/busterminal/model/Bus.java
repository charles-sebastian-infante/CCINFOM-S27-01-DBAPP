package busterminal.model;
import java.sql.*;

import javax.naming.spi.DirStateFactory.Result;

public class Bus {
    public int busID;
    public String busNumber;
    public int capacity;
    public String status;
    public int currentTerminal;
    public int routeID;
    private final String url;

    public Bus() {
        busID = 0;
        busNumber = "";
        capacity = 45;
        status = "Available";
        currentTerminal = 0;
        routeID = 0;
    }

    public int addRecord() {
        try {
            Connection conn;
            conn = DriverManager.getConnection(url);
            PreparedStatement pStmt = conn.prepareStatement(
                "INSERT INTO Bus (bus_number, capacity, status, "
                + "current_terminal , route_id) VALUES (?,?,?,?,?)");
            pStmt.setString(1, busNumber);
            pStmt.setInt(2, capacity);
            pStmt.setString(3, status);
            pStmt.setInt(4, currentTerminal);
            pStmt.setInt(5, routeID);
            pStmt.executeUpdate();
            pStmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public int modRecord() {
        try {
            Connection conn;
            conn = DriverManager.getConnection(url);
            PreparedStatement pStmt = conn.prepareStatement("UPDATE Bus " +
                "SET bus_number = ?, " + "   capacity = ?, " + 
                "     status = ?, " + "   currentTerminal = ?, " + 
                "   route_id = ?, " + "WHERE bus_id = ?");

        pStmt.setString(1, busNumber);
        pStmt.setInt(2, capacity);
        pStmt.setString(3, status);
        pStmt.setInt(4, currentTerminal);
        pStmt.setInt(5, routeID);
        pStmt.setInt(6, busID);
        pStmt.executeUpdate();
        pStmt.close();
        conn.close();
        return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public int delRecord() {
        try {
            Connection conn;
            conn = DriverManager.getConnection(url);
            PreparedStatement pStmt = conn.prepareStatement
                ("DELETE FROM Bus WHERE bus_id = ?");
            pStmt.setInt(1, busID);
            pStmt.executeUpdate();
            pStmt.close();
            conn.close();
            return 1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public int getRecord() {
        try {
            Connection conn;
            conn = DriverManager.getConnection(url);
            PreparedStatement pStmt = conn.prepareStatement
                ("SELECT * FROM Bus WHERE bus_id = ?");
                pStmt.setInt(1, busID);
                ResultSet rs = pStmt.executeQuery();

                busID = 0;
                busNumber = "";
                capacity = 0;
                status = "";
                currentTerminal = 0;
                routeID = 0;

                while (rs.next()) {
                    busID = rs.getInt("bus_id");
                    busNumber = rs.getString("bus_number");
                    capacity = rs.getInt("capacity");
                    status = rs.getString("status");
                    currentTerminal = rs.getInt("current_terminal");
                    routeID = rs.getInt("route_id");
                }
                rs.close();
                pStmt.close();
                conn.close();
                return 1;
        } catch (SQLException e)  {
            System.out.println(e.getMessage());
            return 0;
        }
    }
}




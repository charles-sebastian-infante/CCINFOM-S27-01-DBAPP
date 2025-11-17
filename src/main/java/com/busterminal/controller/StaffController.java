package com.busterminal.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.busterminal.model.Staff;
import com.busterminal.utils.DBConnection;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/staff")
public class StaffController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("list".equals(action))
            listStaff(request, response);
        else if ("edit".equals(action))
            editStaffForm(request, response);
        else if ("reassign".equals(action))
            showReassignmentSection(request, response);
        else
            listStaff(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("create".equals(action))
            addStaff(request, response);
        else if ("update".equals(action))
            updateStaff(request, response);
        else if ("delete".equals(action))
            deleteStaff(request, response);
        else if ("processReassignment".equals(action))
            processReassignment(request, response);
    }

    private void listStaff(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pStmt = conn.prepareStatement(
                    "SELECT * FROM Staff");
            ResultSet rs = pStmt.executeQuery();

            List<Staff> staffList = new ArrayList<>();
            List<Map<String, String>> displayList = new ArrayList<>();
            Map<Integer, String> busMap = getBusNames();
            Map<Integer, String> terminalMap = getTerminalNames();
            Map<Integer, String> roleMap = getRoleNames();

            while (rs.next()) {
                Staff s = new Staff();
                s.staffID = rs.getInt("staff_id");
                s.staffName = rs.getString("staff_name");
                s.roleID = rs.getInt("role_id");
                s.assignedTerminal = rs.getInt("assigned_terminal");
                s.assignedBus = rs.getInt("assigned_bus");
                s.shift = rs.getString("shift");
                s.contact = rs.getString("contact");
                staffList.add(s);
            }

            for (Staff s : staffList) {
                Map<String, String> row = new HashMap<>();
                row.put("staffID", String.valueOf(s.staffID));
                row.put("staffName", String.valueOf(s.staffName));
                row.put("role", roleMap.get(s.roleID));
                row.put("assigned_terminal", terminalMap.get(s.assignedTerminal));
                row.put("assigned_bus", busMap.get(s.assignedBus));
                row.put("shift", String.valueOf(s.shift));
                row.put("contact", String.valueOf(s.contact));
                displayList.add(row);
            }
            request.setAttribute("staffList", displayList);
            request.getRequestDispatcher("/admin/manage_staff.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void editStaffForm(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            Staff staff = new Staff();
            staff.staffID = Integer.parseInt(request.getParameter("id"));

            if (staff.getRecord() == 1) {
                request.setAttribute("staff", staff);
                request.getRequestDispatcher("/admin/manage_staff.jsp")
                        .forward(request, response);
            } else {
                response.sendRedirect("staff?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("staff?action=list");
        }
    }

    private void addStaff(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            Staff staff = new Staff();
            staff.staffName = request.getParameter("staffName");
            staff.roleID = Integer.parseInt(request.getParameter("roleID"));
            staff.assignedTerminal = Integer.parseInt(request
                    .getParameter("assignedTerminal"));
            staff.assignedBus = Integer.parseInt(request
                    .getParameter("assignedBus"));
            staff.shift = request.getParameter("shift");
            staff.contact = request.getParameter("contact");

            if (staff.addRecord() == 1) {
                response.sendRedirect("staff?action=list");
            } else {
                request.setAttribute("error", "Failed to add staff");
                request.getRequestDispatcher("/admin/manage_staff.jsp")
                        .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_staff.jsp")
                    .forward(request, response);
        }
    }

    private void updateStaff(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            Staff staff = new Staff();
            staff.staffID = Integer.parseInt(request.getParameter("staffID"));
            staff.staffName = request.getParameter("staffName");
            staff.roleID = Integer.parseInt(request.getParameter("roleID"));
            staff.assignedTerminal = Integer.parseInt(request
                    .getParameter("assignedTerminal"));
            staff.assignedBus = Integer.parseInt(request
                    .getParameter("assignedBus"));
            staff.shift = request.getParameter("shift");
            staff.contact = request.getParameter("contact");

            if (staff.modRecord() == 1) {
                response.sendRedirect("staff?action=list");
            } else {
                request.setAttribute("error", "Failed to update staff");
                request.setAttribute("staff", staff);
                request.getRequestDispatcher("/admin/manage_staff.jsp")
                        .forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: " + e.getMessage());
            request.getRequestDispatcher("/admin/manage_staff.jsp")
                    .forward(request, response);
        }
    }

    private void deleteStaff(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Staff staff = new Staff();
            staff.staffID = Integer.parseInt(request.getParameter("id"));

            if (staff.delRecord() == 1) {
                response.sendRedirect("staff?action=list");
            } else {
                request.setAttribute("error", "Failed to delete staff");
                response.sendRedirect("staff?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("staff?action=list");
        }
    }

    private void showReassignmentSection(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Connection conn = DBConnection.getConnection();

            // Get staff that are unassigned OR on available buses (Driver and Conductor
            // only)
            PreparedStatement reassignableStmt = conn.prepareStatement(
                    "SELECT s.staff_id, s.staff_name, r.role_name, s.shift, " +
                            "COALESCE(b.bus_number, 'Unassigned') as current_bus " +
                            "FROM Staff s " +
                            "JOIN Role r ON s.role_id = r.role_id " +
                            "LEFT JOIN Bus b ON s.assigned_bus = b.bus_id " +
                            "WHERE r.role_name IN ('Driver', 'Conductor') " +
                            "AND (s.assigned_bus IS NULL OR s.assigned_bus = 0 OR b.status = 'Available') " +
                            "ORDER BY r.role_name, s.shift, s.staff_name");

            ResultSet reassignableRs = reassignableStmt.executeQuery();
            List<Map<String, Object>> unassignedStaff = new ArrayList<>();

            while (reassignableRs.next()) {
                Map<String, Object> staff = new HashMap<>();
                staff.put("staff_id", reassignableRs.getInt("staff_id"));
                staff.put("staff_name", reassignableRs.getString("staff_name"));
                staff.put("role_name", reassignableRs.getString("role_name"));
                staff.put("shift", reassignableRs.getString("shift"));
                staff.put("current_bus", reassignableRs.getString("current_bus"));
                unassignedStaff.add(staff);
            }

            // Get all buses with their assigned staff
            PreparedStatement busesStmt = conn.prepareStatement(
                    "SELECT b.bus_id, b.bus_number, b.status, " +
                            "GROUP_CONCAT(CONCAT(r.role_name, ':', s.shift, ':', s.staff_name) SEPARATOR '|') as assigned_staff "
                            +
                            "FROM Bus b " +
                            "LEFT JOIN Staff s ON b.bus_id = s.assigned_bus " +
                            "LEFT JOIN Role r ON s.role_id = r.role_id " +
                            "WHERE b.status NOT IN ('In Transit') " +
                            "GROUP BY b.bus_id " +
                            "ORDER BY b.bus_number");

            ResultSet busesRs = busesStmt.executeQuery();
            List<Map<String, Object>> buses = new ArrayList<>();

            while (busesRs.next()) {
                Map<String, Object> bus = new HashMap<>();
                bus.put("bus_id", busesRs.getInt("bus_id"));
                bus.put("bus_number", busesRs.getString("bus_number"));
                bus.put("status", busesRs.getString("status"));
                bus.put("assigned_staff", busesRs.getString("assigned_staff"));
                buses.add(bus);
            }

            reassignableRs.close();
            reassignableStmt.close();
            busesRs.close();
            busesStmt.close();
            conn.close();

            request.setAttribute("unassignedStaff", unassignedStaff);
            request.setAttribute("buses", buses);
            request.setAttribute("showReassignment", true);
            request.getRequestDispatcher("/admin/manage_staff.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Error loading reassignment data: " + e.getMessage());
            listStaff(request, response);
        }
    }

    private void processReassignment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int staffID = Integer.parseInt(request.getParameter("staffID"));
            int busID = Integer.parseInt(request.getParameter("busID"));

            Connection conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                // Get staff details
                PreparedStatement staffStmt = conn.prepareStatement(
                        "SELECT s.staff_id, s.staff_name, s.shift, r.role_name " +
                                "FROM Staff s " +
                                "JOIN Role r ON s.role_id = r.role_id " +
                                "WHERE s.staff_id = ?");
                staffStmt.setInt(1, staffID);
                ResultSet staffRs = staffStmt.executeQuery();

                if (!staffRs.next()) {
                    throw new Exception("Staff not found");
                }

                String staffName = staffRs.getString("staff_name");
                String role = staffRs.getString("role_name");
                String shift = staffRs.getString("shift");
                staffRs.close();
                staffStmt.close();

                // Check if bus is in transit
                PreparedStatement busCheckStmt = conn.prepareStatement(
                        "SELECT status FROM Bus WHERE bus_id = ?");
                busCheckStmt.setInt(1, busID);
                ResultSet busRs = busCheckStmt.executeQuery();

                if (busRs.next() && "In Transit".equals(busRs.getString("status"))) {
                    throw new Exception("Cannot reassign to a bus that is in transit");
                }
                busRs.close();
                busCheckStmt.close();

                // Check for existing staff with same role and shift on this bus
                PreparedStatement conflictStmt = conn.prepareStatement(
                        "SELECT s.staff_id, s.staff_name " +
                                "FROM Staff s " +
                                "JOIN Role r ON s.role_id = r.role_id " +
                                "WHERE s.assigned_bus = ? AND r.role_name = ? AND s.shift = ?");
                conflictStmt.setInt(1, busID);
                conflictStmt.setString(2, role);
                conflictStmt.setString(3, shift);
                ResultSet conflictRs = conflictStmt.executeQuery();

                String replacedStaffName = null;
                if (conflictRs.next()) {
                    int replacedStaffID = conflictRs.getInt("staff_id");
                    replacedStaffName = conflictRs.getString("staff_name");

                    // Set the replaced staff's assigned_bus to null
                    PreparedStatement removeStmt = conn.prepareStatement(
                            "UPDATE Staff SET assigned_bus = NULL WHERE staff_id = ?");
                    removeStmt.setInt(1, replacedStaffID);
                    removeStmt.executeUpdate();
                    removeStmt.close();
                }
                conflictRs.close();
                conflictStmt.close();

                // Assign new staff to bus
                PreparedStatement assignStmt = conn.prepareStatement(
                        "UPDATE Staff SET assigned_bus = ? WHERE staff_id = ?");
                assignStmt.setInt(1, busID);
                assignStmt.setInt(2, staffID);
                assignStmt.executeUpdate();
                assignStmt.close();

                conn.commit();

                String message = "Successfully assigned " + staffName + " to bus";
                if (replacedStaffName != null) {
                    message += " (replaced " + replacedStaffName + ")";
                }
                request.setAttribute("message", message);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }

            response.sendRedirect("staff?action=reassign");

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Reassignment failed: " + e.getMessage());
            showReassignmentSection(request, response);
        }
    }

    private Map<Integer, String> getRoleNames() throws SQLException {
        Map<Integer, String> map = new HashMap<>();

        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT role_id, role_name FROM role");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            map.put(rs.getInt("role_id"), rs.getString("role_name"));
        }
        rs.close();
        ps.close();
        conn.close();
        return map;
    }

    private Map<Integer, String> getTerminalNames() throws SQLException {
        Map<Integer, String> map = new HashMap<>();

        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT terminal_id, terminal_name FROM terminal");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            map.put(rs.getInt("terminal_id"), rs.getString("terminal_name"));
        }
        rs.close();
        ps.close();
        conn.close();
        return map;
    }

    private Map<Integer, String> getBusNames() throws SQLException {
        Map<Integer, String> map = new HashMap<>();

        Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT bus_id, bus_number FROM bus");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            map.put(rs.getInt("bus_id"), rs.getString("bus_number"));
        }
        rs.close();
        ps.close();
        conn.close();
        return map;
    }
}

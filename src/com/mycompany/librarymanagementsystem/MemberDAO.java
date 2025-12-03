package com.mycompany.librarymanagementsystem;

import java.sql.*;

public class MemberDAO {

    public static void addMember(String name, String email, String password, String role) {
        Connection conn = DBConnection.connect();
        String sql = "INSERT INTO members(name, email, password, role) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean login(String email, String password) {
        Connection conn = DBConnection.connect();
        String sql = "SELECT 1 FROM members WHERE email=? AND password=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getRole(String email) {
        Connection conn = DBConnection.connect();
        String sql = "SELECT role FROM members WHERE email=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("role");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}

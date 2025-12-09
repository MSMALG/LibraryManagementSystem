/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.librarymanagementsystem;

/**
 *
 * @author user
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    /*public static void addNotification(int memberId, String message) throws SQLException {
        Connection conn = DBConnection.connect();
        String sql = "INSERT INTO notifications(member_id, message, created_at, read) VALUES(?, ?, datetime('now'), 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setString(2, message);
            ps.executeUpdate();
        }
    }*/
    public static void addNotification(int memberId, String message) throws SQLException {
        Connection conn = DBConnection.connect();
        String sql = "INSERT INTO notifications(member_id, message, created_at, read) " +
                     "VALUES(?, ?, datetime('now'), 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setString(2, message);
            ps.executeUpdate();
        }
    }

    public static List<String> getUnreadNotifications(int memberId) throws SQLException {
        Connection conn = DBConnection.connect();
        List<String> out = new ArrayList<>();
        String sql = "SELECT notification_id, message FROM notifications WHERE member_id=? AND read=0 ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getString("message"));
                }
            }
        }
        return out;
    }
       /**
     * Gets the count of unread notifications for a specific member.
     */
    
    public static void markAllRead(int memberId) throws SQLException {
        Connection conn = DBConnection.connect();
        String sql = "UPDATE notifications SET read=1 WHERE member_id=?"; 
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.executeUpdate();
        }
    }
    
    public static int getUnreadNotificationCount(int memberId) throws SQLException {
        int count = 0;
        // Using 'read' column name and check for 0 (unread)
        String sql = "SELECT COUNT(*) AS count FROM notifications WHERE member_id = ? AND read = 0"; 
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("count");
                }
            }
        }
        return count;
    }
}


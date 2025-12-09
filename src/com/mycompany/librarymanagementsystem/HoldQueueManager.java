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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset; // Import this
import java.time.ZonedDateTime; // Import this

public class HoldQueueManager {
    //private static final int NOTIFY_HOURS = 48;
    private static final int NOTIFY_HOURS = 0; // 0 hours
    private static final int NOTIFY_MINUTES = 2; 
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * Notify the next waiting member for a book (if any).
     * Marks hold.status = 'NOTIFIED' and sets expires_at.
     */
public static void notifyNext(int bookId) throws SQLException {
    HoldDAO.Hold next = HoldDAO.findNextWaiting(bookId);
    if (next == null) return;
    /*String expiresAt = LocalDateTime.now()
        .plusMinutes(NOTIFY_MINUTES)
        .format(FMT);*/
    String expiresAt = ZonedDateTime.now(ZoneOffset.UTC) // Get current time in UTC
            .plusMinutes(NOTIFY_MINUTES)
            .format(FMT); 
    HoldDAO.markNotified(next.holdId, expiresAt);
    String msg = "Your reserved book (ID: " + bookId + ") is available. Pick up before " + expiresAt;
    NotificationDAO.addNotification(next.memberId, msg);
}
    /**
     * Cancel a hold (set CANCELLED)
     */
    public static void cancelHold(int holdId) throws SQLException {
        HoldDAO.cancelHold(holdId);
    }
  
    public static void expireNotifiedAndAdvanceForAllBooks() throws SQLException {
        Connection conn = DBConnection.connect();
        String sql = "SELECT DISTINCT book_id FROM holds WHERE status='NOTIFIED' AND expires_at < datetime('now')";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                expireNotifiedAndAdvance(bookId);
            }
        }
    }
    private static void expireNotifiedAndAdvance(int bookId) throws SQLException {
        Connection conn = DBConnection.connect();
        // get expired notified holds
        String sel = "SELECT hold_id FROM holds WHERE book_id=? AND status='NOTIFIED' AND expires_at < datetime('now')";
        try (PreparedStatement ps = conn.prepareStatement(sel)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int expiredHoldId = rs.getInt("hold_id");
                    HoldDAO.cancelHold(expiredHoldId);
                }
            }
        }
        notifyNext(bookId);
    }
} 
package com.mycompany.librarymanagementsystem;
import java.sql.*;
public class HoldDAO {
    public static class Hold {
        public int holdId;
        public int memberId;
        public int bookId;
        public String status;
        public Hold(int holdId, int memberId, int bookId, String status) {
            this.holdId = holdId;
            this.memberId = memberId;
            this.bookId = bookId;
            this.status = status;
        }
    }
    // Place a new hold in the queue
    public static void placeHold(int memberId, int bookId) {
        Connection conn = DBConnection.connect();
        String posSql = "SELECT COALESCE(MAX(position), 0) + 1 AS nextPos FROM holds WHERE book_id=?";
        String insSql = "INSERT INTO holds(member_id, book_id, position) VALUES(?,?,?)";
        try (PreparedStatement ps1 = conn.prepareStatement(posSql)) {
            ps1.setInt(1, bookId);
            try (ResultSet rs = ps1.executeQuery()) {
                int position = rs.getInt("nextPos");
                try (PreparedStatement ps2 = conn.prepareStatement(insSql)) {
                    ps2.setInt(1, memberId);
                    ps2.setInt(2, bookId);
                    ps2.setInt(3, position);
                    ps2.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Find the next WAITING hold for a book
   public static Hold findNextWaiting(int bookId) {
        String sql = "SELECT * FROM holds WHERE book_id=? AND status='WAITING' ORDER BY position ASC LIMIT 1";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Hold(
                        rs.getInt("hold_id"),
                        rs.getInt("member_id"),
                        rs.getInt("book_id"),
                        rs.getString("status")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
/*public static Hold findNextWaiting(int bookId) {
    String sql = "SELECT * FROM holds WHERE book_id = ? AND status NOT IN ('NO_SHOW', 'PICKED_UP') ORDER BY position ASC LIMIT 1";
    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, bookId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Hold(
                    rs.getInt("hold_id"),
                    rs.getInt("member_id"),
                    rs.getInt("book_id"),
                    rs.getString("status")
                );
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}*/
    // Mark a hold as notified and set expiration
    public static void markNotified(int holdId, String expiresAt) {
        String sql = "UPDATE holds SET status='NOTIFIED', notified_at=datetime('now'), expires_at=? WHERE hold_id=?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, expiresAt);
            ps.setInt(2, holdId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Remove expired NOTIFIED holds so the next in queue can be notified
    public static void removeExpiredHolds() {
        String sql = "DELETE FROM holds WHERE status='NOTIFIED' AND expires_at < datetime('now')";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Cancel a specific hold
    public static void cancelHold(int holdId) {
        String sql = "DELETE FROM holds WHERE hold_id=?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, holdId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 
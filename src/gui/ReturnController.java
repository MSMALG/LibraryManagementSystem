/*package gui;

import com.mycompany.librarymanagementsystem.DBConnection;
import com.mycompany.librarymanagementsystem.HoldQueueManager;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReturnController {

    public boolean tryReturn(String userEmail, int bookId) {
        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                JOptionPaneHelper.showError("DB connection failed.");
                return false;
            }

            conn.setAutoCommit(false);

            Integer memberId = getMemberId(conn, userEmail);
            if (memberId == null) {
                JOptionPaneHelper.showError("Member not found.");
                return false;
            }

            PreparedStatement ps = conn.prepareStatement("""
                SELECT l.loan_id, l.copy_id, l.due_date
                FROM loans l JOIN copies c ON l.copy_id = c.copy_id
                WHERE l.member_id = ? AND c.book_id = ? AND l.return_date IS NULL
                LIMIT 1
            """);
            ps.setInt(1, memberId);
            ps.setInt(2, bookId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPaneHelper.showInfo("No active loan to return.");
                return false;
            }

            int loanId = rs.getInt("loan_id");
            int copyId = rs.getInt("copy_id");
            LocalDate due = LocalDate.parse(rs.getString("due_date"));
            LocalDate today = LocalDate.now();

            long fine = 0;
            if (today.isAfter(due)) {
                fine = ChronoUnit.DAYS.between(due, today) * 2; // $2 per day
            }

            // Update loan as returned
            PreparedStatement ups = conn.prepareStatement(
                    "UPDATE loans SET return_date=? WHERE loan_id=?");
            ups.setString(1, today.toString());
            ups.setInt(2, loanId);
            ups.executeUpdate();

            // Mark copy as available
            PreparedStatement ups2 = conn.prepareStatement(
                    "UPDATE copies SET status='available' WHERE copy_id=?");
            ups2.setInt(1, copyId);
            ups2.executeUpdate();

            // Add fine if any
            if (fine > 0) {
                PreparedStatement finePS = conn.prepareStatement("""
                    INSERT INTO fines(member_id, loan_id, amount)
                    VALUES (?, ?, ?)
                """);
                finePS.setInt(1, memberId);
                finePS.setInt(2, loanId);
                finePS.setLong(3, fine);
                finePS.executeUpdate();
            }

            conn.commit();
            
            // SIMPLE FIX: Just call notifyNext - let it handle its own connection
            try {
                HoldQueueManager.notifyNext(bookId);
            } catch (Exception e) {
                System.err.println("Notification failed (but return succeeded): " + e.getMessage());
                // Don't fail the return if notification fails
            }
            
            JOptionPaneHelper.showInfo("Return processed successfully.");
            return true;

        } catch (Exception e) {
            JOptionPaneHelper.showError("Return failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }*/
package gui;

import com.mycompany.librarymanagementsystem.DBConnection;
import com.mycompany.librarymanagementsystem.HoldQueueManager;
import java.sql.*;
import java.time.ZonedDateTime; // Use ZonedDateTime instead of LocalDate
import java.time.format.DateTimeFormatter; // Need this formatter
import java.time.temporal.ChronoUnit;
import java.time.ZoneOffset;

public class ReturnController {

    // Define the formatter used for storage/retrieval
    private static final DateTimeFormatter DB_FMT = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    public boolean tryReturn(String userEmail, int bookId) {
        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                JOptionPaneHelper.showError("DB connection failed.");
                return false;
            }

            conn.setAutoCommit(false);

            Integer memberId = getMemberId(conn, userEmail);
            if (memberId == null) {
                JOptionPaneHelper.showError("Member not found.");
                return false;
            }

            PreparedStatement ps = conn.prepareStatement("""
                SELECT l.loan_id, l.copy_id, l.due_date
                FROM loans l JOIN copies c ON l.copy_id = c.copy_id
                WHERE l.member_id = ? AND c.book_id = ? AND l.return_date IS NULL
                LIMIT 1
            """);
            ps.setInt(1, memberId);
            ps.setInt(2, bookId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPaneHelper.showInfo("No active loan to return.");
                return false;
            }

            int loanId = rs.getInt("loan_id");
            int copyId = rs.getInt("copy_id");
            
            // FIX 1: Parse the full timestamp using ZonedDateTime
            ZonedDateTime due = ZonedDateTime.parse(rs.getString("due_date"), DB_FMT);
            ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC); // Get current time in UTC

            // FIX 2: Calculate fine based on minutes
            double fineAmount = 0;
            if (today.isAfter(due)) {
                // Calculate difference in minutes, assuming $0.10 per minute from my previous example
                long minutesLate = ChronoUnit.MINUTES.between(due, today);
                fineAmount = minutesLate * 0.10; 
            }

            // Update loan as returned
            PreparedStatement ups = conn.prepareStatement(
                    "UPDATE loans SET return_date=? WHERE loan_id=?");
            // FIX 3: Store the return date in the same full timestamp format
            ups.setString(1, today.format(DB_FMT));
            ups.setInt(2, loanId);
            ups.executeUpdate();

            // Mark copy as available
            PreparedStatement ups2 = conn.prepareStatement(
                    "UPDATE copies SET status='available' WHERE copy_id=?");
            ups2.setInt(1, copyId);
            ups2.executeUpdate();

            // Add fine if any
            if (fineAmount > 0) {
                PreparedStatement finePS = conn.prepareStatement("""
                    INSERT INTO fines(member_id, amount) VALUES (?, ?)
                """);
                finePS.setInt(1, memberId);
                // finePS.setInt(2, loanId); <-- Removed loan_id as per your DB setup (fines table definition)
                finePS.setDouble(2, fineAmount); // Use setDouble for real values
                finePS.executeUpdate();
                
                // Add a notification/pop-up here if desired (as discussed previously)
                JOptionPaneHelper.showInfo(String.format("Book was returned late by %d minutes. A fine of $%.2f has been applied.", ChronoUnit.MINUTES.between(due, today), fineAmount));
            }

            conn.commit();
            
            // SIMPLE FIX: Just call notifyNext - let it handle its own connection
            try {
                HoldQueueManager.notifyNext(bookId);
            } catch (Exception e) {
                System.err.println("Notification failed (but return succeeded): " + e.getMessage());
            }
            
            if (fineAmount <= 0) {
                JOptionPaneHelper.showInfo("Return processed successfully.");
            }
            return true;

        } catch (Exception e) {
            JOptionPaneHelper.showError("Return failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private Integer getMemberId(Connection conn, String email) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT member_id FROM members WHERE email=?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("member_id");
        return null;
    }
    
    // Keep these cleanup methods as they were working
    public static void cleanupExpiredHoldsAndAdvanceQueue(int bookId) {
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                 "UPDATE holds SET status = 'NO_SHOW' " +
                 "WHERE book_id = ? AND status = 'NOTIFIED' AND datetime(expires_at) < datetime('now')")) {
                ps.setInt(1, bookId);
                int updated = ps.executeUpdate();
                System.out.println("Marked " + updated + " holds as NO_SHOW");
            }

            if (hasAvailableCopy(bookId)) {
                HoldQueueManager.notifyNext(bookId);
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void cleanupAllExpiredHolds() {
        try (Connection conn = DBConnection.connect()) {
            try (PreparedStatement ps = conn.prepareStatement(
                 "UPDATE holds SET status = 'NO_SHOW' " +
                 "WHERE status = 'NOTIFIED' AND datetime(expires_at) < datetime('now')")) {
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    System.out.println("Auto-cleaned " + updated + " expired holds");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static boolean hasAvailableCopy(int bookId) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT 1 FROM copies WHERE book_id = ? AND status = 'available' LIMIT 1")) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }
}
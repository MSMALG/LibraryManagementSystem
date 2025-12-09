package gui;

import com.mycompany.librarymanagementsystem.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset; 
import java.time.ZonedDateTime; 

public class BorrowController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int LOAN_DAYS = 14;
    private static final int LOAN_MINS = 2;

    private static final DateTimeFormatter SQLITE_DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 

    public boolean tryBorrow(String userEmail, int bookId) {
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

            int copyId = getAvailableCopy(conn, bookId);
            if (copyId == -1) {
                JOptionPaneHelper.showInfo("No available copies. A hold has been placed.");
                placeHold(conn, memberId, bookId);
                conn.commit();
                return false;
            }

            //String today = LocalDate.now().format(FMT);
            //String due = LocalDate.now().plusDays(LOAN_DAYS).format(FMT);
            String today = ZonedDateTime.now().format(SQLITE_DB_FORMATTER); 
            String due = ZonedDateTime.now().plusMinutes(LOAN_MINS).format(SQLITE_DB_FORMATTER);

           try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO loans(member_id, copy_id, loan_date, due_date) VALUES(?,?,?,?)")) {
            ps.setInt(1, memberId);
            ps.setInt(2, copyId);
            ps.setString(3, today); 
            ps.setString(4, due);   
            ps.executeUpdate();
            System.out.println("DEBUG: Inserted Loan Due Date: " + due);
        }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE copies SET status='borrowed' WHERE copy_id=?")) {
                ps.setInt(1, copyId);
                ps.executeUpdate();
            }
            
            try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE holds SET status = 'PICKED_UP' WHERE member_id = ? AND book_id = ? AND status = 'NOTIFIED'")) {
               ps.setInt(1, memberId);
               ps.setInt(2, bookId);
               ps.executeUpdate();
           }

            conn.commit();
            return true;

        } catch (Exception e) {
            JOptionPaneHelper.showError("Borrow failed: " + e.getMessage());
            return false;
        }
    }

    public boolean tryRenew(String userEmail, int bookId) {
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
                SELECT l.loan_id, l.copy_id
                FROM loans l JOIN copies c ON l.copy_id=c.copy_id
                WHERE l.member_id=? AND c.book_id=? AND l.return_date IS NULL
                LIMIT 1
            """);
            ps.setInt(1, memberId);
            ps.setInt(2, bookId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPaneHelper.showInfo("No active loan to renew.");
                return false;
            }

            int loanId = rs.getInt("loan_id");

            String newDue = LocalDate.now().plusDays(LOAN_DAYS).format(FMT);

            PreparedStatement ups = conn.prepareStatement(
                    "UPDATE loans SET due_date=? WHERE loan_id=?");
            ups.setString(1, newDue);
            ups.setInt(2, loanId);
            ups.executeUpdate();

            conn.commit();
            JOptionPaneHelper.showInfo("Renewed! New due date: " + newDue);
            return true;

        } catch (Exception e) {
            JOptionPaneHelper.showError("Renew failed: " + e.getMessage());
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

    private int getAvailableCopy(Connection conn, int bookId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT copy_id FROM copies WHERE book_id=? AND status='available' LIMIT 1");
        ps.setInt(1, bookId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("copy_id");
        return -1;
    }

    private void placeHold(Connection conn, int memberId, int bookId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO holds(member_id, book_id, position)
            VALUES(?, ?, COALESCE((SELECT MAX(position)+1 FROM holds WHERE book_id=?), 1))
        """);
        ps.setInt(1, memberId);
        ps.setInt(2, bookId);
        ps.setInt(3, bookId);
        ps.executeUpdate();
    }
}

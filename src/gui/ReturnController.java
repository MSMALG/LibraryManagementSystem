package gui;

import com.mycompany.librarymanagementsystem.DBConnection;
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

            // ابحث عن استعارة نشطة لنفس العضو ونفس الكتاب
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

            // احسب الغرامة
            long fine = 0;
            if (today.isAfter(due)) {
                fine = ChronoUnit.DAYS.between(due, today) * 2; // غرامة: 2 ريال لكل يوم
            }

            // سجل تاريخ الإرجاع
            PreparedStatement ups = conn.prepareStatement(
                    "UPDATE loans SET return_date=? WHERE loan_id=?");
            ups.setString(1, today.toString());
            ups.setInt(2, loanId);
            ups.executeUpdate();

            // رجّع حالة النسخة متاحة
            PreparedStatement ups2 = conn.prepareStatement(
                    "UPDATE copies SET status='available' WHERE copy_id=?");
            ups2.setInt(1, copyId);
            ups2.executeUpdate();

            // إذا فيه غرامة سجلها
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
            JOptionPaneHelper.showInfo("Return processed successfully.");
            return true;

        } catch (Exception e) {
            JOptionPaneHelper.showError("Return failed: " + e.getMessage());
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
}

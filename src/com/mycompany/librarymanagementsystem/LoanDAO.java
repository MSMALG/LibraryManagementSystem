package com.mycompany.librarymanagementsystem;
import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.ZoneOffset;

public class LoanDAO {
    public static void borrowBook(int memberId, int copyId, String loanDate, String dueDate) {
        Connection conn = DBConnection.connect();
        String insSql = "INSERT INTO loans(member_id, copy_id, loan_date, due_date) VALUES(?,?,?,?)";
        String updSql = "UPDATE copies SET status='borrowed' WHERE copy_id=?";
        try (PreparedStatement ps1 = conn.prepareStatement(insSql);
             PreparedStatement ps2 = conn.prepareStatement(updSql)) {
            ps1.setInt(1, memberId);
            ps1.setInt(2, copyId);
            ps1.setString(3, loanDate);
            ps1.setString(4, dueDate);
            ps1.executeUpdate();
            ps2.setInt(1, copyId);
            ps2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     public static void returnBook(int copyId, int loanId, ZonedDateTime returnDateTime) {
        Connection conn = null;
        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false); // Start transaction

            // 1. Fetch the due date and member ID from the loan record
            String selectSql = "SELECT due_date, member_id FROM loans WHERE loan_id=?";
            ZonedDateTime dueDate = null;
            int memberId = -1;

            try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setInt(1, loanId);
                ResultSet rs = psSelect.executeQuery();
                if (rs.next()) {
                    // Parse the stored date string back into a ZonedDateTime object for comparison
                    // You need to match the format used in BorrowController
                    dueDate = ZonedDateTime.parse(rs.getString("due_date"), DateTimeFormatter.ISO_ZONED_DATE_TIME);
                    memberId = rs.getInt("member_id");
                }
            }

            // 2. Calculate fine if overdue
            if (dueDate != null && returnDateTime.isAfter(dueDate)) {
                // Calculate difference in minutes for testing (or days for production)
                // Using ChronoUnit.MINUTES for your 3-minute test case
                long minutesLate = ChronoUnit.MINUTES.between(dueDate, returnDateTime);
                double fineAmount = minutesLate * 0.10; // $0.10 per minute late

                if (fineAmount > 0) {
                    String insertFineSql = "INSERT INTO fines(member_id, amount) VALUES(?,?)";
                    try (PreparedStatement psFine = conn.prepareStatement(insertFineSql)) {
                        psFine.setInt(1, memberId);
                        psFine.setDouble(2, fineAmount);
                        psFine.executeUpdate();
                        System.out.println("Generated fine of $" + fineAmount + " for member " + memberId);
                    }
                }
            }

            // 3. Update the loan record with the return date
            String updLoan = "UPDATE loans SET return_date=? WHERE loan_id=?";
            try (PreparedStatement ps1 = conn.prepareStatement(updLoan)) {
                // Store the return date in the same ISO format  used for the due date
                ps1.setString(1, returnDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
                ps1.setInt(2, loanId);
                ps1.executeUpdate();
            }

            // 4. Update the copy status
            String updCopy = "UPDATE copies SET status='available' WHERE copy_id=?";
            try (PreparedStatement ps2 = conn.prepareStatement(updCopy)) {
                ps2.setInt(1, copyId);
                ps2.executeUpdate();
            }

            conn.commit(); // Commit all changes
            System.out.println("Book successfully returned.");

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

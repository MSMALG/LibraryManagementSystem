package com.mycompany.librarymanagementsystem;
import java.sql.*;
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
    public static void returnBook(int copyId, int loanId, String returnDate) {
        Connection conn = DBConnection.connect();
        String updLoan = "UPDATE loans SET return_date=? WHERE loan_id=?";
        String updCopy = "UPDATE copies SET status='available' WHERE copy_id=?";
        try (PreparedStatement ps1 = conn.prepareStatement(updLoan);
             PreparedStatement ps2 = conn.prepareStatement(updCopy)) {
            ps1.setString(1, returnDate);
            ps1.setInt(2, loanId);
            ps1.executeUpdate();
            ps2.setInt(1, copyId);
            ps2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package gui;
import com.mycompany.librarymanagementsystem.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
public class LoansPanel extends JPanel {
    private LibraryMainFrame parent;
    private JTable loansTable;
    private DefaultTableModel loansModel;
    private JButton returnBtn;
    private JButton renewBtn;
    private JButton returnWindowBtn;
   
    private String currentUserEmail;
    private ReturnController returnController;
    private BorrowController borrowController; // reuse renew from borrowController
    public LoansPanel(LibraryMainFrame parent) {
        this.parent = parent;
        this.returnController = new ReturnController();
        this.borrowController = new BorrowController();
        setLayout(new BorderLayout(8,8));
        loansModel = new DefaultTableModel(new Object[]{"loan_id","Title","Copy ID","Loan Date","Due Date"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        loansTable = new JTable(loansModel);
        add(new JScrollPane(loansTable), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,8));
        returnBtn = new JButton("Return Selected");
        renewBtn = new JButton("Renew Selected");
        returnWindowBtn= new JButton("Return Back");
        bottom.add(returnBtn);
        bottom.add(renewBtn);
        bottom.add(returnWindowBtn);
        add(bottom, BorderLayout.SOUTH);
        returnBtn.addActionListener(e -> handleReturn());
        renewBtn.addActionListener(e -> handleRenew());
        returnWindowBtn.addActionListener(e -> ReturnWindow() );
    }
    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }
    public void reloadLoans() {
        loansModel.setRowCount(0);
        if (currentUserEmail == null) return;
        try (Connection conn = DBConnection.connect()) {
            // get member id
            PreparedStatement mps = conn.prepareStatement("SELECT member_id FROM members WHERE email=?");
            mps.setString(1, currentUserEmail);
            ResultSet mrs = mps.executeQuery();
            if (!mrs.next()) return;
            int memberId = mrs.getInt("member_id");
            String sql = """
                SELECT l.loan_id, c.copy_id, b.title, l.loan_date, l.due_date
                FROM loans l
                JOIN copies c ON l.copy_id = c.copy_id
                JOIN books b ON c.book_id = b.book_id
                WHERE l.member_id=? AND l.return_date IS NULL
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                loansModel.addRow(new Object[]{
                    rs.getInt("loan_id"),
                    rs.getString("title"),
                    rs.getInt("copy_id"),
                    rs.getString("loan_date"),
                    rs.getString("due_date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading loans: " + e.getMessage());
        }
    }
    private Integer getSelectedLoanId() {
        int row = loansTable.getSelectedRow();
        if (row == -1) return null;
        Object value = loansModel.getValueAt(row, 0);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                System.err.println("Error parsing loan_id: " + value + " is not a valid integer.");
                return null;
            }
        }
        return null;
    }
    private Integer getSelectedCopyId() {
        int row = loansTable.getSelectedRow();
        if (row == -1) return null;
        Object value = loansModel.getValueAt(row, 2); // Copy ID is at column 2
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                System.err.println("Error parsing copy_id: " + value + " is not a valid integer.");
                return null;
            }
        }
        return null;
    }
    private void handleReturn() {
        Integer loanId = getSelectedLoanId();
        if (loanId == null) {
            JOptionPane.showMessageDialog(this, "Please select a loan first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Integer selectedBookIdFromTable = null;
        try (Connection conn = DBConnection.connect()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT b.book_id
                FROM loans l
                JOIN copies c ON l.copy_id = c.copy_id
                JOIN books b ON c.book_id = b.book_id
                WHERE l.loan_id = ?
            """);
            ps.setInt(1, loanId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                selectedBookIdFromTable = rs.getInt("book_id");
            } else {
                JOptionPane.showMessageDialog(this, "Could not find book ID for the selected loan.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error retrieving book ID for return: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedBookIdFromTable == null) {
            JOptionPane.showMessageDialog(this, "Unable to process return. Book ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean ok = returnController.tryReturn(currentUserEmail, selectedBookIdFromTable);
        if (ok) {
            // JOptionPane.showMessageDialog(this, "Returned successfully.");
            reloadLoans();
        }
    }
    private void handleRenew() {
        Integer loanId = getSelectedLoanId();
        if (loanId == null) {
            JOptionPane.showMessageDialog(this, "Please select a loan first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // we need the book_id to call tryRenew; look it up
        Integer copyId = getSelectedCopyId();
        if (copyId == null) return;
        try (Connection conn = DBConnection.connect()) {
            PreparedStatement ps = conn.prepareStatement("SELECT book_id FROM copies WHERE copy_id=?");
            ps.setInt(1, copyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int bookId = rs.getInt("book_id");
                boolean ok = borrowController.tryRenew(currentUserEmail, bookId);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Renewed successfully.");
                    reloadLoans();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Could not find book for the selected copy.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Renew failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
   
    private void ReturnWindow(){
    String userName = parent.getCurrentUserName();
    String role = parent.getCurrentUserRole();
    parent.showHome(userName, role, currentUserEmail);
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;
/**
 *
 * @author user
 */
import com.mycompany.librarymanagementsystem.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
public class AdminPanel extends JPanel {
    private LibraryMainFrame parent;
    private JTabbedPane tabs;
    // Books tab
    private JTable booksTable;
    private DefaultTableModel booksModel;
    private JButton addBookBtn, editBookBtn, delBookBtn, refreshBooksBtn;
    // Members tab
    private JTable membersTable;
    private DefaultTableModel membersModel;
    private JButton addMemberBtn, editMemberBtn, delMemberBtn, refreshMembersBtn;
    // Holds tab
    private JTable holdsTable;
    private DefaultTableModel holdsModel;
    private JButton notifyNextBtn, cancelHoldBtn, refreshHoldsBtn;
    // Reports tab
    private JButton exportTopBorrowedBtn, exportOverdueBtn, exportFinesBtn;
    public AdminPanel(LibraryMainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        tabs = new JTabbedPane();
        initBooksTab();
        initMembersTab();
        initHoldsTab();
        initReportsTab();
        initCopiesTab();
        add(tabs, BorderLayout.CENTER);
    }
    private void initBooksTab() {
        JPanel p = new JPanel(new BorderLayout());
        booksModel = new DefaultTableModel(new Object[]{"book_id","Title","Author","ISBN","Category"}, 0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        booksTable = new JTable(booksModel);
        p.add(new JScrollPane(booksTable), BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addBookBtn = new JButton("Add Book");
        editBookBtn = new JButton("Edit Book");
        delBookBtn = new JButton("Delete Book");
        refreshBooksBtn = new JButton("Refresh");
        btns.add(addBookBtn); btns.add(editBookBtn); btns.add(delBookBtn); btns.add(refreshBooksBtn);
        p.add(btns, BorderLayout.SOUTH);
        refreshBooksBtn.addActionListener(e -> loadBooks());
        addBookBtn.addActionListener(e -> showBookDialog(null));
        editBookBtn.addActionListener(e -> {
            int r = booksTable.getSelectedRow();
            if (r == -1) return;
            int bookId = (Integer) booksModel.getValueAt(r, 0);
            showBookDialog(bookId);
        });
        delBookBtn.addActionListener(e -> {
            int r = booksTable.getSelectedRow();
            if (r == -1) return;
            int bookId = (Integer) booksModel.getValueAt(r, 0);
            int ok = JOptionPane.showConfirmDialog(this, "Delete book ID " + bookId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            try {
                Connection conn = DBConnection.connect();
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE book_id=?")) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }
                loadBooks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
            }
        });
        tabs.addTab("Books", p);
        loadBooks();
    }
    private void showBookDialog(Integer bookId) {
        // dialog for add/edit
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField categoryField = new JTextField();
        if (bookId != null) {
            try {
                Connection conn = DBConnection.connect();
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM books WHERE book_id=?")) {
                    ps.setInt(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            titleField.setText(rs.getString("title"));
                            authorField.setText(rs.getString("author"));
                            isbnField.setText(rs.getString("isbn"));
                            categoryField.setText(rs.getString("category"));
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Load book failed: " + ex.getMessage());
                return;
            }
        }
        JPanel p = new JPanel(new GridLayout(4,2,6,6));
        p.add(new JLabel("Title:")); p.add(titleField);
        p.add(new JLabel("Author:")); p.add(authorField);
        p.add(new JLabel("ISBN:")); p.add(isbnField);
        p.add(new JLabel("Category:")); p.add(categoryField);
        int res = JOptionPane.showConfirmDialog(this, p, bookId == null ? "Add Book" : "Edit Book", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            if (bookId == null) {
                BookDAO.addBook(titleField.getText(), authorField.getText(), isbnField.getText(), categoryField.getText());
            } else {
                Connection conn = DBConnection.connect();
                try (PreparedStatement ps = conn.prepareStatement("UPDATE books SET title=?, author=?, isbn=?, category=? WHERE book_id=?")) {
                    ps.setString(1, titleField.getText());
                    ps.setString(2, authorField.getText());
                    ps.setString(3, isbnField.getText());
                    ps.setString(4, categoryField.getText());
                    ps.setInt(5, bookId);
                    ps.executeUpdate();
                }
            }
            loadBooks();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }
    private void initMembersTab() {
        JPanel p = new JPanel(new BorderLayout());
        membersModel = new DefaultTableModel(new Object[]{"member_id","Name","Email","Role"}, 0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        membersTable = new JTable(membersModel);
        p.add(new JScrollPane(membersTable), BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addMemberBtn = new JButton("Add Member");
        editMemberBtn = new JButton("Edit Member");
        delMemberBtn = new JButton("Delete Member");
        refreshMembersBtn = new JButton("Refresh");
        btns.add(addMemberBtn); btns.add(editMemberBtn); btns.add(delMemberBtn); btns.add(refreshMembersBtn);
        p.add(btns, BorderLayout.SOUTH);
        refreshMembersBtn.addActionListener(e -> loadMembers());
        addMemberBtn.addActionListener(e -> showMemberDialog(null));
        editMemberBtn.addActionListener(e -> {
            int r = membersTable.getSelectedRow();
            if (r == -1) return;
            int memberId = (Integer) membersModel.getValueAt(r, 0);
            showMemberDialog(memberId);
        });
        delMemberBtn.addActionListener(e -> {
            int r = membersTable.getSelectedRow();
            if (r == -1) return;
            int memberId = (Integer) membersModel.getValueAt(r, 0);
            int ok = JOptionPane.showConfirmDialog(this, "Delete member ID " + memberId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            try {
                Connection conn = DBConnection.connect();
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM members WHERE member_id=?")) {
                    ps.setInt(1, memberId);
                    ps.executeUpdate();
                }
                loadMembers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
            }
        });
        tabs.addTab("Members", p);
        loadMembers();
    }
    private void showMemberDialog(Integer memberId) {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Member","Librarian"});
        if (memberId != null) {
            try {
                Connection conn = DBConnection.connect();
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM members WHERE member_id=?")) {
                    ps.setInt(1, memberId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            nameField.setText(rs.getString("name"));
                            emailField.setText(rs.getString("email"));
                            roleBox.setSelectedItem(rs.getString("role"));
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Load member failed: " + ex.getMessage());
                return;
            }
        }
        JPanel p = new JPanel(new GridLayout(3,2,6,6));
        p.add(new JLabel("Name:")); p.add(nameField);
        p.add(new JLabel("Email:")); p.add(emailField);
        p.add(new JLabel("Role:")); p.add(roleBox);
        int res = JOptionPane.showConfirmDialog(this, p, memberId == null ? "Add Member" : "Edit Member", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            if (memberId == null) {
                MemberDAO.addMember(nameField.getText(), emailField.getText(), "password123", (String) roleBox.getSelectedItem());
            } else {
                Connection conn = DBConnection.connect();
                try (PreparedStatement ps = conn.prepareStatement("UPDATE members SET name=?, email=?, role=? WHERE member_id=?")) {
                    ps.setString(1, nameField.getText());
                    ps.setString(2, emailField.getText());
                    ps.setString(3, (String) roleBox.getSelectedItem());
                    ps.setInt(4, memberId);
                    ps.executeUpdate();
                }
            }
            loadMembers();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }
    private void initHoldsTab() {
        JPanel p = new JPanel(new BorderLayout());
        holdsModel = new DefaultTableModel(new Object[]{"hold_id","book_id","member_id","position","status","notified_at","expires_at"}, 0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        holdsTable = new JTable(holdsModel);
        p.add(new JScrollPane(holdsTable), BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        notifyNextBtn = new JButton("Notify Next for Selected Book");
        cancelHoldBtn = new JButton("Cancel Selected Hold");
        refreshHoldsBtn = new JButton("Refresh");
        btns.add(notifyNextBtn); btns.add(cancelHoldBtn); btns.add(refreshHoldsBtn);
        p.add(btns, BorderLayout.SOUTH);
        refreshHoldsBtn.addActionListener(e -> loadHolds());
        notifyNextBtn.addActionListener(e -> {
    Integer bookId = getSelectedBookIdFromHolds();
    if (bookId == null) {
        JOptionPane.showMessageDialog(this, "Please select a hold row first.");
        return;
    }
    // Check if any copy is available
    boolean hasAvailableCopy = false;
    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement(
             "SELECT 1 FROM copies WHERE book_id = ? AND status = 'available' LIMIT 1")) {
        ps.setInt(1, bookId);
        try (ResultSet rs = ps.executeQuery()) {
            hasAvailableCopy = rs.next();
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    if (!hasAvailableCopy) {
        JOptionPane.showMessageDialog(this,
            "Cannot notify: No copies are available yet.\nWait for a return first.",
            "Action Blocked", JOptionPane.WARNING_MESSAGE);
        return;
    }
    try {
        HoldQueueManager.notifyNext(bookId);
        JOptionPane.showMessageDialog(this, "Next member notified!");
        loadHolds();
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Notify failed: " + ex.getMessage());
    }
    });
        cancelHoldBtn.addActionListener(e -> {
            int row = holdsTable.getSelectedRow();
            if (row == -1) return;
            Integer holdId = (Integer) holdsModel.getValueAt(row, 0);
            try {
                HoldQueueManager.cancelHold(holdId);
                loadHolds();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Cancel failed: " + ex.getMessage());
            }
        });
        tabs.addTab("Holds", p);
        loadHolds();
    }
    private void initReportsTab() {
        JPanel p = new JPanel(new GridLayout(3,1,8,8));
        exportTopBorrowedBtn = new JButton("Export Top Borrowed (CSV)");
        exportOverdueBtn = new JButton("Export Overdue (CSV)");
        exportFinesBtn = new JButton("Export Fines Summary (CSV)");
        p.add(exportTopBorrowedBtn); p.add(exportOverdueBtn); p.add(exportFinesBtn);
        exportTopBorrowedBtn.addActionListener(e -> doExportTop());
        exportOverdueBtn.addActionListener(e -> doExportOverdue());
        exportFinesBtn.addActionListener(e -> doExportFines());
        tabs.addTab("Reports", p);
    }
    private Integer getSelectedBookIdFromHolds() {
        int row = holdsTable.getSelectedRow();
        if (row == -1) return null;
        Object v = holdsModel.getValueAt(row, 1);
        if (v instanceof Integer) return (Integer) v;
        try { return Integer.parseInt(v.toString()); } catch (Exception ex) { return null; }
    }
   private void loadBooks() {
    booksModel.setRowCount(0); // clear table
    try {
        // Get all books as a list
        List<BookDAO.Book> books = BookDAO.searchBooksList(""); // empty keyword = all
        for (BookDAO.Book b : books) {
            booksModel.addRow(new Object[]{
                b.bookId,
                b.title,
                b.author,
                b.isbn,
                b.category
            });
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Failed loading books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    private void loadMembers() {
        membersModel.setRowCount(0);
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement("SELECT member_id, name, email, role FROM members");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                membersModel.addRow(new Object[]{
                    rs.getInt("member_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("role")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed loading members: " + e.getMessage());
        }
    }
    private void loadHolds() {
        holdsModel.setRowCount(0);
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement("SELECT hold_id, book_id, member_id, position, status, notified_at, expires_at FROM holds ORDER BY book_id, position");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                holdsModel.addRow(new Object[]{
                    rs.getInt("hold_id"),
                    rs.getInt("book_id"),
                    rs.getInt("member_id"),
                    rs.getInt("position"),
                    rs.getString("status"),
                    rs.getString("notified_at"),
                    rs.getString("expires_at")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed loading holds: " + e.getMessage());
        }
    }
    private void doExportTop() {
        try {
            List<String[]> rows = ReportsController.topBorrowedBooks(50);
            Path out = ReportsController.exportToCsv(rows, new String[]{"book_id","title","borrow_count"}, "top_borrowed.csv");
            JOptionPane.showMessageDialog(this, "Exported to " + out.toAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }
    private void doExportOverdue() {
        try {
            List<String[]> rows = ReportsController.overdueBooks();
            Path out = ReportsController.exportToCsv(rows, new String[]{"loan_id","member","title","due_date"}, "overdue.csv");
            JOptionPane.showMessageDialog(this, "Exported to " + out.toAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }
    private void doExportFines() {
        try {
            List<String[]> rows = ReportsController.finesSummary();
            Path out = ReportsController.exportToCsv(rows, new String[]{"member_id","name","total_fines"}, "fines.csv");
            JOptionPane.showMessageDialog(this, "Exported to " + out.toAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }
   
    private void initCopiesTab() {
    JPanel p = new JPanel(new BorderLayout());
    // Table showing all copies
    DefaultTableModel copiesModel = new DefaultTableModel(new Object[]{"copy_id", "book_id", "Title", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    JTable copiesTable = new JTable(copiesModel);
    p.add(new JScrollPane(copiesTable), BorderLayout.CENTER);
    // Buttons
    JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton addCopyBtn = new JButton("Add Copy of Selected Book");
    JButton refreshBtn = new JButton("Refresh");
    btns.add(addCopyBtn);
    btns.add(refreshBtn);
    p.add(btns, BorderLayout.SOUTH);
    // Load all copies
    Runnable loadCopies = () -> {
        copiesModel.setRowCount(0);
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT c.copy_id, c.book_id, b.title, c.status FROM copies c JOIN books b ON c.book_id = b.book_id ORDER BY c.book_id")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                copiesModel.addRow(new Object[]{
                    rs.getInt("copy_id"),
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("status")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading copies: " + ex.getMessage());
        }
    };
    refreshBtn.addActionListener(e -> loadCopies.run());
    addCopyBtn.addActionListener(e -> {
        // Get selected book from Books tab or prompt
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book in the Books tab first!");
            return;
        }
        int bookId = (Integer) booksModel.getValueAt(selectedRow, 0);
        int count = JOptionPane.showConfirmDialog(this,
            "Add one physical copy of this book?\nBook ID: " + bookId,
            "Add Copy", JOptionPane.YES_NO_OPTION);
        if (count == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO copies(book_id, status) VALUES(?, 'available')")) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Copy added successfully!");
                loadCopies.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to add copy: " + ex.getMessage());
            }
        }
    });
    tabs.addTab("Copies", p);
    loadCopies.run();
}
}
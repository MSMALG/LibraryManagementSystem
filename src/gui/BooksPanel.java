package gui;

import com.mycompany.librarymanagementsystem.BookDAO;
import com.mycompany.librarymanagementsystem.HoldDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.util.List;


public class BooksPanel extends JPanel {

    private LibraryMainFrame parent;
    private JTextField keywordField;
    private JComboBox<String> filterCombo;
    private JButton searchBtn;
    private JTable booksTable;
    private DefaultTableModel tableModel;

    private JButton borrowBtn;
    private JButton returnWindowBtn;
    private JLabel copiesInfoLabel;

    private SearchController searchController;
    private BorrowController borrowController;

    private String currentUserEmail;

    public BooksPanel(LibraryMainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(8,8));

        searchController = new SearchController();
        borrowController = new BorrowController();

        // top: search controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,8));
        keywordField = new JTextField(30);
        filterCombo = new JComboBox<>(new String[]{"All (Title/Author/ISBN)","Title","Author","ISBN","Category"});
        searchBtn = new JButton("Search");
        top.add(new JLabel("Search:"));
        top.add(keywordField);
        top.add(filterCombo);
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        // center: table
        tableModel = new DefaultTableModel(new Object[]{"book_id","Title","Author","ISBN","Category"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        booksTable = new JTable(tableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(booksTable), BorderLayout.CENTER);

        // bottom: actions
        JPanel bottom = new JPanel(new BorderLayout());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,8));
        borrowBtn = new JButton("Borrow Selected");
        returnWindowBtn= new JButton("Return Back");

        bottom.add(returnWindowBtn);
        add(bottom, BorderLayout.SOUTH);


        returnWindowBtn.addActionListener(e -> ReturnWindow() );
        
        
        btns.add(borrowBtn);
        btns.add(returnWindowBtn);
        
        copiesInfoLabel = new JLabel("Select a book to see copies info.");
        bottom.add(btns, BorderLayout.WEST);
        bottom.add(copiesInfoLabel, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // actions
        searchBtn.addActionListener(e -> doSearch());
        booksTable.getSelectionModel().addListSelectionListener(e -> updateCopiesInfoForSelectedBook());
        borrowBtn.addActionListener(e -> handleBorrow());

        booksTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) updateCopiesInfoForSelectedBook();
            }
        });
    }

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }

    /*private void doSearch() {
        String keyword = keywordField.getText().trim();
        String filter = (String) filterCombo.getSelectedItem();
        try {
            ResultSet rs = searchController.search(keyword, filter);
            tableModel.setRowCount(0);
            if (rs != null) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category")
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }*/
    
    private void doSearch() {
    String keyword = keywordField.getText().trim();
    String filter = (String) filterCombo.getSelectedItem();
    try {
        java.util.List<BookDAO.Book> books = searchController.searchList(keyword, filter);
        tableModel.setRowCount(0);
        for (BookDAO.Book b : books) {
            tableModel.addRow(new Object[]{
                b.bookId, b.title, b.author, b.isbn, b.category
            });
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}



    private Integer getSelectedBookId() {
        int row = booksTable.getSelectedRow();
        if (row == -1) return null;
        return (Integer) tableModel.getValueAt(row, 0);
    }

    private void updateCopiesInfoForSelectedBook() {
        Integer bookId = getSelectedBookId();
        if (bookId == null) {
            copiesInfoLabel.setText("Select a book to see copies info.");
            return;
        }
        String info = searchController.getCopiesInfo(bookId);
        copiesInfoLabel.setText(info);
    }

    private void handleBorrow() {
        Integer bookId = getSelectedBookId();
        if (bookId == null) {
            JOptionPane.showMessageDialog(this, "Please select a book first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (currentUserEmail == null) {
            JOptionPane.showMessageDialog(this, "User not set. Please login again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean ok = borrowController.tryBorrow(currentUserEmail, bookId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Borrowed successfully.");
            updateCopiesInfoForSelectedBook();
        }
        // refresh loans tab as well (in case UI open)
        parent.showBooks();// ensures loans tab reloads when user navigates
    }
    
    private void ReturnWindow(){
    
    parent.showHome(currentUserEmail, TOOL_TIP_TEXT_KEY);
    
}
    
}



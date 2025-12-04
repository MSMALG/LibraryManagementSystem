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
        setLayout(new BorderLayout(10,10));

        searchController = new SearchController();
        borrowController = new BorrowController();

        // top: search controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,10));
        
        top.add(new JLabel("Search:"));
        
        keywordField = new JTextField(25);
        keywordField.setPreferredSize(new Dimension(250, 30));
        top.add(keywordField);
        
        filterCombo = new JComboBox<>(new String[]{"All (Title/Author/ISBN)","Title","Author","ISBN","Category"});
        filterCombo.setPreferredSize(new Dimension(180, 30));
        top.add(filterCombo);
        
        searchBtn = new JButton("Search");
        searchBtn.setPreferredSize(new Dimension(100, 30));
        top.add(searchBtn);
        
        add(top, BorderLayout.NORTH);

        // center: table
        tableModel = new DefaultTableModel(new Object[]{"book_id","Title","Author","ISBN","Category"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        booksTable = new JTable(tableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.setRowHeight(25);
        add(new JScrollPane(booksTable), BorderLayout.CENTER);

        // bottom: actions
        JPanel bottom = new JPanel(new BorderLayout(10,10));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,10));
        
        borrowBtn = new JButton("Borrow Selected");
        borrowBtn.setPreferredSize(new Dimension(140, 35));
        
        returnWindowBtn = new JButton("Return Back");
        returnWindowBtn.setPreferredSize(new Dimension(130, 35));

        btns.add(borrowBtn);
        btns.add(returnWindowBtn);
        
        copiesInfoLabel = new JLabel("Select a book to see copies info.");
        copiesInfoLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        
        bottom.add(btns, BorderLayout.WEST);
        bottom.add(copiesInfoLabel, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // actions
        searchBtn.addActionListener(e -> doSearch());
        booksTable.getSelectionModel().addListSelectionListener(e -> updateCopiesInfoForSelectedBook());
        borrowBtn.addActionListener(e -> handleBorrow());
        returnWindowBtn.addActionListener(e -> ReturnWindow());

        booksTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) updateCopiesInfoForSelectedBook();
            }
        });
    }

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }

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
        parent.showBooks();
    }
    
    private void ReturnWindow(){
        parent.showHome(currentUserEmail, TOOL_TIP_TEXT_KEY);
    }
}

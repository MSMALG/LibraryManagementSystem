package gui;

import com.mycompany.librarymanagementsystem.BookDAO;
import com.mycompany.librarymanagementsystem.DBConnection;

import java.sql.*;
import java.util.List;

public class SearchController {

    /**
     * Search for books and return a List of BookDAO.Book DTOs.
     * Uses BookDAO.searchBooksList(...) which closes DB resources properly.
     */
    public List<BookDAO.Book> searchList(String keyword, String filter) throws SQLException {
        if (keyword == null) keyword = "";
        keyword = keyword.trim();

        // If "All" selected, use BookDAO convenience method
        if (filter == null || filter.startsWith("All")) {
            return BookDAO.searchBooksList(keyword);
        }

        // Map filter label to actual column name (safe mapping)
        String column;
        switch (filter.toLowerCase()) {
            case "title": column = "title"; break;
            case "author": column = "author"; break;
            case "isbn": column = "isbn"; break;
            case "category": column = "category"; break;
            default: column = "title"; break; // fallback
        }

        // Run parameterized query for the requested column and return mapped list
        String sql = "SELECT * FROM books WHERE " + column + " LIKE ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                // Map ResultSet rows to List<BookDAO.Book>
                java.util.List<BookDAO.Book> out = new java.util.ArrayList<>();
                while (rs.next()) {
                    out.add(new BookDAO.Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category")
                    ));
                }
                return out;
            }
        }
    }

    /**
     * Return copies info text for a book (safe resource handling).
     */
    public String getCopiesInfo(int bookId) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps1 = conn.prepareStatement("SELECT COUNT(*) AS c FROM copies WHERE book_id=?");
             PreparedStatement ps2 = conn.prepareStatement("SELECT COUNT(*) AS c FROM copies WHERE book_id=? AND status='available'")) {

            ps1.setInt(1, bookId);
            try (ResultSet rs1 = ps1.executeQuery()) {
                int total = rs1.getInt("c");

                ps2.setInt(1, bookId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    int avail = rs2.getInt("c");
                    return "Copies: " + total + " | Available: " + avail;
                }
            }
        } catch (Exception e) {
            return "Error loading copies.";
        }
    }
}

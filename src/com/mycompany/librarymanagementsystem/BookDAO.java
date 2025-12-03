package com.mycompany.librarymanagementsystem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public static class Book {
        public final int bookId;
        public final String title;
        public final String author;
        public final String isbn;
        public final String category;
        public Book(int bookId, String title, String author, String isbn, String category) {
            this.bookId = bookId; this.title = title; this.author = author; this.isbn = isbn; this.category = category;
        }
    }

    public static void addBook(String title, String author, String isbn, String category) {
        Connection conn = DBConnection.connect();
        String sql = "INSERT INTO books(title, author, isbn, category) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, isbn);
            ps.setString(4, category);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // returns a List<Book> and closes resources immediately
    public static List<Book> searchBooksList(String keyword) {
        List<Book> out = new ArrayList<>();
        Connection conn = DBConnection.connect();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String k = "%" + (keyword == null ? "" : keyword) + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("category")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public static ResultSet searchBooksResultSet(String keyword) {
        try {
            Connection conn = DBConnection.connect();
            String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            String k = "%" + (keyword == null ? "" : keyword) + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            return ps.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

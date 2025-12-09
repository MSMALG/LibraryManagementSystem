/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.librarymanagementsystem;

/**
 *
 * @author user
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportsController {

    public static List<String[]> topBorrowedBooks(int limit) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        Connection conn = DBConnection.connect();
        String sql = """
            SELECT b.book_id, b.title, COUNT(l.loan_id) AS borrow_count
            FROM loans l
            JOIN copies c ON l.copy_id = c.copy_id
            JOIN books b ON c.book_id = b.book_id
            GROUP BY b.book_id, b.title
            ORDER BY borrow_count DESC
            LIMIT ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getInt("book_id")),
                        rs.getString("title"),
                        String.valueOf(rs.getInt("borrow_count"))
                    });
                }
            }
        }
        return rows;
    }

    
    public static List<String[]> overdueBooks() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        Connection conn = DBConnection.connect();
         try (Statement stmt = conn.createStatement();
             ResultSet rsTime = stmt.executeQuery("SELECT DATETIME('now', 'UTC') AS db_now")) {
            if (rsTime.next()) {
                System.out.println("DEBUG: Database current UTC time is: " + rsTime.getString("db_now"));
            }
        }
            String sql = """
            SELECT l.loan_id, m.name AS member_name, b.title, l.due_date
            FROM loans l
            JOIN members m ON l.member_id = m.member_id
            JOIN copies c ON l.copy_id = c.copy_id
            JOIN books b ON c.book_id = b.book_id
            WHERE l.return_date IS NULL AND DATETIME(l.due_date) < DATETIME('now', 'localtime')
            ORDER BY l.due_date ASC
        """;
        System.out.println("DEBUG: Executing SQL: " + sql);
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{
                    String.valueOf(rs.getInt("loan_id")),
                    rs.getString("member_name"),
                    rs.getString("title"),
                    rs.getString("due_date")
                });
            }
        }
        
        
        System.out.println("DEBUG: Overdue rows found: " + rows.size());
        return rows;
    }


    public static List<String[]> finesSummary() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        Connection conn = DBConnection.connect();
        String sql = """
            SELECT m.member_id, m.name, IFNULL(SUM(f.amount),0) AS total_fines
            FROM members m
            LEFT JOIN fines f ON f.member_id = m.member_id
            GROUP BY m.member_id, m.name
            ORDER BY total_fines DESC
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{
                    String.valueOf(rs.getInt("member_id")),
                    rs.getString("name"),
                    String.valueOf(rs.getDouble("total_fines"))
                });
            }
        }
        return rows;
    }

    public static Path exportToCsv(List<String[]> rows, String[] headers, String filename) throws IOException {
        Path out = Paths.get(filename);
        try (BufferedWriter writer = Files.newBufferedWriter(out, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            if (headers != null) writer.write(String.join(",", headers) + "\n");
            for (String[] row : rows) {
                for (int i=0;i<row.length;i++) {
                    String v = row[i] == null ? "" : row[i];
                    if (v.contains(",") || v.contains("\"")) v = "\"" + v.replace("\"","\"\"") + "\"";
                    writer.write(v + (i==row.length-1 ? "" : ","));
                }
                writer.newLine();
            }
        }
        return out;
    }
}

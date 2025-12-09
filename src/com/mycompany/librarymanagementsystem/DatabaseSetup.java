package com.mycompany.librarymanagementsystem;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSetup {

    public static void createTables() {
        try {
            Connection conn = DBConnection.connect();
            Statement stmt = conn.createStatement();

            // core tables
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS members (
                    member_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    book_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    author TEXT,
                    isbn TEXT,
                    category TEXT
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS copies (
                    copy_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER,
                    status TEXT,
                    FOREIGN KEY(book_id) REFERENCES books(book_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS loans (
                    loan_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    member_id INTEGER,
                    copy_id INTEGER,
                    loan_date TEXT,
                    due_date TEXT,
                    return_date TEXT,
                    FOREIGN KEY(member_id) REFERENCES members(member_id),
                    FOREIGN KEY(copy_id) REFERENCES copies(copy_id)
                );
            """);

            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS holds (
                    hold_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    member_id INTEGER,
                    book_id INTEGER,
                    position INTEGER,
                    FOREIGN KEY(member_id) REFERENCES members(member_id),
                    FOREIGN KEY(book_id) REFERENCES books(book_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS fines (
                    fine_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    member_id INTEGER,
                    amount REAL,
                    paid INTEGER DEFAULT 0,
                    FOREIGN KEY(member_id) REFERENCES members(member_id)
                );
            """);

            // the ALTER TABLE statements
            try {
                stmt.execute("ALTER TABLE holds ADD COLUMN status TEXT DEFAULT 'WAITING';");
            } catch (Exception ignore) {}

            try {
                stmt.execute("ALTER TABLE holds ADD COLUMN notified_at TEXT;");
            } catch (Exception ignore) {}

            try {
                stmt.execute("ALTER TABLE holds ADD COLUMN expires_at TEXT;");
            } catch (Exception ignore) {}

            // Notifications table
            try {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        member_id INTEGER,
                        message TEXT,
                        created_at TEXT DEFAULT (datetime('now')),
                        read INTEGER DEFAULT 0,
                        FOREIGN KEY(member_id) REFERENCES members(member_id)
                    );
                """);
            } catch (Exception ignore) {}

            System.out.println("Tables created / migrations applied (if needed).");

        } catch (Exception e) {
            System.out.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

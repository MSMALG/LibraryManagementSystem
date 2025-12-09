package com.mycompany.librarymanagementsystem;

import java.sql.*;

public class DBConnection {

    private static Connection conn;

    public static Connection connect() {
        try {
            if (conn == null || conn.isClosed()) {

                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:library.db");

                // Important settings to prevent locking
                Statement st = conn.createStatement();
                st.execute("PRAGMA journal_mode=WAL;");
                st.execute("PRAGMA busy_timeout = 5000;");
                st.execute("PRAGMA synchronous=NORMAL;");
                st.close();

                System.out.println("Connected - Stable connection enabled.");
            }

        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
        }

        return conn;
    }

    // to not close the database from anywhere
    public static void neverClose() {
        
    }
}
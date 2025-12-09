package com.mycompany.librarymanagementsystem;

import java.sql.*;

public class TestPasswordHashing {
    public static void main(String[] args) throws SQLException {
        System.out.println("ALL USER PASSWORDS IN DATABASE:\n");
        
        Connection conn = DBConnection.connect();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT member_id, email, password, password_hash, role FROM members"
        );
        
        while (rs.next()) {
            System.out.println("──────────────────────────────────────");
            System.out.println("ID: " + rs.getInt("member_id"));
            System.out.println("Email: " + rs.getString("email"));
            System.out.println("Role: " + rs.getString("role"));
            System.out.println("Plain Password: " + rs.getString("password"));
            
            String hash = rs.getString("password_hash");
            System.out.println("Hashed Password: " + (hash != null ? hash : "[NULL]"));
            
            if (hash != null && !hash.isEmpty()) {
                System.out.println("Hash Type: " + 
                    (hash.startsWith("$2a$") || hash.startsWith("$2b$") ? "BCrypt" : "Unknown"));
                System.out.println("Hash Length: " + hash.length());
            }
            System.out.println();
        }
        
        rs.close();
        stmt.close();
        conn.close();
    }
}

package com.mycompany.librarymanagementsystem;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt; // Assuming you have jbcrypt imported

public class MemberDAO {

    // Modified to accept forceChange boolean
    public static void addMember(String name, String email, String password, String role, boolean forceChange) {
        Connection conn = DBConnection.connect();
        String hashedPassword = PasswordHasher.hashPasswordBCrypt(password);
        String sql = "INSERT INTO members(name, email, password, password_hash, role, force_password_change) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, ""); // Empty old password field
            ps.setString(4, hashedPassword); // Store BCrypt hash
            ps.setString(5, role);
            ps.setInt(6, forceChange ? 1 : 0); // FIX: Set based on parameter
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Overload for legacy calls (default to true for safety, e.g., admin adds)
    public static void addMember(String name, String email, String password, String role) {
        addMember(name, email, password, role, true); // Default to force change
    }

    public static boolean login(String email, String password) {
        Connection conn = DBConnection.connect();
        String sql = "SELECT password, password_hash, force_password_change FROM members WHERE email=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String oldPassword = rs.getString("password");
                    int forceChange = rs.getInt("force_password_change");
                   
                    // CASE 1: User has BCrypt hash (new system)
                    if (storedHash != null && !storedHash.isEmpty()) {
                        boolean valid = PasswordHasher.checkPasswordBCrypt(password, storedHash);
                        if (valid && forceChange == 1) {
                            // User needs to change password
                            System.out.println("User " + email + " needs password change");
                        }
                        return valid;
                    }
                   
                    // CASE 2: Migration needed - check old plain password
                    if (oldPassword != null && !oldPassword.isEmpty()) {
                        boolean matches = password.equals(oldPassword);
                        if (matches) {
                            // Migrate to BCrypt hash
                            String newHash = PasswordHasher.hashPasswordBCrypt(password);
                            String updateSql = "UPDATE members SET password_hash=?, force_password_change=1 WHERE email=?";
                            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                                updatePs.setString(1, newHash);
                                updatePs.setString(2, email);
                                updatePs.executeUpdate();
                            }
                            // After migration, they need to change password
                            System.out.println("Migrated user " + email + " - password change required");
                        }
                        return matches;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static String getRole(String email) {
        Connection conn = DBConnection.connect();
        String sql = "SELECT role FROM members WHERE email=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("role");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // Reset password with hash
    public static boolean resetPassword(String email, String newPassword) {
        Connection conn = DBConnection.connect();
        String hashedPassword = PasswordHasher.hashPasswordBCrypt(newPassword);
        String sql = "UPDATE members SET password_hash=? WHERE email=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean requiresPasswordChange(String email) {
        Connection conn = DBConnection.connect();
        String sql = "SELECT force_password_change FROM members WHERE email=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("force_password_change") == 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean changePassword(String email, String newPassword) {
        Connection conn = DBConnection.connect();
        String hashedPassword = PasswordHasher.hashPasswordBCrypt(newPassword);
        String sql = "UPDATE members SET password_hash=?, force_password_change=0 WHERE email=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
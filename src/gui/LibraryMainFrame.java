package gui;

import com.mycompany.librarymanagementsystem.DBConnection;
import com.mycompany.librarymanagementsystem.MemberDAO;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LibraryMainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private HomePanel homePanel;
    private PasswordSetupPanel passwordSetupPanel;
    // Dashboard contains the tabs (Search / My Loans)
    private BooksPanel booksPanel;
    private LoansPanel loansPanel;
    // keep current logged email
    private String currentUserEmail;
    private String activeUserRole;
    private JFrame adminFrame = null;
  
    public LibraryMainFrame() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null); // center window
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
       
        // create screens
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        homePanel = new HomePanel(this);
        booksPanel = new BooksPanel(this);
        loansPanel = new LoansPanel(this);
        passwordSetupPanel = new PasswordSetupPanel(this, ""); // Empty email for now
       
        // add screens
        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(registerPanel, "REGISTER");
        cardPanel.add(homePanel, "HOME");
        cardPanel.add(booksPanel, "BOOKS");
        cardPanel.add(loansPanel, "LOANS");
        cardPanel.add(passwordSetupPanel, "PASSWORD_SETUP");
       
        add(cardPanel);
        showLogin(); // start with login screen
        setVisible(true);
       
        // Initialize cleanup and other services
        ReturnController.cleanupAllExpiredHolds();
    }
   
    public String getCurrentUserName() {
        if (currentUserEmail == null) return "User";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM members WHERE email = ?")) {
            ps.setString(1, currentUserEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentUserEmail; // fallback
    }
   
    public String getCurrentUserName(String email) {
        if (email == null) return "User";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM members WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return email; // fallback
    }
  
    public String getCurrentUserRole() {
        return activeUserRole != null ? activeUserRole : "Member";
    }
  
    // open login screen
    public void showLogin() {
        cardLayout.show(cardPanel, "LOGIN");
        // clearing the session data on logout
        this.currentUserEmail = null;
        this.activeUserRole = null;
    }
   
    // open register screen
    public void showRegister() {
        cardLayout.show(cardPanel, "REGISTER");
    }
   
    // open home screen with password change check
    public void showHome(String userName, String role, String userEmail) {
        this.currentUserEmail = userEmail;
        this.activeUserRole = role;
       
        // Update panels with current email
        booksPanel.setCurrentUserEmail(userEmail);
        loansPanel.setCurrentUserEmail(userEmail);
        homePanel.setCurrentUserEmail(userEmail);
        homePanel.setUserInfo(userName, role);
       
        // Check if password change is required
        checkPasswordChangeRequired(userEmail, userName, role);
    }
   
    // Check if user needs to change password
    public void checkPasswordChangeRequired(String email, String userName, String role) {
        if (MemberDAO.requiresPasswordChange(email)) {
            // Show password setup panel
            passwordSetupPanel = new PasswordSetupPanel(this, email);
            cardPanel.remove(passwordSetupPanel); // Remove old instance
            cardPanel.add(passwordSetupPanel, "PASSWORD_SETUP");
            cardLayout.show(cardPanel, "PASSWORD_SETUP");
           
            // Show info message
            JOptionPane.showMessageDialog(this,
                "For security, you must set a new password before continuing.",
                "Password Change Required",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Normal login - go to home
            cardLayout.show(cardPanel, "HOME");
            // Show unread notifications
            homePanel.showUnreadNotificationsOnlyOnce();
        }
    }
   
    // Called after successful password change
    public void showHomeFromPasswordSetup(String email) {
        // Get user info after password change
        String userName = getCurrentUserName(email);
        String role = MemberDAO.getRole(email);
       
        // Update user info
        this.currentUserEmail = email;
        this.activeUserRole = role;
        homePanel.setUserInfo(userName, role);
        homePanel.setCurrentUserEmail(email);
        booksPanel.setCurrentUserEmail(email);
        loansPanel.setCurrentUserEmail(email);
       
        // Go to home screen
        cardLayout.show(cardPanel, "HOME");
       
        // Show success message
        JOptionPane.showMessageDialog(this,
            "Password changed successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
       
        // Show unread notifications
        homePanel.showUnreadNotificationsOnlyOnce();
    }
   
    //handle Admin Panel access
    public void attemptShowAdminPanel(String roleFromHomePanel) {
        if (!"Librarian".equalsIgnoreCase(activeUserRole)) {
            JOptionPane.showMessageDialog(this,
                "You do not have permission to access the admin panel.",
                "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (adminFrame == null || !adminFrame.isDisplayable()) {
            adminFrame = new JFrame("Admin Panel - Librarian Access");
            adminFrame.setSize(1100, 700);
            adminFrame.setLocationRelativeTo(this);
            adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            adminFrame.add(new AdminPanel(this)); // pass 'this' so it can refresh if needed
            adminFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    adminFrame = null; // allow reopening after close
                }
            });
            adminFrame.setVisible(true);
        } else {
            adminFrame.toFront();
            adminFrame.requestFocus();
        }
    }
   
    // open the dashboard (tabs)
    public void showBooks() {
        // ensure panels know the current email (in case user navigated)
        if (currentUserEmail != null) {
            booksPanel.setCurrentUserEmail(currentUserEmail);
            loansPanel.setCurrentUserEmail(currentUserEmail);
            loansPanel.reloadLoans(); // refresh loans when opening
        }
        cardLayout.show(cardPanel, "BOOKS");
    }
   
    public void showLoans() {
        // ensure panels know the current email (in case user navigated)
        if (currentUserEmail != null) {
            booksPanel.setCurrentUserEmail(currentUserEmail);
            loansPanel.setCurrentUserEmail(currentUserEmail);
            loansPanel.reloadLoans(); // refresh loans when opening
        }
        cardLayout.show(cardPanel, "LOANS");
    }
  
    // called by LoginPanel when authenticating
    public String checkCredentialsFromDatabase(String email, String password) {
        boolean canLogin = MemberDAO.login(email, password);
        if (!canLogin) return null;
        String dbRole = MemberDAO.getRole(email);
        return dbRole;
    }
   
    // FIX: Added forceChange=false for self-registration
    public boolean registerNewUserInDatabase(String name, String email, String password, String role) {
        String existingRole = MemberDAO.getRole(email);
        if (existingRole != null) return false; // email already registered
        MemberDAO.addMember(name, email, password, role, false); // No force change for self-register
        return true;
    }
   
    // Legacy overload (defaults to no force for self-register)
    public boolean registerNewUserInDatabase(String name, String email, String password) {
        return registerNewUserInDatabase(name, email, password, "Member");
    }
   
    // legacy overloaded methods
    public void showHome(String userName, String role) {
        // if not provided email, pass the username as identifier
        showHome(userName, role, userName);
    }
   
    public boolean checkCredentialsFromDatabase(String email, String password, String role) {
        boolean canLogin = MemberDAO.login(email, password);
        if (!canLogin) return false;
        String dbRole = MemberDAO.getRole(email);
        return dbRole != null && dbRole.equalsIgnoreCase(role);
    }
}
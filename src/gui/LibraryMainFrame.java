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
    // Dashboard contains the tabs (Search / My Loans)
    private JPanel dashboardPanel;
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
        // create dashboard & panels (BooksPanel and LoansPanel)
        booksPanel = new BooksPanel(this);
        loansPanel = new LoansPanel(this);
        // add screens
        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(registerPanel, "REGISTER");
        cardPanel.add(homePanel, "HOME");
        cardPanel.add(booksPanel, "BOOKS");
        cardPanel.add(loansPanel, "LOANS");
        add(cardPanel);
        showLogin(); // start with login screen
        setVisible(true);
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
   
    public String getCurrentUserRole()
    {
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
    // open home screen
    public void showHome(String userName, String role, String userEmail) {
        homePanel.setUserInfo(userName, role);
        this.currentUserEmail = userEmail;
        this.activeUserRole = role;
        booksPanel.setCurrentUserEmail(userEmail);
        loansPanel.setCurrentUserEmail(userEmail);
        homePanel.setCurrentUserEmail(userEmail);
        cardLayout.show(cardPanel, "HOME");
        // Only show popup ONCE per login if there are unread notifications
        homePanel.showUnreadNotificationsOnlyOnce();
    }
   
    //secure method to handle Admin Panel access
    public void attemptShowAdminPanel(String roleFromHomePanel) {
    if (!"Librarian".equalsIgnoreCase(activeUserRole)) {
        JOptionPane.showMessageDialog(this,
            "You do not have permission to access the admin panel.",
            "Access Denied", JOptionPane.ERROR_MESSAGE);
        return;
    }
    if (adminFrame == null || !adminFrame.isDisplayable())
    {
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
    }
    else
    {
        adminFrame.toFront();
        adminFrame.requestFocus();
    }
}
    // convenience method to open the dashboard (tabs)
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
   
    // called by LoginPanel when authenticating (used earlier)
    public String checkCredentialsFromDatabase(String email, String password) {
        boolean canLogin = MemberDAO.login(email, password);
        if (!canLogin) return null;
        String dbRole = MemberDAO.getRole(email);
        return dbRole;
    }
    public boolean registerNewUserInDatabase(String name, String email, String password, String role) {
        String existingRole = MemberDAO.getRole(email);
        if (existingRole != null) return false; // email already registered
        MemberDAO.addMember(name, email, password, role);
        return true;
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
    public boolean registerNewUserInDatabase(String name, String email, String password) {
        String existingRole = MemberDAO.getRole(email);
        if (existingRole != null) return false;
        MemberDAO.addMember(name, email, password, "Member");
        return true;
    }
}

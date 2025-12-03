package gui;

import com.mycompany.librarymanagementsystem.MemberDAO;

import javax.swing.*;
import java.awt.*;

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
    }

    // open login screen
    public void showLogin() {
        cardLayout.show(cardPanel, "LOGIN");
    }

    // open register screen
    public void showRegister() {
        cardLayout.show(cardPanel, "REGISTER");
    }

    // open home screen
    public void showHome(String userName, String role, String userEmail) {
        homePanel.setUserInfo(userName, role);

        // store current user email (used by BooksPanel & LoansPanel)
        this.currentUserEmail = userEmail;
        booksPanel.setCurrentUserEmail(userEmail);
        loansPanel.setCurrentUserEmail(userEmail);

        cardLayout.show(cardPanel, "HOME");
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

    // legacy overloaded methods (kept for compatibility with earlier code)
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


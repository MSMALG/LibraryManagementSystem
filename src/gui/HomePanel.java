package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomePanel extends JPanel {

    private LibraryMainFrame parentFrame;

    private JLabel welcomeLabel;
    private JButton logoutButton;
    private JButton searchBooksButton;
    private JButton manageLoansButton;
    private JButton adminPanelButton;

    public HomePanel(LibraryMainFrame parentFrame) {
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(10, 10));

        // title
        welcomeLabel = new JLabel("Welcome to the Library System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));

        add(welcomeLabel, BorderLayout.NORTH);

        // main buttons
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        searchBooksButton = new JButton("Search Books");
        manageLoansButton = new JButton("Borrow / Return Books");
        adminPanelButton = new JButton("Admin Panel");

        centerPanel.add(searchBooksButton);
        centerPanel.add(manageLoansButton);
        centerPanel.add(adminPanelButton);

        add(centerPanel, BorderLayout.CENTER);

        // logout button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutButton = new JButton("Logout");
        bottomPanel.add(logoutButton);

        add(bottomPanel, BorderLayout.SOUTH);

        addActions();
    }

    // set user text
    public void setUserInfo(String userName, String role) {
        String text = "Welcome, " + userName + " (" + role + ")";
        welcomeLabel.setText(text);

        // disable admin button for member
        if ("Member".equalsIgnoreCase(role)) {
            adminPanelButton.setEnabled(false);
        } else {
            adminPanelButton.setEnabled(true);
        }
    }

    private void addActions() {

        // logout click
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.showLogin();
            }
        });

        // Search Books – belongs to second team member
        searchBooksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // instead of showing "Coming Soon"
            parentFrame.showBooks();

            }
        });

        // Borrow / Return – belongs to second team member
        manageLoansButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*JOptionPane.showMessageDialog(HomePanel.this,
                        "This part belongs to the second team member.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);*/
             parentFrame.showLoans();

            }
        });
       
       /*manageLoansButton.addActionListener(new ActionListener() {
         @Override
            public void actionPerformed(ActionEvent e) {
        parentFrame.showDashboard();
            }
        });*/


      adminPanelButton.addActionListener(e -> {
        JFrame adminFrame = new JFrame("Admin Panel");
        adminFrame.setSize(1000,700);
        adminFrame.setLocationRelativeTo(null);
        adminFrame.add(new AdminPanel(null)); // you can pass parent if needed
        adminFrame.setVisible(true);
    });

    }
}

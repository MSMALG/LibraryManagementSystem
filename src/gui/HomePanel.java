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

        setLayout(new BorderLayout(15, 15));

        // title
        welcomeLabel = new JLabel("Welcome to the Library System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        add(welcomeLabel, BorderLayout.NORTH);

        // main buttons - centered with proper sizing
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        searchBooksButton = new JButton("Search Books");
        searchBooksButton.setPreferredSize(new Dimension(250, 45));
        searchBooksButton.setFont(new Font("Arial", Font.PLAIN, 15));
        gbc.gridy = 0;
        centerWrapper.add(searchBooksButton, gbc);

        manageLoansButton = new JButton("Borrow / Return Books");
        manageLoansButton.setPreferredSize(new Dimension(250, 45));
        manageLoansButton.setFont(new Font("Arial", Font.PLAIN, 15));
        gbc.gridy = 1;
        centerWrapper.add(manageLoansButton, gbc);

        adminPanelButton = new JButton("Admin Panel");
        adminPanelButton.setPreferredSize(new Dimension(250, 45));
        adminPanelButton.setFont(new Font("Arial", Font.PLAIN, 15));
        gbc.gridy = 2;
        centerWrapper.add(adminPanelButton, gbc);

        add(centerWrapper, BorderLayout.CENTER);

        // logout button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(100, 35));
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

        // Search Books
        searchBooksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.showBooks();
            }
        });

        // Borrow / Return
        manageLoansButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.showLoans();
            }
        });

        adminPanelButton.addActionListener(e -> {
            JFrame adminFrame = new JFrame("Admin Panel");
            adminFrame.setSize(900,600);
            adminFrame.setLocationRelativeTo(null);
            adminFrame.add(new AdminPanel(null));
            adminFrame.setVisible(true);
        });
    }
}

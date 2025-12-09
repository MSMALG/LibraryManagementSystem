package gui;

import com.mycompany.librarymanagementsystem.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.List;

public class HomePanel extends JPanel {
    private LibraryMainFrame parentFrame;
    private JLabel welcomeLabel;
    private JButton logoutButton;
    private JButton searchBooksButton;
    private JButton manageLoansButton;
    private JButton adminPanelButton;
    private String currentUserRole;
    private String currentUserEmail;

    // Notification components
    private JPanel bottomPanel;
    private JButton notificationBtn; 
    private JLabel badgeLabel;

    public HomePanel(LibraryMainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(15, 15));

        // Title
        welcomeLabel = new JLabel("Welcome to the Library System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(welcomeLabel, BorderLayout.NORTH);

        // Center buttons
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

        // BOTTOM PANEL: NOTIFICATIONS BUTTON + LOGOUT 
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Notification Button (text + badge)
        notificationBtn = new JButton("Notifications");  // ← Add spaces!
        notificationBtn.setFont(new Font("Arial", Font.BOLD, 13));
        notificationBtn.setForeground(new Color(0, 100, 200));
        notificationBtn.setFocusPainted(false);
        notificationBtn.setHorizontalTextPosition(SwingConstants.LEFT);  // ← Important!
        notificationBtn.setMargin(new Insets(5, 15, 5, 15));  // ← More space for text

        // Badge (small red circle with number)
        badgeLabel = new JLabel("", SwingConstants.CENTER);
        badgeLabel.setFont(new Font("Arial", Font.BOLD, 10));
        badgeLabel.setForeground(Color.WHITE);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(Color.RED);
        badgeLabel.setPreferredSize(new Dimension(100, 35));
        badgeLabel.setVisible(false);

        // Overlay badge on button (top-right)
        notificationBtn.setLayout(new OverlayLayout(notificationBtn));
        notificationBtn.add(badgeLabel);

        // Click → show notifications
        notificationBtn.addActionListener(e -> showNotificationsPopup());

        // Logout
        logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(100, 35));

        bottomPanel.add(notificationBtn);
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);

        addActions();
    }

    public void setUserInfo(String userName, String role) {
        this.currentUserRole = role;
        welcomeLabel.setText("Welcome, " + userName + " (" + role + ")");
        adminPanelButton.setEnabled("Librarian".equalsIgnoreCase(role));
    }

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }

    private void addActions() {
        logoutButton.addActionListener(e -> parentFrame.showLogin());
        searchBooksButton.addActionListener(e -> parentFrame.showBooks());
        manageLoansButton.addActionListener(e -> parentFrame.showLoans());
        adminPanelButton.addActionListener(e -> parentFrame.attemptShowAdminPanel(currentUserRole));
    }

    // This is called only on login — shows popup ONLY if unread exist
public void showUnreadNotificationsOnlyOnce() {
    if (currentUserEmail == null) return;

    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement("SELECT member_id FROM members WHERE email = ?")) {
        ps.setString(1, currentUserEmail);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return;
            int memberId = rs.getInt("member_id");

            List<String> notifs = NotificationDAO.getUnreadNotifications(memberId);
            if (notifs.isEmpty()) return;  // ← No popup if none!

            // Show only once per login
            StringBuilder msg = new StringBuilder("<html><b>You have " + notifs.size() + " new notification(s):</b><br><hr><br>");
            for (String n : notifs) {
                msg.append("• ").append(n).append("<br><br>");
            }
            msg.append("</html>");

            int choice = JOptionPane.showOptionDialog(
                this, msg.toString(), "New Notifications",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, new String[]{"Mark as Read", "Close"}, "Mark as Read"
            );

            if (choice == 0) {
                NotificationDAO.markAllRead(memberId);
                JOptionPane.showMessageDialog(this, "All notifications marked as read!");
            }
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

    // This is called when user clicks the button ;always shows something
    public void showNotificationsPopup() {
        if (currentUserEmail == null) {
            JOptionPane.showMessageDialog(this, "Please log in first.");
            return;
        }

    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement("SELECT member_id FROM members WHERE email = ?")) {
        ps.setString(1, currentUserEmail);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return;
            int memberId = rs.getInt("member_id");

            List<String> notifs = NotificationDAO.getUnreadNotifications(memberId);

            if (notifs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No new notifications.\nYou're all caught up!", 
                    "Notifications", JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder msg = new StringBuilder("<html><b>You have " + notifs.size() + " unread notification(s):</b><br><hr><br>");
                for (String n : notifs) msg.append("• ").append(n).append("<br><br>");
                msg.append("</html>");

                int choice = JOptionPane.showOptionDialog(
                    this, msg.toString(), "Notifications",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, new String[]{"Mark as Read", "Close"}, "Mark as Read"
                );

                if (choice == 0) {
                    NotificationDAO.markAllRead(memberId);
                    JOptionPane.showMessageDialog(this, "Marked as read!");
                }
            }
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
}
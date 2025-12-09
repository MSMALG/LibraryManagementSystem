package gui;

import com.mycompany.librarymanagementsystem.MemberDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PasswordSetupPanel extends JPanel {
    private LibraryMainFrame parentFrame;
    private String userEmail;
    
    private JLabel titleLabel;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton submitButton;
    private JButton cancelButton;
    private JLabel requirementsLabel;
    
    public PasswordSetupPanel(LibraryMainFrame parentFrame, String userEmail) {
        this.parentFrame = parentFrame;
        this.userEmail = userEmail;
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        titleLabel = new JLabel("Set Your Password", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);
        
        // Info
        JLabel infoLabel = new JLabel("<html><center>For security, you must set a new password.<br>"
                + "Your temporary password will no longer work.</center></html>", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 1;
        add(infoLabel, gbc);
        
        // New Password
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("New Password:"), gbc);
        
        newPasswordField = new JPasswordField(20);
        newPasswordField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(newPasswordField, gbc);
        
        // Confirm Password
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Confirm Password:"), gbc);
        
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(confirmPasswordField, gbc);
        
        // Password Requirements
        requirementsLabel = new JLabel("<html>Password must:<br>"
                + "• Be at least 8 characters<br>"
                + "• Contain uppercase & lowercase<br>"
                + "• Contain a digit<br>"
                + "• Contain a special character</html>");
        requirementsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        requirementsLabel.setForeground(Color.GRAY);
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(requirementsLabel, gbc);
        
        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        submitButton = new JButton("Set Password");
        cancelButton = new JButton("Cancel");
        
        submitButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridy = 5;
        add(buttonPanel, gbc);
        
        addActions();
    }
    
    private void addActions() {
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePasswordChange();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Log them out if they cancel
                JOptionPane.showMessageDialog(PasswordSetupPanel.this,
                    "You must set a password to continue.",
                    "Password Required",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
    }
    
    private void handlePasswordChange() {
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validation
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in both password fields.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!isValidPassword(newPassword)) {
            JOptionPane.showMessageDialog(this,
                "Password does not meet requirements.\n"
                + "Must be at least 8 characters with uppercase, lowercase, digit, and special character.",
                "Weak Password",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Update password in database
        boolean success = MemberDAO.changePassword(userEmail, newPassword);
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Password changed successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Redirect to home page
            parentFrame.showHomeFromPasswordSetup(userEmail);
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to change password. Please try again.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
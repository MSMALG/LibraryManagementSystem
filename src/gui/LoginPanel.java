package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.mycompany.librarymanagementsystem.DBConnection;

public class LoginPanel extends JPanel {

    private LibraryMainFrame parentFrame;

    private JTextField emailField;
    private JPasswordField passwordField;

    private JButton loginButton;
    private JButton goToRegisterButton;

    public LoginPanel(LibraryMainFrame parentFrame) {
        this.parentFrame = parentFrame;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // title
        JLabel titleLabel = new JLabel("Library Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        // Email label
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Email:"), gbc);

        // Email field
        emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(emailField, gbc);

        // Password label
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Password:"), gbc);

        // Password field
        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);

        // buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(120, 35));
        loginButton.setFont(new Font("Arial", Font.PLAIN, 14));
        
        goToRegisterButton = new JButton("Create Account");
        goToRegisterButton.setPreferredSize(new Dimension(140, 35));
        goToRegisterButton.setFont(new Font("Arial", Font.PLAIN, 14));

        buttonPanel.add(loginButton);
        buttonPanel.add(goToRegisterButton);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        addActions();
    }

    private void addActions() {
        // login click
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // go to register
        goToRegisterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.showRegister();
            }
        });
    }

    // handle login logic
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        // check empty
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill all fields.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // check email
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.",
                    "Invalid Email",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // check password
        if (!isValidPassword(password)) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 8 characters and contain at least one digit.",
                    "Weak Password",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        
        String standardizedEmail = email.toLowerCase(); 
        String role = parentFrame.checkCredentialsFromDatabase(standardizedEmail, password);
        if (role == null) {
            JOptionPane.showMessageDialog(this,
                    "Invalid email or password.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Fetch the real name from DB
        String realName = standardizedEmail;  
            try (Connection conn = DBConnection.connect();
                 PreparedStatement ps = conn.prepareStatement("SELECT name FROM members WHERE email = ?")) {
                ps.setString(1, standardizedEmail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        realName = rs.getString("name");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // pass the real name from the start
            parentFrame.showHome(realName, role, standardizedEmail);
           }

    // email check
    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        try {
            return email.matches(regex);
        } catch (Exception e) {
            return false;
        }
    }

    // password check
    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
                break;
            }
        }

        return hasDigit;
    }
}

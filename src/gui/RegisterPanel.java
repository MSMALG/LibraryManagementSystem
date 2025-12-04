package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterPanel extends JPanel {

    private LibraryMainFrame parentFrame;

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleComboBox;

    private JButton registerButton;
    private JButton backToLoginButton;

    public RegisterPanel(LibraryMainFrame parentFrame) {
        this.parentFrame = parentFrame;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // title
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // Name
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Full Name:"), gbc);

        nameField = new JTextField(20);
        nameField.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(nameField, gbc);

        // Email
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Email:"), gbc);

        emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(emailField, gbc);

        // Password
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);

        // Confirm Password
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Confirm Password:"), gbc);

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(confirmPasswordField, gbc);

        // Role
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Role:"), gbc);

        roleComboBox = new JComboBox<>(new String[]{"Member", "Librarian"});
        roleComboBox.setPreferredSize(new Dimension(250, 30));
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(roleComboBox, gbc);

        // buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(120, 35));
        registerButton.setFont(new Font("Arial", Font.PLAIN, 14));
        
        backToLoginButton = new JButton("Back to Login");
        backToLoginButton.setPreferredSize(new Dimension(140, 35));
        backToLoginButton.setFont(new Font("Arial", Font.PLAIN, 14));

        buttonPanel.add(registerButton);
        buttonPanel.add(backToLoginButton);

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        addActions();
    }

    private void addActions() {
        // register click
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        // back click
        backToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.showLogin();
            }
        });
    }

    // handle register logic
    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        // empty check
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill all fields.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // email check
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.",
                    "Invalid Email",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // password length
        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 8 characters.",
                    "Weak Password",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // same password
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match.",
                    "Mismatch",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isRegistered = parentFrame.registerNewUserInDatabase(name, email, password, role);

        if (!isRegistered) {
            JOptionPane.showMessageDialog(this,
                    "Registration failed. Maybe email is already registered.",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Account created successfully. You can now login.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            parentFrame.showLogin();
        }
    }

    // clear inputs
    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        roleComboBox.setSelectedIndex(0);
    }
}

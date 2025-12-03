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

        setLayout(new BorderLayout(10, 10));

        // title
        JLabel titleLabel = new JLabel("Create New Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        // form
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));

        JLabel nameLabel = new JLabel("Full Name:");
        nameField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField();

        JLabel roleLabel = new JLabel("Role:");
        roleComboBox = new JComboBox<>(new String[]{"Member", "Librarian"});

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(confirmPasswordLabel);
        formPanel.add(confirmPasswordField);
        formPanel.add(roleLabel);
        formPanel.add(roleComboBox);


        add(formPanel, BorderLayout.CENTER);

        // buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        registerButton = new JButton("Register");
        backToLoginButton = new JButton("Back to Login");

        buttonPanel.add(registerButton);
        buttonPanel.add(backToLoginButton);

        add(buttonPanel, BorderLayout.SOUTH);

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

        // fake register call
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

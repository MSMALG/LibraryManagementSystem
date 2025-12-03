package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {

    private LibraryMainFrame parentFrame;

    private JTextField emailField;
    private JPasswordField passwordField;

    private JButton loginButton;
    private JButton goToRegisterButton;

    public LoginPanel(LibraryMainFrame parentFrame) {
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(10, 10));

        // title
        JLabel titleLabel = new JLabel("Library Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        // form
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 8, 8));

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

      
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        add(formPanel, BorderLayout.CENTER);

        // buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        loginButton = new JButton("Login");
        goToRegisterButton = new JButton("Create New Account");

        buttonPanel.add(loginButton);
        buttonPanel.add(goToRegisterButton);

        add(buttonPanel, BorderLayout.SOUTH);

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

        String role = parentFrame.checkCredentialsFromDatabase(email, password);

    if (role == null) {
        JOptionPane.showMessageDialog(this,
                "Invalid email or password.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE);
    } else {
        parentFrame.showHome(email, role);
    }
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

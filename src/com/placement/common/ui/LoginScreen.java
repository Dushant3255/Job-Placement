package com.placement.common.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.placement.common.model.UserRole;
import com.placement.common.service.AuthService;

public class LoginScreen extends JFrame {

    private char defaultEcho;
    private JLabel errorLabel;

    // ✅ backend
    private final AuthService authService = new AuthService();

    public LoginScreen() {
        setTitle("Student Placement Portal");
        setSize(420, 620);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        /* ---------- HEADER ---------- */
        JPanel header = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(
                        0, 0, new Color(99, 102, 241),
                        getWidth(), getHeight(), new Color(124, 58, 237)
                ));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(420, 140));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Student Placement Portal");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel subtitle = new JLabel("Job Recruitment System");
        subtitle.setForeground(new Color(230, 230, 255));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);

        /* ---------- FORM ---------- */
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(10, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        /* ---------- USERNAME ---------- */
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(new JLabel("Username / Email"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JTextField userField = new JTextField();
        styleField(userField);
        form.add(userField, gbc);

        /* ---------- PASSWORD ---------- */
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Password"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JPasswordField passField = new JPasswordField();
        styleField(passField);
        defaultEcho = passField.getEchoChar();
        form.add(passField, gbc);

        /* ---------- SHOW PASSWORD ---------- */
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.EAST;

        JCheckBox showPass = new JCheckBox("Show password");
        showPass.setBackground(Color.WHITE);
        showPass.addActionListener(e ->
                passField.setEchoChar(showPass.isSelected() ? (char) 0 : defaultEcho)
        );
        form.add(showPass, gbc);

        /* ---------- ERROR MESSAGE ---------- */
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(errorLabel, gbc);

        /* ---------- BUTTONS ---------- */
        gbc.gridy = 4;

        InteractiveButton signIn = new InteractiveButton("Sign In", new Color(99, 102, 241));
        InteractiveButton createAccount = new InteractiveButton("Create Account", new Color(237, 233, 254));
        createAccount.setForeground(new Color(79, 70, 229));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttons.setBackground(Color.WHITE);
        buttons.add(signIn);
        buttons.add(createAccount);

        form.add(buttons, gbc);

        /* ---------- FORGOT PASSWORD ---------- */
        gbc.gridy = 5;

        JButton forgot = new JButton("<HTML><U>Forgot Password?</U></HTML>");
        forgot.setBorderPainted(false);
        forgot.setContentAreaFilled(false);
        forgot.setForeground(Color.GRAY);
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        forgot.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { forgot.setForeground(new Color(79, 70, 229)); }
            public void mouseExited(MouseEvent e) { forgot.setForeground(Color.GRAY); }
        });

        form.add(forgot, gbc);

        /* ---------- ACTIONS ---------- */

        signIn.addActionListener(e -> {
            String login = userField.getText().trim();
            String pw = String.valueOf(passField.getPassword());

            if (login.isEmpty() || pw.isEmpty()) {
                errorLabel.setText("Username/email and password are required.");
                return;
            }

            errorLabel.setText(" ");
            signIn.setEnabled(false);

            new SwingWorker<AuthService.AuthResult, Void>() {
                @Override
                protected AuthService.AuthResult doInBackground() throws Exception {
                    return authService.login(login, pw);
                }

                @Override
                protected void done() {
                    try {
                        AuthService.AuthResult result = get();
                        signIn.setEnabled(true);

                        if (result.status == AuthService.AuthStatus.FAIL) {
                            errorLabel.setText(result.message);
                            return;
                        }

                        if (result.status == AuthService.AuthStatus.NEEDS_VERIFICATION) {
                            // ✅ Send them to OTP verification
                            String email = result.user.getEmail();
                            UserRole role = result.user.getRole();

                            JOptionPane.showMessageDialog(
                                    LoginScreen.this,
                                    "Your account is not verified yet. Please verify OTP.",
                                    "Verification Required",
                                    JOptionPane.INFORMATION_MESSAGE
                            );

                            if (role == UserRole.COMPANY) {
                                new VerifyOtpScreen(email, true).setVisible(true);
                            } else {
                                new VerifyOtpScreen(email, (String) null).setVisible(true);
                            }
                            dispose();
                            return;
                        }

                        // ✅ SUCCESS
                        errorLabel.setText(" ");
                        JOptionPane.showMessageDialog(
                                LoginScreen.this,
                                "Login successful! Welcome " + result.user.getUsername(),
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        if (result.user.getRole() == UserRole.COMPANY) {
                            new com.placement.company.ui.CompanyDashboardScreen(result.user.getUsername()).setVisible(true);
                        } else {
                            // new StudentDashboardScreen(...).setVisible(true);
                        }
                        dispose();


                    } catch (Exception ex) {
                        signIn.setEnabled(true);

                        Throwable root = ex;
                        if (ex instanceof java.util.concurrent.ExecutionException && ex.getCause() != null) {
                            root = ex.getCause();
                        }

                        String msg = (root.getMessage() == null) ? "Login failed." : root.getMessage();
                        errorLabel.setText(msg);
                    }
                }
            }.execute();
        });

        createAccount.addActionListener(e -> {
            new CreateAccountScreen().setVisible(true);
            dispose();
        });

        forgot.addActionListener(e -> {
            new ForgotPasswordScreen().setVisible(true);
            dispose();
        });

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        add(root);
    }

    private void styleField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 230)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    static class InteractiveButton extends JButton {
        private final Color normal;
        private final Color pressed;

        public InteractiveButton(String text, Color normal) {
            super(text);
            this.normal = normal;
            this.pressed = normal.darker();
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 26));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? pressed : normal);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}

package com.placement.common.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ResetPasswordScreen extends JFrame {

    private final String email;
    private JPasswordField pass1;
    private JPasswordField pass2;
    private JLabel errorLabel;
    private char echo1, echo2;

    private static final Color GRAD_START = new Color(99, 102, 241);
    private static final Color GRAD_END   = new Color(124, 58, 237);

    public ResetPasswordScreen(String email) {
        this.email = email;

        setTitle("Student Placement Portal");
        setSize(420, 580);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel header = buildHeader("Reset Password", e -> {
            new LoginScreen().setVisible(true);
            dispose();
        });
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(18, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = new JTextField(email);
        emailField.setEditable(false);
        styleField(emailField);

        pass1 = new JPasswordField();
        pass2 = new JPasswordField();
        styleField(pass1);
        styleField(pass2);

        echo1 = pass1.getEchoChar();
        echo2 = pass2.getEchoChar();

        int row = 0;
        addRow(form, gbc, row++, "Email", emailField);
        addRow(form, gbc, row++, "New Password", pass1);
        addRow(form, gbc, row++, "Confirm Password", pass2);

        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.EAST;

        JCheckBox show = new JCheckBox("Show password");
        show.setBackground(Color.WHITE);
        show.addActionListener(e -> {
            boolean on = show.isSelected();
            pass1.setEchoChar(on ? (char) 0 : echo1);
            pass2.setEchoChar(on ? (char) 0 : echo2);
        });
        form.add(show, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(errorLabel, gbc);

        gbc.gridy = row++;
        ForgotPasswordScreen.InteractiveButton resetBtn =
                new ForgotPasswordScreen.InteractiveButton("Reset Password", new Color(99, 102, 241));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttons.setBackground(Color.WHITE);
        buttons.add(resetBtn);
        form.add(buttons, gbc);

        resetBtn.addActionListener(e -> {
            String p1 = String.valueOf(pass1.getPassword());
            String p2 = String.valueOf(pass2.getPassword());

            if (p1.isEmpty() || p2.isEmpty()) { errorLabel.setText("All fields are required."); return; }
            if (!p1.equals(p2)) { errorLabel.setText("Passwords do not match."); return; }
            if (p1.length() < 6) { errorLabel.setText("Password must be at least 6 characters."); return; }

            errorLabel.setText(" ");
            JOptionPane.showMessageDialog(this, "Password reset successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            new LoginScreen().setVisible(true);
            dispose();
        });

        root.add(form, BorderLayout.CENTER);
        add(root);
    }

    private JPanel buildHeader(String subtitleText, java.awt.event.ActionListener backAction) {
        JPanel header = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, GRAD_START, getWidth(), getHeight(), GRAD_END));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        header.setPreferredSize(new Dimension(420, 140));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(18, 26, 15, 20));

        JButton backBtn = new HeaderBackButton("← Back");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(backAction);

        JLabel title = new JLabel("Student Placement Portal");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setForeground(new Color(230, 230, 255));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(backBtn);
        header.add(Box.createVerticalStrut(10));
        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        return header;
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        form.add(new JLabel(labelText), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        field.setPreferredSize(new Dimension(220, 38));
        form.add(field, gbc);
    }

    private void styleField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 230)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }
}

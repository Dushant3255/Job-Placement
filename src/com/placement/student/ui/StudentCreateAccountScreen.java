package com.placement.student.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.placement.common.ui.HeaderBackButton;
import com.placement.common.ui.CreateAccountScreen;
import com.placement.common.ui.VerifyOtpScreen;

public class StudentCreateAccountScreen extends JFrame {

    private JLabel errorLabel;
    private char defaultEcho1;
    private char defaultEcho2;

    public StudentCreateAccountScreen() {
        setTitle("Student Placement Portal");
        setSize(420, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        /* ---------- HEADER ---------- */
        JPanel header = buildHeader("Create Student Account", e -> {
            new CreateAccountScreen().setVisible(true);
            dispose();
        });

        /* ---------- FORM ---------- */
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(10, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField firstName = new JTextField();
        JTextField lastName = new JTextField();
        JTextField username = new JTextField();
        JTextField email = new JTextField();
        JPasswordField password = new JPasswordField();
        JPasswordField confirmPassword = new JPasswordField();

        styleField(firstName);
        styleField(lastName);
        styleField(username);
        styleField(email);
        styleField(password);
        styleField(confirmPassword);

        defaultEcho1 = password.getEchoChar();
        defaultEcho2 = confirmPassword.getEchoChar();

        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderBox.setBackground(Color.WHITE);
        genderBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        int row = 0;
        addRow(form, gbc, row++, "First Name", firstName);
        addRow(form, gbc, row++, "Last Name", lastName);
        addRow(form, gbc, row++, "Username", username);
        addRow(form, gbc, row++, "Email", email);
        addRow(form, gbc, row++, "Password", password);
        addRow(form, gbc, row++, "Confirm Password", confirmPassword);
        addRow(form, gbc, row++, "Gender", genderBox);

        // Show password
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.EAST;

        JCheckBox showPass = new JCheckBox("Show password");
        showPass.setBackground(Color.WHITE);
        showPass.addActionListener(e -> {
            boolean show = showPass.isSelected();
            password.setEchoChar(show ? (char) 0 : defaultEcho1);
            confirmPassword.setEchoChar(show ? (char) 0 : defaultEcho2);
        });
        form.add(showPass, gbc);

        // Error label
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(errorLabel, gbc);

        // Continue button
        gbc.gridy = row++;
        InteractiveButton continueBtn = new InteractiveButton("Continue", new Color(99, 102, 241));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttons.setBackground(Color.WHITE);
        buttons.add(continueBtn);
        form.add(buttons, gbc);

        continueBtn.addActionListener(e -> {
            if (firstName.getText().trim().isEmpty()
                    || lastName.getText().trim().isEmpty()
                    || username.getText().trim().isEmpty()
                    || email.getText().trim().isEmpty()
                    || password.getPassword().length == 0
                    || confirmPassword.getPassword().length == 0) {
                errorLabel.setText("All fields are required.");
                return;
            }

            String p1 = String.valueOf(password.getPassword());
            String p2 = String.valueOf(confirmPassword.getPassword());
            if (!p1.equals(p2)) {
                errorLabel.setText("Passwords do not match.");
                return;
            }

            errorLabel.setText(" ");

            new VerifyOtpScreen(
                    email.getText().trim(),
                    (String) genderBox.getSelectedItem()
            ).setVisible(true);
            dispose();
        });

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        add(root);
    }

    /* ---------- Header / styling helpers ---------- */

    private JPanel buildHeader(String subtitleText, java.awt.event.ActionListener backAction) {
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
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
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

    /* ---------- Buttons ---------- */
    static class InteractiveButton extends JButton {
        private final Color normal;
        private final Color hover;
        private final Color pressed;

        public InteractiveButton(String text, Color normal) {
            super(text);
            this.normal = normal;
            this.hover = normal.darker();
            this.pressed = normal.darker().darker();

            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { repaint(); }
                @Override public void mouseExited(MouseEvent e) { repaint(); }
                @Override public void mousePressed(MouseEvent e) { repaint(); }
                @Override public void mouseReleased(MouseEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) g2.setColor(pressed);
            else if (getModel().isRollover()) g2.setColor(hover);
            else g2.setColor(normal);

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}

package com.placement.company.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.placement.common.ui.CreateAccountScreen;
import com.placement.common.ui.HeaderBackButton;
import com.placement.common.ui.VerifyOtpScreen;

// ✅ backend imports (added)
import com.placement.common.service.RegistrationService;
import com.placement.company.model.CompanyProfile;

public class CompanyCreateAccountScreen extends JFrame {

    private JLabel errorLabel;
    private char defaultEcho1;
    private char defaultEcho2;

    // ✅ Company theme (reddish)
    private static final Color GRAD_START = new Color(220, 38, 38);
    private static final Color GRAD_END   = new Color(244, 63, 94);

    private static final Color PRIMARY_BTN = new Color(220, 38, 38);
    private static final Color SECONDARY_BG = new Color(254, 226, 226);
    private static final Color SECONDARY_FG = new Color(185, 28, 28);

    // ✅ backend service (added)
    private final RegistrationService registrationService = new RegistrationService();

    public CompanyCreateAccountScreen() {
        setTitle("Student Placement Portal");
        setSize(420, 760);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        /* ---------- HEADER ---------- */
        JPanel header = buildHeader("Create Company Account", e -> {
            new CreateAccountScreen().setVisible(true);
            dispose();
        });

        /* ---------- FORM ---------- */
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(28, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField companyName = new JTextField();
        JTextField email = new JTextField();
        JTextField username = new JTextField();
        JTextField phone = new JTextField();
        JTextField website = new JTextField();

        styleField(companyName);
        styleField(email);
        styleField(username);
        styleField(phone);
        styleField(website);

        JComboBox<String> industryBox = new JComboBox<>(new String[]{
                "IT / Software", "Finance", "Manufacturing", "Education", "Healthcare", "Other"
        });
        industryBox.setBackground(Color.WHITE);
        industryBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JComboBox<String> sizeBox = new JComboBox<>(new String[]{
                "1-10", "11-50", "51-200", "201-500", "500+"
        });
        sizeBox.setBackground(Color.WHITE);
        sizeBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTextArea addressArea = new JTextArea(3, 18);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 230)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(BorderFactory.createEmptyBorder());
        addressScroll.setPreferredSize(new Dimension(220, 85));

        JPasswordField password = new JPasswordField();
        JPasswordField confirmPassword = new JPasswordField();
        styleField(password);
        styleField(confirmPassword);
        defaultEcho1 = password.getEchoChar();
        defaultEcho2 = confirmPassword.getEchoChar();

        int row = 0;
        addRow(form, gbc, row++, "Company Name", companyName);
        addRow(form, gbc, row++, "Email", email);
        addRow(form, gbc, row++, "Username", username);
        addRow(form, gbc, row++, "Phone", phone);
        addRow(form, gbc, row++, "Website", website);
        addRow(form, gbc, row++, "Industry", industryBox);
        addRow(form, gbc, row++, "Company Size", sizeBox);

        // Address (bigger)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        form.add(new JLabel("Address"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(addressScroll, gbc);
        row++;

        addRow(form, gbc, row++, "Password", password);
        addRow(form, gbc, row++, "Confirm Password", confirmPassword);

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

        // Buttons
        gbc.gridy = row++;

        InteractiveButton continueBtn = new InteractiveButton("Continue", PRIMARY_BTN);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttons.setBackground(Color.WHITE);
        buttons.add(continueBtn);
        form.add(buttons, gbc);

        // ✅ ONLY logic changed: validations + backend call
        continueBtn.addActionListener(e -> {

            String cName = companyName.getText().trim();
            String em = email.getText().trim();
            String un = username.getText().trim();
            String ph = phone.getText().trim();
            String web = website.getText().trim();
            String industry = (String) industryBox.getSelectedItem();
            String size = (String) sizeBox.getSelectedItem();
            String addr = addressArea.getText().trim();

            String p1 = String.valueOf(password.getPassword());
            String p2 = String.valueOf(confirmPassword.getPassword());

            // Required fields (keeping your original set)
            if (cName.isEmpty()
                    || em.isEmpty()
                    || un.isEmpty()
                    || ph.isEmpty()
                    || password.getPassword().length == 0
                    || confirmPassword.getPassword().length == 0) {
                errorLabel.setText("Please fill all required fields.");
                return;
            }

            // Email check (keeping your original)
            if (!em.contains("@") || !em.contains(".")) {
                errorLabel.setText("Please enter a valid email.");
                return;
            }

            // ✅ Password min 8 chars (added)
            if (p1.length() < 8) {
                errorLabel.setText("Password must be at least 8 characters.");
                return;
            }

            // Password match (keeping your original)
            if (!p1.equals(p2)) {
                errorLabel.setText("Passwords do not match.");
                return;
            }

            // ✅ Phone: 8 digits starting with 5 (added)
            if (!ph.matches("^5\\d{7}$")) {
                errorLabel.setText("Phone must be 8 digits and start with 5.");
                return;
            }

            errorLabel.setText(" ");
            continueBtn.setEnabled(false);

            CompanyProfile profile = new CompanyProfile(
                    cName,
                    ph,
                    web,
                    industry,
                    size,
                    addr
            );

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    registrationService.registerCompanyPendingOtp(un, em, p1, profile);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();

                        continueBtn.setEnabled(true);
                        errorLabel.setText(" ");

                        new VerifyOtpScreen(em, true).setVisible(true);
                        dispose();

                    } catch (Exception ex) {
                        continueBtn.setEnabled(true);

                        // ✅ unwrap SwingWorker/ExecutionException so we don't show "java.lang...."
                        Throwable root = ex;
                        if (ex instanceof java.util.concurrent.ExecutionException && ex.getCause() != null) {
                            root = ex.getCause();
                        }

                        String msg = (root.getMessage() == null) ? "" : root.getMessage();

                        // ✅ map common DB constraint errors (SQLite) to friendly text
                        String lower = msg.toLowerCase();

                        if (lower.contains("username already exists")) {
                            errorLabel.setText("Username already in use.");
                            return;
                        }
                        if (lower.contains("email already exists")) {
                            errorLabel.setText("Email already in use.");
                            return;
                        }
                        if (lower.contains("unique constraint failed: users.username")) {
                            errorLabel.setText("Username already in use.");
                            return;
                        }
                        if (lower.contains("unique constraint failed: users.email")) {
                            errorLabel.setText("Email already in use.");
                            return;
                        }

                        // fallback
                        errorLabel.setText(msg.isBlank() ? "Registration failed." : msg);
                    }
                }

            }.execute();
        });

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        add(root);
    }

    /* ---------- Header ---------- */

    private JPanel buildHeader(String subtitleText, java.awt.event.ActionListener backAction) {
        JPanel header = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(
                        0, 0, GRAD_START,
                        getWidth(), getHeight(), GRAD_END
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
        subtitle.setForeground(new Color(255, 230, 230));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(backBtn);
        header.add(Box.createVerticalStrut(10));
        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        return header;
    }

    /* ---------- Helpers ---------- */

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

    /* ---------- Button ---------- */

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

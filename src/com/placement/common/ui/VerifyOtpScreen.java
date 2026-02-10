package com.placement.common.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.placement.student.ui.StudentCreateAccountScreen;

public class VerifyOtpScreen extends JFrame {

    private final String email;
    private final String gender;
    private final boolean isCompany;

    private JTextField otpField;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    private InteractiveButton verifyBtn;
    private InteractiveButton resendBtn;

    private static final String TEST_OTP = "123456";

    // Theme colors (student=purple, company=red)
    private final Color GRAD_START;
    private final Color GRAD_END;
    private final Color PRIMARY_BTN;
    private final Color SECONDARY_BTN_BG;
    private final Color SECONDARY_BTN_FG;

    // Student constructor (existing)
    public VerifyOtpScreen(String email, String gender) {
        this(email, gender, false);
    }

    // Company constructor
    public VerifyOtpScreen(String email, boolean isCompany) {
        this(email, null, isCompany);
    }

    // Main constructor
    public VerifyOtpScreen(String email, String gender, boolean isCompany) {
        this.email = email;
        this.gender = gender;
        this.isCompany = isCompany;

        if (isCompany) {
            GRAD_START = new Color(220, 38, 38);
            GRAD_END = new Color(244, 63, 94);
            PRIMARY_BTN = new Color(220, 38, 38);
            SECONDARY_BTN_BG = new Color(254, 226, 226);
            SECONDARY_BTN_FG = new Color(185, 28, 28);
        } else {
            GRAD_START = new Color(99, 102, 241);
            GRAD_END = new Color(124, 58, 237);
            PRIMARY_BTN = new Color(99, 102, 241);
            SECONDARY_BTN_BG = new Color(237, 233, 254);
            SECONDARY_BTN_FG = new Color(79, 70, 229);
        }

        setTitle("Student Placement Portal");
        setSize(420, 620);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
        sendOtp();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel header = buildHeader("Verify OTP", e -> {
            if (isCompany) {
                // company back: go back to CreateAccountScreen (simple)
                new CreateAccountScreen().setVisible(true);
            } else {
                new StudentCreateAccountScreen().setVisible(true);
            }
            dispose();
        });
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(10, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField emailField = new JTextField(email);
        emailField.setEditable(false);
        styleField(emailField);

        otpField = new JTextField();
        styleField(otpField);

        int row = 0;
        addRow(form, gbc, row++, "Email", emailField);
        addRow(form, gbc, row++, "Enter OTP", otpField);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(statusLabel, gbc);

        gbc.gridy = row++;
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        form.add(progressBar, gbc);

        gbc.gridy = row++;

        verifyBtn = new InteractiveButton("Verify", PRIMARY_BTN);

        resendBtn = new InteractiveButton("Resend OTP", SECONDARY_BTN_BG);
        resendBtn.setForeground(SECONDARY_BTN_FG);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttons.setBackground(Color.WHITE);
        buttons.add(verifyBtn);
        buttons.add(resendBtn);

        form.add(buttons, gbc);

        verifyBtn.addActionListener(e -> verifyOtp());
        resendBtn.addActionListener(e -> sendOtp());

        root.add(form, BorderLayout.CENTER);
        add(root);
    }

    private void sendOtp() {
        setBusy(true, "Sending OTP...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                Thread.sleep(500);
                return null;
            }
            @Override protected void done() {
                setBusy(false, "OTP sent. (Testing OTP: 123456)");
            }
        };
        worker.execute();
    }

    private void verifyOtp() {
        String entered = otpField.getText().trim();

        if (entered.isEmpty()) { setError("Please enter the OTP."); return; }
        if (!entered.matches("\\d{6}")) { setError("OTP must be 6 digits."); return; }

        setBusy(true, "Verifying OTP...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                Thread.sleep(450);
                return TEST_OTP.equals(entered);
            }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    if (!ok) { setError("Invalid OTP. Try 123456."); return; }

                    setBusy(false, "Verified!");

                    // ✅ student -> profile picture, company -> logo (same screen)
                    new ProfilePictureScreen(email, gender, isCompany).setVisible(true);
                    dispose();

                } catch (Exception ex) {
                    setError("Verification failed. Try again.");
                }
            }
        };
        worker.execute();
    }

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
        subtitle.setForeground(isCompany ? new Color(255, 230, 230) : new Color(230, 230, 255));
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

    private void setBusy(boolean busy, String msg) {
        statusLabel.setForeground(new Color(60, 60, 60));
        statusLabel.setText(msg);
        progressBar.setVisible(busy);
        progressBar.setIndeterminate(busy);
        verifyBtn.setEnabled(!busy);
        resendBtn.setEnabled(!busy);
    }

    private void setError(String msg) {
        statusLabel.setForeground(Color.RED);
        statusLabel.setText(msg);
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
        verifyBtn.setEnabled(true);
        resendBtn.setEnabled(true);
    }

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

            // If background is light (secondary button), caller sets foreground manually
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

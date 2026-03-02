package com.placement.common.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.placement.student.ui.StudentCreateAccountScreen;
import com.placement.student.ui.StudentOnboardingAfterOtpScreen;
import com.placement.company.ui.CompanyCreateAccountScreen;
import com.placement.common.service.EmailService;
import com.placement.common.dao.UserDao;
import com.placement.common.model.User;
import com.placement.common.service.OtpService;


public class VerifyOtpScreen extends JFrame {

    public enum Purpose {
        STUDENT_SIGNUP,
        COMPANY_SIGNUP,
        FORGOT_PASSWORD,
        TWO_FACTOR
    }

    private final String idText;     // can be email OR username (for 2FA)
    private final String gender;     // only for student signup
    private final boolean companyTheme;
    private final Purpose purpose;

    private JTextField otpField;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    private InteractiveButton verifyBtn;
    private InteractiveButton resendBtn;

    // Backend
    private final OtpService otpService = new OtpService();
    private final UserDao userDao = new UserDao();

    private final Color GRAD_START;
    private final Color GRAD_END;
    private final Color PRIMARY_BTN;
    private final Color SECONDARY_BTN_BG;
    private final Color SECONDARY_BTN_FG;

    // ✅ prevent duplicate auto-send
    private boolean otpSentOnce = false;

    /* ---------------- Constructors you already use ---------------- */

    // Student signup
    public VerifyOtpScreen(String email, String gender) {
        this(email, gender, false, Purpose.STUDENT_SIGNUP);
    }

    // Company signup
    public VerifyOtpScreen(String email, boolean isCompany) {
        this(email, null, isCompany, Purpose.COMPANY_SIGNUP);
    }

    // Forgot password / 2FA reuse
    public VerifyOtpScreen(String idText, Purpose purpose) {
        this(idText, null, false, purpose);
    }

    // Main constructor
    public VerifyOtpScreen(String idText, String gender, boolean companyTheme, Purpose purpose) {
        this.idText = idText;
        this.gender = gender;
        this.companyTheme = companyTheme;
        this.purpose = purpose;

        if (companyTheme) {
            GRAD_START = new Color(220, 38, 38);
            GRAD_END   = new Color(244, 63, 94);
            PRIMARY_BTN = new Color(220, 38, 38);
            SECONDARY_BTN_BG = new Color(254, 226, 226);
            SECONDARY_BTN_FG = new Color(185, 28, 28);
        } else {
            GRAD_START = new Color(99, 102, 241);
            GRAD_END   = new Color(124, 58, 237);
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

        // ✅ SEND OTP AUTOMATICALLY when the window opens (signup + forgot + 2FA)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                if (!otpSentOnce) {
                    otpSentOnce = true;
                    sendOtp();
                }
            }
        });
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        String subtitle = switch (purpose) {
            case FORGOT_PASSWORD -> "Reset Password OTP";
            case TWO_FACTOR -> "Two-Step Verification";
            default -> "Verify OTP";
        };

        JPanel header = buildHeader(subtitle, e -> goBack());
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(18, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String firstLabel = (purpose == Purpose.TWO_FACTOR) ? "Username/Email" : "Email";

        JTextField idField = new JTextField(idText);
        idField.setEditable(false);
        styleField(idField);

        otpField = new JTextField();
        styleField(otpField);

        int row = 0;
        addRow(form, gbc, row++, firstLabel, idField);
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

    private void goBack() {
        switch (purpose) {
            case STUDENT_SIGNUP -> new StudentCreateAccountScreen().setVisible(true);
            case COMPANY_SIGNUP -> new CompanyCreateAccountScreen().setVisible(true);
            case FORGOT_PASSWORD -> new ForgotPasswordScreen(idText).setVisible(true);
            case TWO_FACTOR -> new LoginScreen().setVisible(true);
        }
        dispose();
    }

    private void onVerifiedSuccess() {
        switch (purpose) {
            case STUDENT_SIGNUP -> {
            	new StudentOnboardingAfterOtpScreen(idText, gender).setVisible(true);
            	dispose();

            }
            case COMPANY_SIGNUP -> {
                new ProfilePictureScreen(idText, null, true).setVisible(true);
                dispose();
            }
            case FORGOT_PASSWORD -> {
                new ResetPasswordScreen(idText).setVisible(true);
                dispose();
            }
            case TWO_FACTOR -> {
                JOptionPane.showMessageDialog(
                        this,
                        "Two-step verification successful!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                new LoginScreen().setVisible(true);
                dispose();
            }
        }
    }

    /**
     * Resolve which email to store/verify OTP against.
     * - For TWO_FACTOR, idText may be username/email, so resolve to user's email in DB.
     * - For others, idText is already email.
     */
    private String resolveEmailForOtp() throws Exception {
        if (purpose != Purpose.TWO_FACTOR) return idText.trim();

        User u = userDao.findByUsernameOrEmail(idText.trim());
        if (u == null) return null;
        return u.getEmail();
    }

    private void sendOtp() {
        setBusy(true, "Sending OTP...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                String email = resolveEmailForOtp();
                if (email == null || email.isBlank()) {
                    throw new IllegalArgumentException("Account not found.");
                }
                otpService.issueOtp(email.trim(), purpose.name());
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    if (otpService.isTestMode()) {
                        setBusy(false, "TEST MODE: use OTP 123456");
                    } else {
                        setBusy(false, "OTP sent to your email.");
                    }
                } catch (Exception ex) {
                    Throwable root = ex;
                    if (ex instanceof java.util.concurrent.ExecutionException && ex.getCause() != null) {
                        root = ex.getCause();
                    }
                    setError("Failed to send OTP: " + (root.getMessage() == null ? "Unknown error" : root.getMessage()));
                }
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
                String email = resolveEmailForOtp();
                if (email == null || email.isBlank()) return false;

                Thread.sleep(150);
                return otpService.verifyOtp(email, purpose.name(), entered);
            }

            @Override protected void done() {
                try {
                    boolean ok = get();
                    if (!ok) {
                        setError(otpService.isTestMode()
                                ? "Invalid or expired OTP. Try 123456."
                                : "Invalid or expired OTP.");
                        return;
                    }

                    // ✅ For signups, mark verified in DB
                    if (purpose == Purpose.STUDENT_SIGNUP || purpose == Purpose.COMPANY_SIGNUP) {
                        String email = resolveEmailForOtp();
                        if (email != null) userDao.setVerifiedByEmail(email, true);
                    }
                    
                    if (purpose == Purpose.STUDENT_SIGNUP || purpose == Purpose.COMPANY_SIGNUP) {
                        String email = resolveEmailForOtp();

                        new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() {
                                try {
                                    if (email == null || email.isBlank()) return null;

                                    User u = userDao.findByUsernameOrEmail(email);
                                    String username = (u != null) ? u.getUsername() : "";
                                    String role = (u != null && u.getRole() != null) ? u.getRole().name() : "USER";

                                    EmailService mail = new EmailService();
                                    mail.sendAccountCreatedEmail(email, username, role);

                                } catch (Exception ex) {
                                    ex.printStackTrace(); // IMPORTANT: don’t silently swallow send failures
                                }
                                return null;
                            }
                        }.execute();
                    }


                    setBusy(false, "Verified!");
                    onVerifiedSuccess();

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
        subtitle.setForeground(companyTheme ? new Color(255, 230, 230) : new Color(230, 230, 255));
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

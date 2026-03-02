package com.placement.company.ui;

import com.placement.common.db.DB;
import com.placement.common.ui.LoginScreen;
import com.placement.common.util.FileStorageUtil;
import com.placement.company.dao.CompanyDao;
import com.placement.company.model.CompanyProfile;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CompanyProfileScreen extends JFrame {

    private static final Color GRAD_START = new Color(220, 38, 38);
    private static final Color GRAD_END   = new Color(244, 63, 94);

    private static final Color BG = new Color(245, 247, 251);
    private static final Color TEXT_MUTED = new Color(90, 98, 112);

    private final int companyUserId;
    private String companyName;

    private final CompanyDao companyDao = new CompanyDao();

    // UI
    private JLabel avatarBig;
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField websiteField;
    private JTextField industryField;
    private JTextField sizeField;
    private JTextArea addressArea;

    private JButton saveBtn;
    private JButton deleteBtn;
    private JButton changeLogoBtn;
    private JButton backBtn;

    public CompanyProfileScreen(int companyUserId, String companyName) {
        this.companyUserId = companyUserId;
        this.companyName = (companyName == null || companyName.isBlank()) ? "Company" : companyName;

        setTitle("Company Profile - Student Placement Portal");
        setSize(980, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));

        initUI();
        loadProfileFromDb();

        SwingUtilities.invokeLater(() -> setExtendedState(JFrame.MAXIMIZED_BOTH));
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JComponent buildHeader() {
        JPanel header = new GradientPanel(GRAD_START, GRAD_END);
        header.setPreferredSize(new Dimension(1100, 150));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));

        JPanel iconBox = new RoundedPanel(18, new Color(255, 255, 255, 30));
        iconBox.setPreferredSize(new Dimension(56, 56));
        iconBox.setMaximumSize(new Dimension(56, 56));
        iconBox.setLayout(new GridBagLayout());

        JLabel icon = new JLabel("\uD83C\uDFE2");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        icon.setForeground(Color.WHITE);
        iconBox.add(icon);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBorder(new EmptyBorder(0, 14, 0, 0));

        JLabel title = new JLabel("Company Profile");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Edit details • Update logo • Delete company");
        subtitle.setForeground(new Color(255, 240, 245));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        titles.add(title);
        titles.add(Box.createVerticalStrut(6));
        titles.add(subtitle);

        left.add(iconBox);
        left.add(titles);

        JButton logout = new OutlineButton("Logout");
        logout.setForeground(Color.WHITE);
        logout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "Log out now?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                new LoginScreen().setVisible(true);
                dispose();
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(logout);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildBody() {
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(18, 28, 28, 28));

        content.add(buildProfileCard());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(BG);
        return scroll;
    }

    private JComponent buildProfileCard() {
        RoundedPanel card = new RoundedPanel(18, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        // Top row: logo + buttons
        JPanel top = new JPanel(new BorderLayout(18, 0));
        top.setOpaque(false);

        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));

        avatarBig = new JLabel();
        avatarBig.setPreferredSize(new Dimension(96, 96));
        avatarBig.setMaximumSize(new Dimension(96, 96));
        avatarBig.setAlignmentX(Component.LEFT_ALIGNMENT);

        // click logo to change too
        avatarBig.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatarBig.setToolTipText("Click to change logo");
        avatarBig.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                chooseAndSaveLogo();
            }
        });

        JLabel logoHint = new JLabel("Company Logo");
        logoHint.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoHint.setForeground(new Color(15, 23, 42));
        logoHint.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel logoHint2 = new JLabel("Click logo or use button below");
        logoHint2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoHint2.setForeground(TEXT_MUTED);

        changeLogoBtn = new SoftButton("Change Logo", new Color(226, 232, 240), new Color(15, 23, 42));
        changeLogoBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        changeLogoBtn.addActionListener(e -> chooseAndSaveLogo());

        logoPanel.add(avatarBig);
        logoPanel.add(logoHint);
        logoPanel.add(logoHint2);
        logoPanel.add(Box.createVerticalStrut(10));
        logoPanel.add(changeLogoBtn);

        top.add(logoPanel, BorderLayout.WEST);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        nameField = input();
        phoneField = input();
        websiteField = input();
        industryField = input();
        sizeField = input();

        addressArea = new JTextArea(4, 24);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        int r = 0;
        addRow(form, gc, r++, "Company Name", nameField);
        addRow(form, gc, r++, "Phone", phoneField);
        addRow(form, gc, r++, "Website", websiteField);
        addRow(form, gc, r++, "Industry", industryField);
        addRow(form, gc, r++, "Company Size", sizeField);

        gc.gridx = 0; gc.gridy = r; gc.weightx = 0; gc.gridwidth = 1;
        form.add(label("Address"), gc);
        gc.gridx = 1; gc.gridy = r; gc.weightx = 1; gc.gridwidth = 2;
        form.add(new JScrollPane(addressArea), gc);

        top.add(form, BorderLayout.CENTER);

        // Bottom actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(14, 0, 0, 0));

        backBtn = new SoftButton("Back to Dashboard", new Color(226, 232, 240), new Color(15, 23, 42));
        backBtn.addActionListener(e -> goBackToDashboard());

        saveBtn = new SolidButton("Save Changes", GRAD_START);
        saveBtn.addActionListener(e -> saveProfile());

        deleteBtn = new SolidButton("Delete Company", new Color(153, 27, 27));
        deleteBtn.addActionListener(e -> deleteCompany());

        actions.add(backBtn);
        actions.add(saveBtn);
        actions.add(deleteBtn);

        card.add(top, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        // initial avatar placeholder
        refreshAvatar();

        return card;
    }

    private JTextField input() {
        JTextField tf = new JTextField(22);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(55, 65, 81));
        return l;
    }

    private void addRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.gridwidth = 1;
        form.add(this.label(label), gc);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1; gc.gridwidth = 2;
        form.add(field, gc);
    }

    private void loadProfileFromDb() {
        setBusy(true);

        new SwingWorker<CompanyProfile, Void>() {
            @Override protected CompanyProfile doInBackground() throws Exception {
                return companyDao.findByUserId(companyUserId);
            }

            @Override protected void done() {
                try {
                    CompanyProfile p = get();
                    if (p != null) {
                        nameField.setText(nz(p.getCompanyName()));
                        phoneField.setText(nz(p.getPhone()));
                        websiteField.setText(nz(p.getWebsite()));
                        industryField.setText(nz(p.getIndustry()));
                        sizeField.setText(nz(p.getCompanySize()));
                        addressArea.setText(nz(p.getAddress()));
                        companyName = nz(p.getCompanyName(), companyName);
                    } else {
                        // no profile row yet - keep defaults
                        nameField.setText(companyName);
                    }
                    refreshAvatar();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            CompanyProfileScreen.this,
                            "Failed to load profile: " + (ex.getMessage() == null ? "" : ex.getMessage()),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void refreshAvatar() {
        try {
            String path = companyDao.getLogoPath(companyUserId);
            avatarBig.setIcon(buildCircularIcon(path, 96));
        } catch (Exception e) {
            avatarBig.setIcon(buildCircularIcon(null, 96));
        }
    }

    private ImageIcon buildCircularIcon(String path, int size) {
        try {
            BufferedImage img = null;

            if (path != null && !path.isBlank()) {
                File f = new File(path);
                if (f.exists()) img = ImageIO.read(f);
            }

            if (img == null) {
                // placeholder
                BufferedImage ph = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = ph.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillOval(0, 0, size, size);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
                FontMetrics fm = g2.getFontMetrics();
                String s = "\uD83C\uDFE2"; // 🏢
                int x = (size - fm.stringWidth(s)) / 2;
                int y = (size - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(s, x, y);
                g2.dispose();
                return new ImageIcon(ph);
            }

            Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);

            BufferedImage circ = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circ.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new Ellipse2D.Float(0, 0, size, size));
            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();

            return new ImageIcon(circ);
        } catch (Exception e) {
            return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
        }
    }

    private void chooseAndSaveLogo() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Company Logo (png/jpg)");
        int ok = fc.showOpenDialog(this);
        if (ok != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (file == null) return;

        setBusy(true);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                String saved = FileStorageUtil.saveCompanyLogo(file, companyUserId);
                companyDao.updateLogoPath(companyUserId, saved);
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    refreshAvatar();
                    JOptionPane.showMessageDialog(
                            CompanyProfileScreen.this,
                            "Logo updated successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            CompanyProfileScreen.this,
                            "Failed to update logo: " + (ex.getMessage() == null ? "" : ex.getMessage()),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void saveProfile() {
        String newName = nameField.getText().trim();
        if (newName.isBlank()) {
            JOptionPane.showMessageDialog(this, "Company Name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String phone = phoneField.getText().trim();
        String web = websiteField.getText().trim();
        String ind = industryField.getText().trim();
        String size = sizeField.getText().trim();
        String addr = addressArea.getText().trim();

        CompanyProfile profile = new CompanyProfile(
                newName,
                phone.isBlank() ? null : phone,
                web.isBlank() ? null : web,
                ind.isBlank() ? null : ind,
                size.isBlank() ? null : size,
                addr.isBlank() ? null : addr
        );

        setBusy(true);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                // ✅ transactional update + propagate company_name to job_listings if changed
                try (Connection con = DB.getConnection()) {
                    con.setAutoCommit(false);

                    String oldName = null;
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT company_name FROM companies WHERE user_id=? LIMIT 1")) {
                        ps.setInt(1, companyUserId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) oldName = rs.getString(1);
                        }
                    }

                    try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE companies
                        SET company_name=?,
                            phone=?,
                            website=?,
                            industry=?,
                            company_size=?,
                            address=?
                        WHERE user_id=?
                    """)) {
                        ps.setString(1, profile.getCompanyName());
                        ps.setString(2, profile.getPhone());
                        ps.setString(3, profile.getWebsite());
                        ps.setString(4, profile.getIndustry());
                        ps.setString(5, profile.getCompanySize());
                        ps.setString(6, profile.getAddress());
                        ps.setInt(7, companyUserId);
                        ps.executeUpdate();
                    }

                    if (oldName != null && !oldName.equals(profile.getCompanyName())) {
                        try (PreparedStatement ps = con.prepareStatement(
                                "UPDATE job_listings SET company_name=? WHERE company_name=?")) {
                            ps.setString(1, profile.getCompanyName());
                            ps.setString(2, oldName);
                            ps.executeUpdate();
                        }
                    }

                    con.commit();
                }

                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    companyName = newName;
                    JOptionPane.showMessageDialog(
                            CompanyProfileScreen.this,
                            "Profile updated successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            CompanyProfileScreen.this,
                            "Failed to save profile: " + (ex.getMessage() == null ? "" : ex.getMessage()),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void deleteCompany() {
        int ok = JOptionPane.showConfirmDialog(
                this,
                "This will delete your company account and ALL its job listings.\n" +
                        "Applications, interviews and offers will also be removed.\n\n" +
                        "Do you want to continue?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (ok != JOptionPane.YES_OPTION) return;

        setBusy(true);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                // ✅ Delete company jobs first (cascades applications/offers/interviews)
                try (Connection con = DB.getConnection()) {
                    con.setAutoCommit(false);

                    String currentName = companyName;
                    // refresh from DB if possible
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT company_name FROM companies WHERE user_id=? LIMIT 1")) {
                        ps.setInt(1, companyUserId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) currentName = rs.getString(1);
                        }
                    }

                    if (currentName != null && !currentName.isBlank()) {
                        try (PreparedStatement ps = con.prepareStatement(
                                "DELETE FROM job_listings WHERE company_name=?")) {
                            ps.setString(1, currentName);
                            ps.executeUpdate();
                        }
                    }

                    // deleting user will cascade to companies row
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM users WHERE id=? AND role='COMPANY'")) {
                        ps.setInt(1, companyUserId);
                        ps.executeUpdate();
                    }

                    con.commit();
                }
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(
                            CompanyProfileScreen.this,
                            "Company deleted successfully.",
                            "Deleted",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    new LoginScreen().setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            CompanyProfileScreen.this,
                            "Failed to delete company: " + (ex.getMessage() == null ? "" : ex.getMessage()),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void goBackToDashboard() {
        new CompanyDashboardScreen(companyUserId, companyName).setVisible(true);
        dispose();
    }

    private void setBusy(boolean busy) {
        if (saveBtn != null) saveBtn.setEnabled(!busy);
        if (deleteBtn != null) deleteBtn.setEnabled(!busy);
        if (changeLogoBtn != null) changeLogoBtn.setEnabled(!busy);
        if (backBtn != null) backBtn.setEnabled(!busy);
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static String nz(String s, String fallback) { return (s == null || s.isBlank()) ? fallback : s; }

    /* ============================= Components ============================= */

    private static class GradientPanel extends JPanel {
        private final Color start;
        private final Color end;

        GradientPanel(Color start, Color end) {
            this.start = start;
            this.end = end;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;

        RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class SolidButton extends JButton {
        private final int radius = 18;

        SolidButton(String text, Color bg) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setBackground(bg);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class SoftButton extends JButton {
        private final int radius = 18;

        SoftButton(String text, Color bg, Color fg) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(fg);
            setBackground(bg);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class OutlineButton extends JButton {
        OutlineButton(String text) {
            super(text);
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setFocusPainted(false);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 110), 1, true),
                    BorderFactory.createEmptyBorder(10, 16, 10, 16)
            ));
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 1, true),
                            BorderFactory.createEmptyBorder(10, 16, 10, 16)
                    ));
                }

                @Override public void mouseExited(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 255, 255, 110), 1, true),
                            BorderFactory.createEmptyBorder(10, 16, 10, 16)
                    ));
                }
            });
        }
    }
}

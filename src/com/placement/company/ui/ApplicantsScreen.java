package com.placement.company.ui;

import com.placement.company.dao.CompanyApplicationDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ApplicantsScreen extends JFrame {

    // Theme (same as company dashboard)
    private static final Color GRAD_START = new Color(220, 38, 38);
    private static final Color GRAD_END   = new Color(244, 63, 94);

    private static final Color BG = new Color(245, 247, 251);
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(90, 98, 112);

    private final JFrame parent;
    private final String companyName;
    private final long jobId;
    private final String jobTitle;

    private final CompanyApplicationDao dao = new CompanyApplicationDao();

    // Filter UI
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JLabel countLabel;

    // Table
    private DefaultTableModel model;
    private JTable table;

    // Action buttons
    private JButton openResumeBtn;
    private JButton shortlistBtn;
    private JButton rejectBtn;
    private JButton interviewBtn;

    // Data
    private List<CompanyApplicationDao.ApplicationRow> allRows = new ArrayList<>();
    private List<CompanyApplicationDao.ApplicationRow> filteredRows = new ArrayList<>();

    public ApplicantsScreen(JFrame parent, String companyName, long jobId, String jobTitle) {
        this.parent = parent;
        this.companyName = companyName;
        this.jobId = jobId;
        this.jobTitle = jobTitle;

        setTitle("Applicants - " + jobTitle);
        setSize(1150, 740);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(980, 650));

        initUI();
        loadApplicants();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    /* ============================= Header ============================= */

    private JComponent buildHeader() {
        JPanel header = new GradientPanel(GRAD_START, GRAD_END);
        header.setPreferredSize(new Dimension(1100, 160));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(22, 24, 18, 24));

        // Left: icon + title/subtitle
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));

        JPanel iconBox = new RoundedPanel(18, new Color(255, 255, 255, 35));
        iconBox.setPreferredSize(new Dimension(56, 56));
        iconBox.setMaximumSize(new Dimension(56, 56));
        iconBox.setLayout(new GridBagLayout());

        JLabel icon = new JLabel("\uD83D\uDC65"); // 👥
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        icon.setForeground(Color.WHITE);
        iconBox.add(icon);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBorder(new EmptyBorder(0, 14, 0, 0));

        JLabel title = new JLabel("Applicants");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Job: " + jobTitle + "  |  ID: " + jobId);
        subtitle.setForeground(new Color(255, 235, 240));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        titles.add(title);
        titles.add(Box.createVerticalStrut(6));
        titles.add(subtitle);

        left.add(iconBox);
        left.add(titles);

        // Right: back + refresh
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton back = new OutlineButton("← Back");
        back.setForeground(Color.WHITE);
        back.addActionListener(e -> {
            if (parent != null) parent.setVisible(true);
            dispose();
        });

        JButton refresh = new SoftButton("Refresh", new Color(255, 255, 255, 210), TEXT_DARK);
        refresh.addActionListener(e -> loadApplicants());

        right.add(back);
        right.add(refresh);

        // Filter bar inside header (white translucent card)
        JPanel filterCard = new RoundedPanel(18, new Color(255, 255, 255, 235));
        filterCard.setLayout(new BorderLayout());
        filterCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);

        searchField = new JTextField(24);
        styleSearchField(searchField);
        searchField.setToolTipText("Search by name or email");

        statusFilter = new JComboBox<>(new String[]{
                "All", "APPLIED", "SHORTLISTED", "REJECTED", "INTERVIEW_SCHEDULED"
        });
        styleCombo(statusFilter);

        countLabel = new JLabel("0 results");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countLabel.setForeground(TEXT_MUTED);

        filters.add(new JLabel("Search:"));
        filters.add(searchField);
        filters.add(Box.createHorizontalStrut(6));
        filters.add(new JLabel("Status:"));
        filters.add(statusFilter);
        filters.add(Box.createHorizontalStrut(12));
        filters.add(countLabel);

        filterCard.add(filters, BorderLayout.WEST);

        // Listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        statusFilter.addActionListener(e -> applyFilters());

        JPanel headerWrap = new JPanel();
        headerWrap.setOpaque(false);
        headerWrap.setLayout(new BoxLayout(headerWrap, BoxLayout.Y_AXIS));
        headerWrap.add(makeRow(left, right));
        headerWrap.add(Box.createVerticalStrut(12));
        headerWrap.add(filterCard);

        header.add(headerWrap, BorderLayout.CENTER);
        return header;
    }

    private JPanel makeRow(JComponent left, JComponent right) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private void styleSearchField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        combo.setBackground(Color.WHITE);
    }

    /* ============================= Center ============================= */

    private JComponent buildCenter() {
        // Table model
        model = new DefaultTableModel(
                new Object[]{"Applicant", "Email", "GPA", "Year", "Status", "Applied At", "Resume"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(36);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setReorderingAllowed(false);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(235, 240, 248));
        table.setIntercellSpacing(new Dimension(0, 1));

        // Status pill renderer
        int statusCol = 4;
        table.getColumnModel().getColumn(statusCol).setCellRenderer(new StatusPillRenderer());

        // Resume renderer (simple badge)
        int resumeCol = 6;
        table.getColumnModel().getColumn(resumeCol).setCellRenderer(new ResumeBadgeRenderer());

        table.getSelectionModel().addListSelectionListener(e -> updateButtons());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);

        // White rounded card around table
        JPanel cardInner = new JPanel(new BorderLayout());
        cardInner.setBackground(Color.WHITE);
        cardInner.setBorder(new EmptyBorder(14, 14, 14, 14));
        cardInner.add(sp, BorderLayout.CENTER);

        RoundedPanel card = new RoundedPanel(18, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        card.add(cardInner, BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(16, 18, 10, 18));
        wrap.add(card, BorderLayout.CENTER);

        return wrap;
    }

    /* ============================= Footer ============================= */

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(BG);
        footer.setBorder(new EmptyBorder(0, 18, 18, 18));

        openResumeBtn = new SoftButton("Open Resume", new Color(226, 232, 240), TEXT_DARK);
        shortlistBtn  = new SoftButton("Shortlist", new Color(236, 253, 245), new Color(22, 101, 52));
        rejectBtn     = new SoftButton("Reject", new Color(254, 226, 226), new Color(153, 27, 27));
        interviewBtn  = new SolidButton("Schedule Interview", GRAD_START);

        openResumeBtn.setEnabled(false);
        shortlistBtn.setEnabled(false);
        rejectBtn.setEnabled(false);
        interviewBtn.setEnabled(false);

        openResumeBtn.addActionListener(e -> openResume());
        shortlistBtn.addActionListener(e -> changeStatus("SHORTLISTED"));
        rejectBtn.addActionListener(e -> changeStatus("REJECTED"));
        interviewBtn.addActionListener(e -> {
            var r = selectedRow();
            if (r == null) return;
            new ScheduleInterviewDialog(this, r, dao, this::loadApplicants).setVisible(true);
        });


        footer.add(openResumeBtn);
        footer.add(shortlistBtn);
        footer.add(rejectBtn);
        footer.add(interviewBtn);

        return footer;
    }

    /* ============================= Data / Filtering ============================= */

    private void loadApplicants() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        model.setRowCount(0);
        allRows.clear();
        filteredRows.clear();

        new SwingWorker<List<CompanyApplicationDao.ApplicationRow>, Void>() {
            @Override protected List<CompanyApplicationDao.ApplicationRow> doInBackground() {
                return dao.listApplicantsForJob(companyName, jobId);
            }

            @Override protected void done() {
                try {
                    allRows = get();
                    applyFilters();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ApplicantsScreen.this,
                            "Failed to load applicants: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                    updateButtons();
                }
            }
        }.execute();
    }

    private void applyFilters() {
        String q = (searchField == null) ? "" : searchField.getText().trim().toLowerCase();
        String status = (statusFilter == null) ? "All" : String.valueOf(statusFilter.getSelectedItem());

        filteredRows.clear();

        for (CompanyApplicationDao.ApplicationRow r : allRows) {
            boolean statusOk = status.equals("All") || (r.status != null && r.status.equalsIgnoreCase(status));
            if (!statusOk) continue;

            if (q.isEmpty()) {
                filteredRows.add(r);
                continue;
            }

            String hay = (r.studentName == null ? "" : r.studentName.toLowerCase()) + " " +
                    (r.email == null ? "" : r.email.toLowerCase());

            if (hay.contains(q)) filteredRows.add(r);
        }

        rebuildTable();
        countLabel.setText(filteredRows.size() + " results");
        updateButtons();
    }

    private void rebuildTable() {
        model.setRowCount(0);
        for (CompanyApplicationDao.ApplicationRow r : filteredRows) {
            model.addRow(new Object[]{
                    r.studentName,
                    r.email,
                    r.gpa == null ? "—" : String.format("%.2f", r.gpa),
                    r.yearOfStudy == null ? "—" : r.yearOfStudy,
                    r.status,
                    r.appliedAt,
                    (r.resumePath == null || r.resumePath.isBlank()) ? "NO" : "YES"
            });
        }
    }

    private CompanyApplicationDao.ApplicationRow selectedRow() {
        int idx = table.getSelectedRow();
        if (idx < 0 || idx >= filteredRows.size()) return null;
        return filteredRows.get(idx);
    }

    private void updateButtons() {
        CompanyApplicationDao.ApplicationRow r = selectedRow();
        boolean has = r != null;

        openResumeBtn.setEnabled(has && r.resumePath != null && !r.resumePath.isBlank());
        shortlistBtn.setEnabled(has);
        rejectBtn.setEnabled(has);

        boolean rejected = has && r.status != null && r.status.equalsIgnoreCase("REJECTED");
        interviewBtn.setEnabled(has && !rejected);
        interviewBtn.setToolTipText(rejected ? "Cannot schedule interview for rejected applicant." : null);
    }

    /* ============================= Actions ============================= */

    private void openResume() {
        CompanyApplicationDao.ApplicationRow r = selectedRow();
        if (r == null) return;

        try {
            File f = new File(r.resumePath);
            if (!f.exists()) {
                JOptionPane.showMessageDialog(this, "Resume file not found:\n" + r.resumePath, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cannot open resume: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changeStatus(String newStatus) {
        CompanyApplicationDao.ApplicationRow r = selectedRow();
        if (r == null) return;

        boolean ok = dao.updateApplicationStatus(r.applicationId, newStatus);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        loadApplicants();
    }

    private void openScheduleInterviewDialog() {
        CompanyApplicationDao.ApplicationRow r = selectedRow();
        if (r == null) return;

        if (r.status != null && r.status.equalsIgnoreCase("REJECTED")) {
            JOptionPane.showMessageDialog(this, "Rejected applicants cannot be interviewed.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField when = new JTextField("2026-02-20 10:00:00");
        JComboBox<String> mode = new JComboBox<>(new String[]{"Online", "Face-to-face"});
        JTextField location = new JTextField("Google Meet link / Room");
        JTextArea notes = new JTextArea(4, 26);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane notesScroll = new JScrollPane(notes);
        notesScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 226, 235), 1, true));
        notesScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        styleSearchField(when);
        styleCombo(mode);
        styleSearchField(location);

        mode.addActionListener(e -> {
            String m = String.valueOf(mode.getSelectedItem());
            if ("Online".equalsIgnoreCase(m)) location.setText("Google Meet / Zoom link");
            else location.setText("Office address / Room");
        });

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        p.add(new JLabel("Scheduled at (YYYY-MM-DD HH:MM:SS)"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        p.add(when, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        p.add(new JLabel("Mode"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        p.add(mode, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        p.add(new JLabel("Location / Link"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        p.add(location, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        p.add(new JLabel("Notes"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        p.add(notesScroll, gbc);

        int res = JOptionPane.showConfirmDialog(this, p, "Schedule Interview", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String scheduledAt = when.getText().trim();
        if (!scheduledAt.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format.\nUse: YYYY-MM-DD HH:MM:SS",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            long interviewId = dao.scheduleInterview(
                    r.applicationId,
                    scheduledAt,
                    String.valueOf(mode.getSelectedItem()),
                    location.getText().trim(),
                    notes.getText().trim()
            );

            if (interviewId <= 0) {
                JOptionPane.showMessageDialog(this, "Failed to schedule interview.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Interview scheduled!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadApplicants();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ============================= Renderers ============================= */

    private static class StatusPillRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String s = value == null ? "" : value.toString().toUpperCase();

            lbl.setText(" " + s + " ");
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setBorder(new EmptyBorder(4, 10, 4, 10));

            Color bg;
            Color fg;

            switch (s) {
                case "REJECTED" -> { bg = new Color(254, 226, 226); fg = new Color(153, 27, 27); }
                case "SHORTLISTED" -> { bg = new Color(220, 252, 231); fg = new Color(22, 101, 52); }
                case "INTERVIEW_SCHEDULED" -> { bg = new Color(219, 234, 254); fg = new Color(30, 64, 175); }
                case "APPLIED" -> { bg = new Color(255, 247, 237); fg = new Color(154, 52, 18); }
                default -> { bg = new Color(226, 232, 240); fg = new Color(15, 23, 42); }
            }

            lbl.setOpaque(true);
            lbl.setBackground(isSelected ? table.getSelectionBackground() : bg);
            lbl.setForeground(isSelected ? table.getSelectionForeground() : fg);

            return lbl;
        }
    }

    private static class ResumeBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String s = value == null ? "NO" : value.toString().toUpperCase();

            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
            lbl.setOpaque(true);

            if ("YES".equals(s)) {
                lbl.setText(" UPLOADED ");
                lbl.setBackground(isSelected ? table.getSelectionBackground() : new Color(220, 252, 231));
                lbl.setForeground(isSelected ? table.getSelectionForeground() : new Color(22, 101, 52));
            } else {
                lbl.setText(" — ");
                lbl.setBackground(isSelected ? table.getSelectionBackground() : new Color(226, 232, 240));
                lbl.setForeground(isSelected ? table.getSelectionForeground() : new Color(90, 98, 112));
            }
            return lbl;
        }
    }

    /* ============================= Dashboard-style Components ============================= */

    private static class GradientPanel extends JPanel {
        private final Color start;
        private final Color end;

        GradientPanel(Color start, Color end) {
            this.start = start;
            this.end = end;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
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

        @Override
        protected void paintComponent(Graphics g) {
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

        @Override
        protected void paintComponent(Graphics g) {
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

        @Override
        protected void paintComponent(Graphics g) {
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
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 140), 1, true),
                    BorderFactory.createEmptyBorder(10, 16, 10, 16)
            ));
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }
}

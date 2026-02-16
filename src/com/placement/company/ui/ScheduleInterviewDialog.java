package com.placement.company.ui;

import com.placement.company.dao.CompanyApplicationDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ScheduleInterviewDialog extends JDialog {

    private static final Color GRAD_START = new Color(220, 38, 38);
    private static final Color GRAD_END   = new Color(244, 63, 94);

    private static final Color TEXT_DARK  = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(90, 98, 112);

    private final CompanyApplicationDao dao;
    private final CompanyApplicationDao.ApplicationRow app;
    private final Runnable onSuccess;

    private JTextField dateField;
    private JTextField timeField;
    private JComboBox<String> modeBox;
    private JTextField locationField;
    private JTextArea notesArea;

    private JButton scheduleBtn;

    public ScheduleInterviewDialog(JFrame owner,
                                   CompanyApplicationDao.ApplicationRow app,
                                   CompanyApplicationDao dao,
                                   Runnable onSuccess) {
        super(owner, "Schedule Interview", true);
        this.dao = dao;
        this.app = app;
        this.onSuccess = onSuccess;

        setSize(720, 520);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setContentPane(buildUI());
        prefill();
        applyModeHint();
    }

    private JComponent buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildHeader() {
        JPanel header = new GradientPanel(GRAD_START, GRAD_END);
        header.setPreferredSize(new Dimension(720, 120));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));

        JPanel iconBox = new RoundedPanel(18, new Color(255, 255, 255, 35));
        iconBox.setPreferredSize(new Dimension(52, 52));
        iconBox.setMaximumSize(new Dimension(52, 52));
        iconBox.setLayout(new GridBagLayout());

        JLabel icon = new JLabel("\uD83D\uDCC5"); // 📅
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        icon.setForeground(Color.WHITE);
        iconBox.add(icon);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBorder(new EmptyBorder(0, 12, 0, 0));

        JLabel t = new JLabel("Schedule Interview");
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel sub = new JLabel("Applicant: " + safe(app.studentName) + "  |  Status: " + safe(app.status));
        sub.setForeground(new Color(255, 235, 240));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        titles.add(t);
        titles.add(Box.createVerticalStrut(6));
        titles.add(sub);

        left.add(iconBox);
        left.add(titles);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private JComponent buildForm() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(new Color(245, 247, 251));
        wrap.setBorder(new EmptyBorder(14, 18, 8, 18));

        RoundedPanel card = new RoundedPanel(18, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        dateField = new JTextField();
        timeField = new JTextField();
        modeBox = new JComboBox<>(new String[]{"Online", "Face-to-face"});
        locationField = new JTextField();

        styleField(dateField);
        styleField(timeField);
        styleCombo(modeBox);
        styleField(locationField);

        notesArea = new JTextArea(5, 28);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 226, 235), 1, true));
        notesScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        int y = 0;

        addRow(form, gbc, y++, "Date (YYYY-MM-DD)", dateField);
        addRow(form, gbc, y++, "Time (HH:MM)", timeField);
        addRow(form, gbc, y++, "Mode", modeBox);
        addRow(form, gbc, y++, "Location / Link", locationField);

        // Notes row
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; gbc.weighty = 0;
        JLabel notesLbl = new JLabel("Notes");
        notesLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesLbl.setForeground(TEXT_MUTED);
        form.add(notesLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(notesScroll, gbc);

        modeBox.addActionListener(e -> applyModeHint());

        card.add(form, BorderLayout.CENTER);
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(245, 247, 251));
        footer.setBorder(new EmptyBorder(0, 18, 14, 18));

        JButton cancel = new SoftButton("Cancel", new Color(226, 232, 240), TEXT_DARK);
        cancel.addActionListener(e -> dispose());

        scheduleBtn = new SolidButton("Schedule", GRAD_START);
        scheduleBtn.addActionListener(e -> onSchedule());

        footer.add(cancel);
        footer.add(scheduleBtn);
        return footer;
    }

    private void onSchedule() {
        // UI guard (DAO also guards)
        if (app.status != null && app.status.equalsIgnoreCase("REJECTED")) {
            JOptionPane.showMessageDialog(this,
                    "Rejected applicants cannot be interviewed.",
                    "Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String date = dateField.getText().trim();
        String time = timeField.getText().trim();

        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Date must be YYYY-MM-DD.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!time.matches("\\d{2}:\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Time must be HH:MM (24h).", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String scheduledAt = date + " " + time + ":00";
        String mode = String.valueOf(modeBox.getSelectedItem());
        String location = locationField.getText().trim();
        String notes = notesArea.getText().trim();

        if (location.isBlank()) {
            JOptionPane.showMessageDialog(this, "Location/Link is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        scheduleBtn.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() {
                return dao.scheduleInterview(app.applicationId, scheduledAt, mode, location, notes);
            }

            @Override
            protected void done() {
                try {
                    long id = get();
                    if (id <= 0) throw new RuntimeException("Failed to create interview.");
                    JOptionPane.showMessageDialog(ScheduleInterviewDialog.this,
                            "Interview scheduled successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    if (onSuccess != null) onSuccess.run();
                    dispose();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ScheduleInterviewDialog.this,
                            ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    scheduleBtn.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private void prefill() {
        // simple defaults
        dateField.setText(java.time.LocalDate.now().plusDays(3).toString());
        timeField.setText("10:00");
        modeBox.setSelectedItem("Online");
        locationField.setText("Google Meet / Zoom link");
    }

    private void applyModeHint() {
        String m = String.valueOf(modeBox.getSelectedItem());
        if ("Online".equalsIgnoreCase(m)) {
            if (locationField.getText().isBlank() || locationField.getText().toLowerCase().contains("office")) {
                locationField.setText("Google Meet / Zoom link");
            }
        } else {
            if (locationField.getText().isBlank() || locationField.getText().toLowerCase().contains("zoom")
                    || locationField.getText().toLowerCase().contains("meet")) {
                locationField.setText("Office address / Room");
            }
        }
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int y, String label, JComponent field) {
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_MUTED);

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        form.add(l, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void styleField(JTextField field) {
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

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    /* ---------- small UI helpers ---------- */

    private static class GradientPanel extends JPanel {
        private final Color start, end;
        GradientPanel(Color start, Color end) { this.start = start; this.end = end; setOpaque(false); }
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
        private final int radius; private final Color fill;
        RoundedPanel(int radius, Color fill) { this.radius = radius; this.fill = fill; setOpaque(false); }
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
}

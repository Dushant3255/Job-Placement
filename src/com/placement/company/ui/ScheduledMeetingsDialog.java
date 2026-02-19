package com.placement.company.ui;

import com.placement.company.dao.CompanyInterviewDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.net.URI;
import java.util.List;

public class ScheduledMeetingsDialog extends JDialog {

    // Match Company Dashboard theme
    private static final Color GRAD_START = new Color(220, 38, 38);
    private static final Color GRAD_END   = new Color(244, 63, 94);

    private static final Color TEXT_DARK  = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(90, 98, 112);

    private final String companyName;
    private final CompanyInterviewDao dao = new CompanyInterviewDao();

    private final DefaultTableModel model;
    private final JTable table;
    private final JComboBox<String> statusFilter;

    // Details
    private JLabel jobVal, studentVal, whenVal, modeVal, statusVal, locationVal;
    private JTextField linkField;
    private JTextArea notesArea;

    private JButton startBtn;
    private JButton completeBtn;
    private JButton cancelBtn; // ✅ NEW

    public ScheduledMeetingsDialog(JFrame owner, String companyName) {
        super(owner, "Scheduled Meetings", true);
        this.companyName = companyName;

        setSize(980, 560);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(new Object[]{
                "Interview ID", "Job", "Student", "Scheduled At", "Mode", "Status"
        }, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::onRowSelected);

        statusFilter = new JComboBox<>(new String[]{"All", "SCHEDULED", "COMPLETED", "CANCELLED"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.addActionListener(e -> refresh());

        setContentPane(buildUI());
        refresh();
    }

    private JComponent buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 251)); // dashboard base bg

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        return root;
    }

    private JComponent buildHeader() {
        JPanel header = new GradientPanel(GRAD_START, GRAD_END);
        header.setPreferredSize(new Dimension(980, 110));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 18, 14, 18));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));

        JPanel iconBox = new RoundedPanel(18, new Color(255, 255, 255, 40));
        iconBox.setPreferredSize(new Dimension(52, 52));
        iconBox.setMaximumSize(new Dimension(52, 52));
        iconBox.setLayout(new GridBagLayout());

        JLabel icon = new JLabel("\uD83D\uDCC6"); // 📆
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        icon.setForeground(Color.WHITE);
        iconBox.add(icon);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBorder(new EmptyBorder(0, 12, 0, 0));

        JLabel t = new JLabel("Scheduled Meetings");
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel sub = new JLabel(companyName == null ? "All company meetings" : ("Company: " + companyName));
        sub.setForeground(new Color(226, 232, 240));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        titles.add(t);
        titles.add(Box.createVerticalStrut(6));
        titles.add(sub);

        left.add(iconBox);
        left.add(titles);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);

        JLabel fl = new JLabel("Status:");
        fl.setForeground(new Color(241, 245, 249));
        fl.setFont(new Font("Segoe UI", Font.BOLD, 12));

        styleCombo(statusFilter);

        JButton refreshBtn = new SoftButton("Refresh", new Color(255, 255, 255, 45), Color.WHITE);
        refreshBtn.addActionListener(e -> refresh());

        right.add(fl);
        right.add(statusFilter);
        right.add(refreshBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.62);
        split.setBorder(new EmptyBorder(14, 16, 10, 16));
        split.setContinuousLayout(true);

        // Left: table card
        RoundedPanel tableCard = new RoundedPanel(18, Color.WHITE);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 243), 1, true));
        tableCard.add(sp, BorderLayout.CENTER);

        // Right: details card
        RoundedPanel details = new RoundedPanel(18, Color.WHITE);
        details.setLayout(new BorderLayout());
        details.setBorder(new EmptyBorder(12, 12, 12, 12));

        details.add(buildDetailsHeader(), BorderLayout.NORTH);
        details.add(buildDetailsBody(), BorderLayout.CENTER);

        split.setLeftComponent(tableCard);
        split.setRightComponent(details);

        return split;
    }

    private JComponent buildDetailsHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel title = new JLabel("Meeting Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_DARK);

        p.add(title, BorderLayout.WEST);
        return p;
    }

    private JComponent buildDetailsBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        jobVal = valueLabel();
        studentVal = valueLabel();
        whenVal = valueLabel();
        modeVal = valueLabel();
        statusVal = valueLabel();
        locationVal = valueLabel();

        linkField = new JTextField();
        linkField.setEditable(false);
        linkField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        linkField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        notesArea = new JTextArea(8, 28);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setEditable(false);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notesArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 226, 235), 1, true));
        notesScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        int y = 0;
        addKV(body, gbc, y++, "Job", jobVal);
        addKV(body, gbc, y++, "Student", studentVal);
        addKV(body, gbc, y++, "Scheduled At", whenVal);
        addKV(body, gbc, y++, "Mode", modeVal);
        addKV(body, gbc, y++, "Status", statusVal);
        addKV(body, gbc, y++, "Office Location", locationVal);

        // Link row
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        body.add(keyLabel("Meeting Link"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        body.add(linkField, gbc);
        y++;

        // Notes row
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; gbc.weighty = 0;
        body.add(keyLabel("Notes"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        body.add(notesScroll, gbc);

        return body;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(245, 247, 251));
        footer.setBorder(new EmptyBorder(0, 16, 14, 16));

        // ✅ Cancel Meeting
        cancelBtn = new SoftButton("Cancel Meeting", new Color(254, 226, 226), new Color(153, 27, 27));
        cancelBtn.addActionListener(e -> cancelSelectedMeeting());

        // ✅ Mark Completed
        completeBtn = new SolidButton("Mark Completed", GRAD_START);
        completeBtn.addActionListener(e -> markSelectedCompleted());

        // Start meeting (only when scheduled + has link)
        startBtn = new SolidButton("Start Meeting", GRAD_START);
        startBtn.addActionListener(e -> startSelectedMeeting());

        JButton closeBtn = new SoftButton("Close", new Color(226, 232, 240), TEXT_DARK);
        closeBtn.addActionListener(e -> dispose());

        footer.add(cancelBtn);
        footer.add(completeBtn);
        footer.add(startBtn);
        footer.add(closeBtn);

        // initial state
        cancelBtn.setEnabled(false);
        completeBtn.setEnabled(false);
        startBtn.setEnabled(false);

        return footer;
    }

    private void refresh() {
        String status = String.valueOf(statusFilter.getSelectedItem());
        List<CompanyInterviewDao.MeetingRow> rows = dao.listMeetings(companyName, status);

        model.setRowCount(0);
        for (CompanyInterviewDao.MeetingRow r : rows) {
            model.addRow(new Object[]{
                    r.interviewId,
                    r.jobTitle,
                    r.studentName + (r.studentEmail == null ? "" : (" (" + r.studentEmail + ")")),
                    r.scheduledAt,
                    r.mode,
                    r.status
            });
        }

        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        } else {
            clearDetails();
        }
    }

    private void onRowSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = table.getSelectedRow();
        if (row < 0) { clearDetails(); return; }

        String statusFilterValue = String.valueOf(statusFilter.getSelectedItem());
        List<CompanyInterviewDao.MeetingRow> rows = dao.listMeetings(companyName, statusFilterValue);

        long interviewId = ((Number) model.getValueAt(row, 0)).longValue();
        CompanyInterviewDao.MeetingRow selected = null;
        for (CompanyInterviewDao.MeetingRow r : rows) {
            if (r.interviewId == interviewId) { selected = r; break; }
        }
        if (selected == null) { clearDetails(); return; }

        jobVal.setText(safe(selected.jobTitle));
        studentVal.setText(safe(selected.studentName) + (selected.studentEmail == null ? "" : (" • " + selected.studentEmail)));
        whenVal.setText(safe(selected.scheduledAt));
        modeVal.setText(safe(selected.mode));
        statusVal.setText(safe(selected.status));
        locationVal.setText(safe(selected.location));

        linkField.setText(selected.meetingLink == null ? "" : selected.meetingLink);
        notesArea.setText(selected.notes == null ? "" : selected.notes);

        boolean isScheduled = "SCHEDULED".equalsIgnoreCase(selected.status);

        // Start only when scheduled AND has link (online). Face-to-face won't have link.
        boolean hasLink = selected.meetingLink != null && !selected.meetingLink.trim().isEmpty();
        startBtn.setEnabled(isScheduled && hasLink);

        // Only allow changing status when scheduled
        completeBtn.setEnabled(isScheduled);
        cancelBtn.setEnabled(isScheduled);
    }

    private void clearDetails() {
        jobVal.setText("—");
        studentVal.setText("—");
        whenVal.setText("—");
        modeVal.setText("—");
        statusVal.setText("—");
        locationVal.setText("—");
        linkField.setText("");
        notesArea.setText("");

        startBtn.setEnabled(false);
        completeBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
    }

    private Long selectedInterviewId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object v = model.getValueAt(row, 0);
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception ex) { return null; }
    }

    private void markSelectedCompleted() {
        Long id = selectedInterviewId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a meeting first.", "Mark Completed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String currentStatus = safe(statusVal.getText());
        if ("COMPLETED".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This meeting is already completed.", "Mark Completed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!"SCHEDULED".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Only SCHEDULED meetings can be completed.", "Mark Completed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Mark this meeting as COMPLETED?",
                "Confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            boolean updated = dao.updateMeetingStatus(id, "COMPLETED");
            if (!updated) throw new RuntimeException("No rows updated.");

            JOptionPane.showMessageDialog(this, "Meeting marked as completed.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to mark completed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ✅ NEW
    private void cancelSelectedMeeting() {
        Long id = selectedInterviewId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a meeting first.", "Cancel Meeting", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String currentStatus = safe(statusVal.getText());
        if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This meeting is already cancelled.", "Cancel Meeting", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if ("COMPLETED".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Completed meetings cannot be cancelled.", "Cancel Meeting", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!"SCHEDULED".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Only SCHEDULED meetings can be cancelled.", "Cancel Meeting", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Cancel this meeting? (Status will become CANCELLED)",
                "Confirm Cancel",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            boolean updated = dao.updateMeetingStatus(id, "CANCELLED");
            if (!updated) throw new RuntimeException("No rows updated.");

            JOptionPane.showMessageDialog(this, "Meeting cancelled.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to cancel meeting: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String selectedMeetingLink() {
        String link = linkField.getText();
        if (link == null) return null;
        link = link.trim();
        return link.isEmpty() ? null : link;
    }

    private void startSelectedMeeting() {
        String link = selectedMeetingLink();
        if (link == null) {
            JOptionPane.showMessageDialog(this,
                    "This meeting does not have a link (Face-to-face interviews use Office Location).",
                    "Start Meeting",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Block starting if not scheduled (extra safety)
        String st = safe(statusVal.getText());
        if (!"SCHEDULED".equalsIgnoreCase(st)) {
            JOptionPane.showMessageDialog(this,
                    "Only SCHEDULED meetings can be started.",
                    "Start Meeting",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(this, "Desktop browsing is not supported on this system.",
                        "Start Meeting", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Desktop.getDesktop().browse(new URI(link));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to open meeting link: " + ex.getMessage(),
                    "Start Meeting", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JLabel keyLabel(String s) {
        JLabel l = new JLabel(s);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private static JLabel valueLabel() {
        JLabel v = new JLabel("—");
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(TEXT_DARK);
        return v;
    }

    private static void addKV(JPanel body, GridBagConstraints gbc, int y, String key, JComponent val) {
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        body.add(keyLabel(key), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        body.add(val, gbc);
    }

    private static void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 60), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        combo.setBackground(Color.WHITE);
    }

    private static String safe(String s) {
        if (s == null) return "—";
        String t = s.trim();
        return t.isEmpty() ? "—" : t;
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

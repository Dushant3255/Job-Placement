package com.placement.company.ui;

import com.placement.common.ui.LoginScreen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Company dashboard UI (dummy data).
 * - Header gradient matches LoginScreen.
 * - Stats cards (dummy numbers).
 * - Job listings (dummy cards) with sensible actions.
 */
public class CompanyDashboardScreen extends JFrame {

    // Match LoginScreen header gradient
    private static final Color GRAD_START = new Color(220, 38, 38);
    private static final Color GRAD_END   = new Color(244, 63, 94);

    private static final Color BG = new Color(245, 247, 251);
    private static final Color TEXT_MUTED = new Color(90, 98, 112);

    private final String companyName;

    public CompanyDashboardScreen() {
        this("Tech Innovations Inc.");
    }

    public CompanyDashboardScreen(String companyName) {
        this.companyName = (companyName == null || companyName.isBlank()) ? "Company" : companyName;
        setTitle("Company Dashboard - Student Placement Portal");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 650));
        initUI();
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

        // Left: icon + title/subtitle
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));

        JPanel iconBox = new RoundedPanel(18, new Color(255, 255, 255, 30));
        iconBox.setPreferredSize(new Dimension(56, 56));
        iconBox.setMaximumSize(new Dimension(56, 56));
        iconBox.setLayout(new GridBagLayout());

        JLabel icon = new JLabel("\uD83C\uDFE2"); // building icon
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        icon.setForeground(Color.WHITE);
        iconBox.add(icon);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBorder(new EmptyBorder(0, 14, 0, 0));

        JLabel title = new JLabel("Company Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Recruitment Management Portal");
        subtitle.setForeground(new Color(235, 235, 255));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        titles.add(title);
        titles.add(Box.createVerticalStrut(6));
        titles.add(subtitle);

        left.add(iconBox);
        left.add(titles);

        // Right: Logout button
        JButton logout = new OutlineButton("Logout");
        logout.setForeground(Color.WHITE);
        logout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(
                    this,
                    "Log out now?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );
            if (ok == JOptionPane.YES_OPTION) {
                new LoginScreen().setVisible(true);
                dispose();
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
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

        content.add(buildWelcome());
        content.add(Box.createVerticalStrut(16));
        content.add(buildStats());
        content.add(Box.createVerticalStrut(18));
        content.add(buildJobsHeader());
        content.add(Box.createVerticalStrut(10));
        content.add(buildJobsList());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(BG);
        return scroll;
    }

    private JComponent buildWelcome() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel welcome = new JLabel("Welcome to " + companyName + ".");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        welcome.setForeground(new Color(25, 30, 45));

        JLabel hint = new JLabel("Manage your job postings, review applications, and make hiring decisions all in one place.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(TEXT_MUTED);
        hint.setBorder(new EmptyBorder(6, 0, 0, 0));

        panel.add(welcome);
        panel.add(hint);
        return wrapAsCard(panel);
    }

    private JComponent buildStats() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 14));
        grid.setOpaque(false);

        grid.add(new StatCard("Active Jobs", "8", new Color(239, 246, 255)));
        grid.add(new StatCard("Total Applicants", "124", new Color(236, 253, 245)));
        grid.add(new StatCard("Offers Made", "15", new Color(245, 243, 255)));
        grid.add(new StatCard("Pending Reviews", "32", new Color(255, 247, 237)));
        
        // ✅ make the entire row taller
        grid.setPreferredSize(new Dimension(0, 105));
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        
        return grid;
    }

    private JComponent buildJobsHeader() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel title = new JLabel("Job Listings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(25, 30, 45));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton refresh = new SoftButton("Refresh", new Color(226, 232, 240), new Color(15, 23, 42));
        refresh.addActionListener(e -> JOptionPane.showMessageDialog(this, "(Dummy) Refresh jobs"));

        JButton post = new SolidButton("+ Post New Job", GRAD_START);
        post.addActionListener(e -> JOptionPane.showMessageDialog(this, "(Dummy) Open Post Job form"));

        actions.add(refresh);
        actions.add(post);

        row.add(title, BorderLayout.WEST);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private JComponent buildJobsList() {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        List<JobRow> jobs = dummyJobs();
        for (int i = 0; i < jobs.size(); i++) {
            list.add(new JobCard(jobs.get(i)));
            if (i < jobs.size() - 1) list.add(Box.createVerticalStrut(12));
        }

        return list;
    }

    private List<JobRow> dummyJobs() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        List<JobRow> out = new ArrayList<>();
        out.add(new JobRow("Software Engineer (Java)", "Engineering", "OPEN", LocalDate.now().minusDays(2).format(fmt),
                "Build and maintain backend services. Swing UI integration a plus."));
        out.add(new JobRow("UI/UX Intern", "Design", "OPEN", LocalDate.now().minusDays(5).format(fmt),
                "Assist in improving the portal UI and user flows."));
        out.add(new JobRow("Data Analyst", "Analytics", "CLOSED", LocalDate.now().minusDays(14).format(fmt),
                "Analyze placement trends and generate weekly reports."));
        out.add(new JobRow("DevOps Engineer", "Infrastructure", "OPEN", LocalDate.now().minusDays(21).format(fmt),
                "CI/CD pipelines, monitoring, and release automation."));
        return out;
    }

    private JComponent wrapAsCard(JComponent inner) {
        RoundedPanel card = new RoundedPanel(18, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.add(inner, BorderLayout.CENTER);
        card.setBorder(new EmptyBorder(0, 0, 0, 0));
        return card;
    }

    /* ----------------------------- Components ----------------------------- */

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

    private static class StatCard extends RoundedPanel {
        StatCard(String label, String value, Color bg) {
            super(18, bg);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(18, 16, 18, 16));

            JLabel top = new JLabel(label);
            top.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            top.setForeground(new Color(55, 65, 81));

            JLabel big = new JLabel(value);
            big.setFont(new Font("Segoe UI", Font.BOLD, 28));
            big.setForeground(new Color(15, 23, 42));

            add(top, BorderLayout.NORTH);
            add(big, BorderLayout.WEST);
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
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 110), 1, true),
                    BorderFactory.createEmptyBorder(10, 16, 10, 16)
            ));
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 1, true),
                            BorderFactory.createEmptyBorder(10, 16, 10, 16)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 255, 255, 110), 1, true),
                            BorderFactory.createEmptyBorder(10, 16, 10, 16)
                    ));
                }
            });
        }
    }

    private static class JobRow {
        final String title;
        final String dept;
        final String status;
        final String posted;
        final String summary;

        JobRow(String title, String dept, String status, String posted, String summary) {
            this.title = title;
            this.dept = dept;
            this.status = status;
            this.posted = posted;
            this.summary = summary;
        }
    }

    private class JobCard extends RoundedPanel {
        JobCard(JobRow job) {
            super(18, Color.WHITE);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(14, 16, 14, 16));

            // ---- Left (info) ----
            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            // Title row: title + status pill
            JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            titleRow.setOpaque(false);

            JLabel title = new JLabel(job.title);
            title.setFont(new Font("Segoe UI", Font.BOLD, 14));
            title.setForeground(new Color(15, 23, 42));

            JLabel status = buildStatusPill(job.status);

            titleRow.add(title);
            titleRow.add(status);

            JLabel meta = new JLabel(job.dept + "  |  Posted: " + job.posted);
            meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            meta.setForeground(TEXT_MUTED);
            meta.setBorder(new EmptyBorder(6, 0, 0, 0));

            // wrapped summary using HTML (cleaner than JTextArea)
            JLabel summary = new JLabel("<html><div style='width:520px;'>" + escapeHtml(job.summary) + "</div></html>");
            summary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            summary.setForeground(new Color(55, 65, 81));
            summary.setBorder(new EmptyBorder(8, 0, 0, 0));

            info.add(titleRow);
            info.add(meta);
            info.add(summary);

            // ---- Right (buttons) ----
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            actions.setOpaque(false);

            JButton applicants = new SoftButton("View Applicants", new Color(226, 232, 240), new Color(15, 23, 42));
            applicants.setPreferredSize(new Dimension(140, 38));

            JButton edit = new SoftButton("Edit", new Color(237, 233, 254), new Color(79, 70, 229));
            edit.setPreferredSize(new Dimension(70, 38));

            JButton toggle = new SolidButton(
                    job.status.equalsIgnoreCase("OPEN") ? "Close" : "Reopen",
                    job.status.equalsIgnoreCase("OPEN") ? new Color(234, 88, 12) : GRAD_START
            );
            toggle.setPreferredSize(new Dimension(85, 38));

            applicants.addActionListener(e -> JOptionPane.showMessageDialog(
                    CompanyDashboardScreen.this, "(Dummy) Applicants for: " + job.title));

            edit.addActionListener(e -> JOptionPane.showMessageDialog(
                    CompanyDashboardScreen.this, "(Dummy) Edit job: " + job.title));

            toggle.addActionListener(e -> JOptionPane.showMessageDialog(
                    CompanyDashboardScreen.this, "(Dummy) Toggle status for: " + job.title));

            actions.add(applicants);
            actions.add(edit);
            actions.add(toggle);

            // Put info + actions on one row, top-aligned
            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setOpaque(false);
            row.add(info, BorderLayout.CENTER);
            row.add(actions, BorderLayout.EAST);

            add(row, BorderLayout.CENTER);
        }

        private JLabel buildStatusPill(String status) {
            boolean open = status != null && status.equalsIgnoreCase("OPEN");
            Color bg = open ? new Color(220, 252, 231) : new Color(254, 226, 226);
            Color fg = open ? new Color(22, 101, 52) : new Color(153, 27, 27);

            JLabel pill = new JLabel(open ? "OPEN" : "CLOSED");
            pill.setOpaque(true);
            pill.setBackground(bg);
            pill.setForeground(fg);
            pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
            pill.setBorder(new EmptyBorder(4, 10, 4, 10));
            return pill;
        }
    }

    // helper to avoid html breaking
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // Quick local test
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompanyDashboardScreen("Tech Innovations Inc.").setVisible(true));
    }
}

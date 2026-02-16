package com.placement.company.ui;

import com.placement.common.ui.LoginScreen;
import com.placement.company.dao.CompanyJobDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CompanyDashboardScreen extends JFrame {

    private static final Color GRAD_START = new Color(220, 38, 38);
    private static final Color GRAD_END   = new Color(244, 63, 94);

    private static final Color BG = new Color(245, 247, 251);
    private static final Color TEXT_MUTED = new Color(90, 98, 112);

    private final String companyName;
    private final CompanyJobDao jobDao = new CompanyJobDao();

    private StatCard activeJobsCard;
    private StatCard totalApplicantsCard;
    private StatCard offersMadeCard;
    private StatCard pendingReviewsCard;

    private JButton refreshBtn;

    private final List<CompanyJobDao.JobRow> allJobs = new ArrayList<>();
    private final List<CompanyJobDao.JobRow> filteredJobs = new ArrayList<>();

    private JTextField jobSearchField;
    private JComboBox<String> statusFilter;

    private CardLayout jobsViewLayout;
    private JPanel jobsViewPanel;

    private JPanel jobsCardsList;

    private JTable jobsTable;
    private DefaultTableModel jobsTableModel;

    private JButton tableViewApplicantsBtn;
    private JButton tableEditBtn;
    private JButton tableToggleBtn;

    private static final String VIEW_CARDS = "CARDS";
    private static final String VIEW_TABLE = "TABLE";

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

        // ✅ Keep dashboard maximized (helps when returning from Applicants too)
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

        JLabel title = new JLabel("Company Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Recruitment Management Portal");
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
        content.add(buildJobsSection());

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

        activeJobsCard = new StatCard("Active Jobs", "—", new Color(239, 246, 255));
        totalApplicantsCard = new StatCard("Total Applicants", "—", new Color(236, 253, 245));
        offersMadeCard = new StatCard("Offers Made", "—", new Color(245, 243, 255));
        pendingReviewsCard = new StatCard("Pending Reviews", "—", new Color(255, 247, 237));

        grid.add(activeJobsCard);
        grid.add(totalApplicantsCard);
        grid.add(offersMadeCard);
        grid.add(pendingReviewsCard);

        grid.setPreferredSize(new Dimension(0, 112));
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        return grid;
    }

    private JComponent buildJobsSection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Job Listings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(25, 30, 45));
        top.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        jobSearchField = new JTextField(18);
        jobSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jobSearchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        jobSearchField.setToolTipText("Search title / dept / description");

        statusFilter = new JComboBox<>(new String[]{"All", "OPEN", "CLOSED"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        RoundedToggleButton cardsBtn = new RoundedToggleButton("Cards");
        RoundedToggleButton tableBtn = new RoundedToggleButton("Table");
        ButtonGroup viewGroup = new ButtonGroup();
        viewGroup.add(cardsBtn);
        viewGroup.add(tableBtn);
        cardsBtn.setSelected(true);
        updateToggleStyles(cardsBtn, tableBtn);

        cardsBtn.addActionListener(e -> {
            updateToggleStyles(cardsBtn, tableBtn);
            jobsViewLayout.show(jobsViewPanel, VIEW_CARDS);
        });

        tableBtn.addActionListener(e -> {
            updateToggleStyles(cardsBtn, tableBtn);
            jobsViewLayout.show(jobsViewPanel, VIEW_TABLE);
        });

        refreshBtn = new SoftButton("Refresh", new Color(226, 232, 240), new Color(15, 23, 42));
        refreshBtn.addActionListener(e -> loadFromDb());

        JButton post = new SolidButton("+ Post New Job", GRAD_START);
        post.addActionListener(e -> {
            PostJobDialog dlg = new PostJobDialog(this, companyName, jobDao, this::loadFromDb);
            dlg.setVisible(true);
        });

        controls.add(jobSearchField);
        controls.add(statusFilter);
        controls.add(cardsBtn);
        controls.add(tableBtn);
        controls.add(refreshBtn);
        controls.add(post);

        top.add(controls, BorderLayout.EAST);

        section.add(top);
        section.add(Box.createVerticalStrut(10));

        jobsViewLayout = new CardLayout();
        jobsViewPanel = new JPanel(jobsViewLayout);
        jobsViewPanel.setOpaque(false);

        jobsViewPanel.add(buildCardsView(), VIEW_CARDS);
        jobsViewPanel.add(buildTableView(), VIEW_TABLE);

        section.add(jobsViewPanel);

        loadFromDb();

        jobSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyJobFiltersAndRefreshUI(); }
            @Override public void removeUpdate(DocumentEvent e) { applyJobFiltersAndRefreshUI(); }
            @Override public void changedUpdate(DocumentEvent e) { applyJobFiltersAndRefreshUI(); }
        });
        statusFilter.addActionListener(e -> applyJobFiltersAndRefreshUI());

        return section;
    }

    private void openEditJob(CompanyJobDao.JobRow job) {
        if (job == null) return;
        EditJobDialog dlg = new EditJobDialog(this, companyName, jobDao, job, this::loadFromDb);
        dlg.setVisible(true);
    }

    private void updateToggleStyles(RoundedToggleButton cardsBtn, RoundedToggleButton tableBtn) {
        if (cardsBtn.isSelected()) {
            cardsBtn.setBg(GRAD_START, Color.WHITE);
            tableBtn.setBg(Color.WHITE, new Color(15, 23, 42));
        } else {
            tableBtn.setBg(GRAD_START, Color.WHITE);
            cardsBtn.setBg(Color.WHITE, new Color(15, 23, 42));
        }
    }

    private JComponent buildCardsView() {
        jobsCardsList = new JPanel();
        jobsCardsList.setOpaque(false);
        jobsCardsList.setLayout(new BoxLayout(jobsCardsList, BoxLayout.Y_AXIS));
        return jobsCardsList;
    }

    private JComponent buildTableView() {
        jobsTableModel = new DefaultTableModel(new Object[]{"Title", "Department", "Posted", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        jobsTable = new JTable(jobsTableModel);
        jobsTable.setRowHeight(34);
        jobsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jobsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        jobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sp = new JScrollPane(jobsTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 243), 1, true));
        sp.setPreferredSize(new Dimension(0, 280));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setOpaque(false);

        tableViewApplicantsBtn = new SoftButton("View Applicants", new Color(226, 232, 240), new Color(15, 23, 42));
        tableEditBtn = new SoftButton("Edit", new Color(237, 233, 254), new Color(79, 70, 229));
        tableToggleBtn = new SolidButton("Close", new Color(234, 88, 12));

        tableViewApplicantsBtn.setEnabled(false);
        tableEditBtn.setEnabled(false);
        tableToggleBtn.setEnabled(false);

        tableViewApplicantsBtn.addActionListener(e -> openApplicants(selectedTableJob()));
        tableEditBtn.addActionListener(e -> openEditJob(selectedTableJob()));

        tableToggleBtn.addActionListener(e -> {
            CompanyJobDao.JobRow job = selectedTableJob();
            if (job == null) return;

            String cur = (job.status == null) ? "OPEN" : job.status;
            String newStatus = cur.equalsIgnoreCase("OPEN") ? "CLOSED" : "OPEN";

            boolean ok = jobDao.updateStatus(job.jobId, newStatus);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Failed to update job status.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadFromDb();
        });

        actions.add(tableViewApplicantsBtn);
        actions.add(tableEditBtn);
        actions.add(tableToggleBtn);

        jobsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                updateTableActionButtons();
            }
        });

        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.add(sp);
        wrap.add(actions);

        return wrap;
    }

    private void loadFromDb() {
        if (refreshBtn != null) refreshBtn.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<LoadedData, Void>() {
            @Override protected LoadedData doInBackground() {
                LoadedData d = new LoadedData();
                d.jobs = jobDao.listByCompanyName(companyName);
                d.activeJobs = jobDao.countActiveJobs(companyName);
                d.totalApplicants = jobDao.countTotalApplicants(companyName);
                d.offersMade = jobDao.countOffersMade(companyName);
                d.pendingReviews = jobDao.countPendingReviews(companyName);
                return d;
            }

            @Override protected void done() {
                try {
                    LoadedData d = get();

                    allJobs.clear();
                    if (d.jobs != null) allJobs.addAll(d.jobs);

                    activeJobsCard.setValue(String.valueOf(d.activeJobs));
                    totalApplicantsCard.setValue(String.valueOf(d.totalApplicants));
                    offersMadeCard.setValue(String.valueOf(d.offersMade));
                    pendingReviewsCard.setValue(String.valueOf(d.pendingReviews));

                    applyJobFiltersAndRefreshUI();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            CompanyDashboardScreen.this,
                            "Failed to load from DB: " + (ex.getMessage() == null ? "" : ex.getMessage()),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    if (refreshBtn != null) refreshBtn.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }

    private static class LoadedData {
        List<CompanyJobDao.JobRow> jobs;
        int activeJobs;
        int totalApplicants;
        int offersMade;
        int pendingReviews;
    }

    private void applyJobFiltersAndRefreshUI() {
        String q = (jobSearchField == null) ? "" : jobSearchField.getText().trim().toLowerCase();
        String status = (statusFilter == null) ? "All" : String.valueOf(statusFilter.getSelectedItem());

        filteredJobs.clear();

        for (CompanyJobDao.JobRow j : allJobs) {
            boolean statusOk = status.equals("All") || (j.status != null && j.status.equalsIgnoreCase(status));
            if (!statusOk) continue;

            if (q.isEmpty()) {
                filteredJobs.add(j);
                continue;
            }

            String hay =
                    (j.title == null ? "" : j.title.toLowerCase()) + " " +
                    (j.department == null ? "" : j.department.toLowerCase()) + " " +
                    (j.description == null ? "" : j.description.toLowerCase());

            if (hay.contains(q)) filteredJobs.add(j);
        }

        rebuildCardsUI();
        rebuildTableUI();
        updateTableActionButtons();
    }

    private void rebuildCardsUI() {
        if (jobsCardsList == null) return;

        jobsCardsList.removeAll();

        if (filteredJobs.isEmpty()) {
            JLabel empty = new JLabel("No jobs found.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            empty.setForeground(TEXT_MUTED);
            empty.setBorder(new EmptyBorder(12, 4, 12, 4));
            jobsCardsList.add(empty);
        } else {
            for (int i = 0; i < filteredJobs.size(); i++) {
                jobsCardsList.add(new JobCard(filteredJobs.get(i)));
                if (i < filteredJobs.size() - 1) jobsCardsList.add(Box.createVerticalStrut(12));
            }
        }

        jobsCardsList.revalidate();
        jobsCardsList.repaint();
    }

    private void rebuildTableUI() {
        if (jobsTableModel == null) return;

        jobsTableModel.setRowCount(0);
        for (CompanyJobDao.JobRow j : filteredJobs) {
            jobsTableModel.addRow(new Object[]{
                    j.title,
                    j.department,
                    formatPosted(j.postedAt),
                    j.status
            });
        }
    }

    private CompanyJobDao.JobRow selectedTableJob() {
        if (jobsTable == null) return null;
        int row = jobsTable.getSelectedRow();
        if (row < 0 || row >= filteredJobs.size()) return null;
        return filteredJobs.get(row);
    }

    private void updateTableActionButtons() {
        CompanyJobDao.JobRow job = selectedTableJob();
        boolean has = job != null;

        if (tableViewApplicantsBtn != null) tableViewApplicantsBtn.setEnabled(has);
        if (tableEditBtn != null) tableEditBtn.setEnabled(has);
        if (tableToggleBtn != null) tableToggleBtn.setEnabled(has);

        if (!has || tableToggleBtn == null) return;

        boolean open = job.status != null && job.status.equalsIgnoreCase("OPEN");
        tableToggleBtn.setText(open ? "Close" : "Reopen");
        tableToggleBtn.setBackground(open ? new Color(234, 88, 12) : GRAD_START);
        tableToggleBtn.repaint();
    }

    private static String formatPosted(String raw) {
        if (raw == null || raw.isBlank()) return "";
        try {
            String datePart = raw.length() >= 10 ? raw.substring(0, 10) : raw;
            LocalDate d = LocalDate.parse(datePart);
            return d.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        } catch (Exception e) {
            return raw;
        }
    }

    private JComponent wrapAsCard(JComponent inner) {
        RoundedPanel card = new RoundedPanel(18, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.add(inner, BorderLayout.CENTER);
        card.setBorder(new EmptyBorder(0, 0, 0, 0));
        return card;
    }

    // ✅ NEW NAV: hide dashboard (not dispose), open applicants with parent reference
    private void openApplicants(CompanyJobDao.JobRow job) {
        if (job == null) return;

        this.setVisible(false);

        ApplicantsScreen screen = new ApplicantsScreen(this, companyName, job.jobId, job.title);
        screen.setVisible(true);
    }

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

    private static class StatCard extends RoundedPanel {
        private final JLabel big;

        StatCard(String label, String value, Color bg) {
            super(18, bg);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(18, 16, 18, 16));

            JLabel top = new JLabel(label);
            top.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            top.setForeground(new Color(55, 65, 81));

            big = new JLabel(value);
            big.setFont(new Font("Segoe UI", Font.BOLD, 30));
            big.setForeground(new Color(15, 23, 42));

            add(top, BorderLayout.NORTH);
            add(big, BorderLayout.WEST);
        }

        void setValue(String v) {
            big.setText(v == null ? "—" : v);
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

    private static class RoundedToggleButton extends JToggleButton {
        private final int radius = 18;
        private Color bg = Color.WHITE;
        private Color fg = new Color(15, 23, 42);

        RoundedToggleButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setOpaque(false);
            setBg(Color.WHITE, fg);
        }

        void setBg(Color bg, Color fg) {
            this.bg = bg;
            this.fg = fg;
            setForeground(fg);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            if (!isSelected()) {
                g2.setColor(new Color(220, 226, 235));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }

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

    private class JobCard extends RoundedPanel {
        JobCard(CompanyJobDao.JobRow job) {
            super(18, Color.WHITE);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(14, 16, 14, 16));

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            titleRow.setOpaque(false);

            JLabel title = new JLabel(job.title);
            title.setFont(new Font("Segoe UI", Font.BOLD, 14));
            title.setForeground(new Color(15, 23, 42));

            JLabel status = buildStatusPill(job.status);
            titleRow.add(title);
            titleRow.add(status);

            String deptText = (job.department == null || job.department.isBlank()) ? "—" : job.department;
            JLabel meta = new JLabel(deptText + "  |  Posted: " + formatPosted(job.postedAt));
            meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            meta.setForeground(TEXT_MUTED);
            meta.setBorder(new EmptyBorder(6, 0, 0, 0));

            JLabel summary = new JLabel("<html><div style='width:520px;'>" + escapeHtml(job.description) + "</div></html>");
            summary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            summary.setForeground(new Color(55, 65, 81));
            summary.setBorder(new EmptyBorder(8, 0, 0, 0));

            info.add(titleRow);
            info.add(meta);
            info.add(summary);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            actions.setOpaque(false);

            JButton applicants = new SoftButton("View Applicants", new Color(226, 232, 240), new Color(15, 23, 42));
            applicants.setPreferredSize(new Dimension(140, 38));

            JButton edit = new SoftButton("Edit", new Color(237, 233, 254), new Color(79, 70, 229));
            edit.setPreferredSize(new Dimension(70, 38));

            boolean open = job.status != null && job.status.equalsIgnoreCase("OPEN");
            JButton toggle = new SolidButton(open ? "Close" : "Reopen", open ? new Color(234, 88, 12) : GRAD_START);
            toggle.setPreferredSize(new Dimension(90, 38));

            applicants.addActionListener(e -> openApplicants(job));
            edit.addActionListener(e -> openEditJob(job));

            toggle.addActionListener(e -> {
                String cur = (job.status == null) ? "OPEN" : job.status;
                String newStatus = cur.equalsIgnoreCase("OPEN") ? "CLOSED" : "OPEN";
                boolean ok = jobDao.updateStatus(job.jobId, newStatus);
                if (!ok) {
                    JOptionPane.showMessageDialog(CompanyDashboardScreen.this, "Failed to update job status.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                loadFromDb();
            });

            actions.add(applicants);
            actions.add(edit);
            actions.add(toggle);

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

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompanyDashboardScreen("Tech Innovations Inc.").setVisible(true));
    }
}

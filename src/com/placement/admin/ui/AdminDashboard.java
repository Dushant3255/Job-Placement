package com.placement.admin.ui;

import com.placement.common.model.User;
import com.placement.common.model.UserRole;
import com.placement.common.ui.LoginScreen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Admin dashboard shell.
 *
 * Integrated from the standalone admin module so the Admin panel appears
 * in the main Job-Placement project and uses the shared SQLite database.
 */
public class AdminDashboard extends JFrame {

    private final User adminUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public AdminDashboard(User adminUser) {
        if (adminUser == null || adminUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("AdminDashboard requires an ADMIN user");
        }
        this.adminUser = adminUser;

        setTitle("Admin Dashboard - Placement System");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        // Menu bar removed for a cleaner dashboard UI
        setJMenuBar(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AdminTheme.BG);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createSidebar(), BorderLayout.WEST);

        contentPanel.setBackground(AdminTheme.BG);
        contentPanel.add(createWelcomePanel(), "WELCOME");
        contentPanel.add(new ManageStudentsPanel(), "STUDENTS");
        contentPanel.add(new ManageCompaniesPanel(), "COMPANIES");
        contentPanel.add(new ManageJobPostingsPanel(), "JOBS");
        contentPanel.add(new ManageApplicationsPanel(), "APPLICATIONS");

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 25, 25));
        header.setBorder(new EmptyBorder(14, 18, 14, 18));
        header.setBorder(BorderFactory.createMatteBorder(
                0, 0, 2, 0, new Color(90, 90, 90)));
        header.setPreferredSize(new Dimension(0, 70));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false); // IMPORTANT
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));

        leftPanel.add(titleLabel);
JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));


        header.add(leftPanel, BorderLayout.WEST);
    
        return header;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(AdminTheme.SURFACE);
        menuBar.setForeground(AdminTheme.TEXT);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(AdminTheme.TEXT);
        JMenuItem profileItem = new JMenuItem("Profile");
        profileItem.addActionListener(e -> showProfileDialog());

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());

        fileMenu.add(profileItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(AdminTheme.TEXT);
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Student Placement System\nAdmin Module",
                "About",
                JOptionPane.INFORMATION_MESSAGE
        ));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AdminTheme.SURFACE);
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Top info block (separated visually from the navigation)
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(AdminTheme.SURFACE);
        info.setBorder(new EmptyBorder(18, 10, 14, 10));

        JLabel adminLabel = new JLabel("Admin Panel");
        adminLabel.setFont(AdminTheme.fontBold(16));
        adminLabel.setForeground(AdminTheme.TEXT);
        adminLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(adminUser.getUsername());
        nameLabel.setFont(AdminTheme.fontPlain(12));
        nameLabel.setForeground(AdminTheme.TEXT_MUTED);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        info.add(adminLabel);
        info.add(Box.createVerticalStrut(6));
        info.add(nameLabel);

        sidebar.add(info);

        JSeparator sep = new JSeparator();
        sep.setForeground(AdminTheme.BORDER);
        sep.setBackground(AdminTheme.BORDER);
        sidebar.add(sep);

        sidebar.add(Box.createVerticalStrut(18));
addMenuButton(sidebar, "Dashboard", "WELCOME");
        addMenuButton(sidebar, "Manage Students", "STUDENTS");
        addMenuButton(sidebar, "Manage Companies", "COMPANIES");
        addMenuButton(sidebar, "Manage Job Postings", "JOBS");
        addMenuButton(sidebar, "Manage Applications", "APPLICATIONS");

        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = new JButton("Logout");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(170, 40));
        AdminTheme.styleButton(logoutButton, new Color(220, 38, 38));
        logoutButton.addActionListener(e -> logout());
        sidebar.add(logoutButton);

        sidebar.add(Box.createVerticalStrut(20));
        return sidebar;
    }

    private void addMenuButton(JPanel sidebar, String label, String panelName) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setFont(AdminTheme.fontPlain(12));
        button.setBackground(AdminTheme.SURFACE_2);
        button.setForeground(AdminTheme.TEXT);
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
        button.addActionListener(e -> cardLayout.show(contentPanel, panelName));
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(10));
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AdminTheme.BG);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome, " + adminUser.getUsername() + "");
        title.setFont(AdminTheme.fontBold(22));
        title.setForeground(AdminTheme.TEXT);

        JLabel subtitle = new JLabel("Use the sections below to manage the placement system.");
        subtitle.setFont(AdminTheme.fontPlain(13));
        subtitle.setForeground(AdminTheme.TEXT_MUTED);

        top.add(title);
        top.add(Box.createVerticalStrut(6));
        top.add(subtitle);

        panel.add(top, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(16, 0, 0, 0));

        grid.add(sectionCard(
                "Students",
                "Search and filter students by program and year.\nView student academic details and metrics.",
                "Open Students",
                "STUDENTS"
        ));

        grid.add(sectionCard(
                "Companies",
                "Search companies. Add a company account and email credentials.\nDelete a company and send confirmation email.",
                "Open Companies",
                "COMPANIES"
        ));

        grid.add(sectionCard(
                "Job Postings",
                "Search job listings. Update OPEN/CLOSED status.\nAdd off-campus jobs.",
                "Open Job Postings",
                "JOBS"
        ));

        grid.add(sectionCard(
                "Applications",
                "Search applications. Confirm final status.\nUpload offer letters to appear in the student offers page.",
                "Open Applications",
                "APPLICATIONS"
        ));

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel sectionCard(String title, String desc, String btnText, String panelName) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AdminTheme.SURFACE);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel t = new JLabel(title);
        t.setFont(AdminTheme.fontBold(16));
        t.setForeground(AdminTheme.TEXT);

        JTextArea d = new JTextArea(desc);
        d.setEditable(false);
        d.setFocusable(false);
        d.setOpaque(false);
        d.setLineWrap(true);
        d.setWrapStyleWord(true);
        d.setFont(AdminTheme.fontPlain(12));
        d.setForeground(AdminTheme.TEXT_MUTED);

        JButton open = new JButton(btnText);
        AdminTheme.styleButton(open, AdminTheme.ACCENT);
        open.addActionListener(e -> cardLayout.show(contentPanel, panelName));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(open);

        card.add(t, BorderLayout.NORTH);
        card.add(d, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private void showProfileDialog() {
        JDialog dialog = new JDialog(this, "Admin Profile", true);
        dialog.setSize(420, 220);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(AdminTheme.BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel u = new JLabel("Username:");
        AdminTheme.styleLabel(u);
        panel.add(u, gbc);
        gbc.gridx = 1;
        JLabel uv = new JLabel(adminUser.getUsername());
        AdminTheme.styleLabel(uv);
        panel.add(uv, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel e = new JLabel("Email:");
        AdminTheme.styleLabel(e);
        panel.add(e, gbc);
        gbc.gridx = 1;
        JLabel ev = new JLabel(adminUser.getEmail());
        AdminTheme.styleLabel(ev);
        panel.add(ev, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginScreen().setVisible(true);
        }
    }
}

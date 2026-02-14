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

        setJMenuBar(createMenuBar());

        JPanel root = new JPanel(new BorderLayout());
        root.add(createSidebar(), BorderLayout.WEST);

        contentPanel.setBackground(new Color(245, 245, 245));
        contentPanel.add(createWelcomePanel(), "WELCOME");
        contentPanel.add(new ManageStudentsPanel(), "STUDENTS");
        contentPanel.add(new ManageCompaniesPanel(), "COMPANIES");
        contentPanel.add(new ManageJobPostingsPanel(), "JOBS");
        contentPanel.add(new ManageApplicationsPanel(), "APPLICATIONS");

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem profileItem = new JMenuItem("Profile");
        profileItem.addActionListener(e -> showProfileDialog());

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());

        fileMenu.add(profileItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);

        JMenu helpMenu = new JMenu("Help");
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
        sidebar.setBackground(new Color(40, 60, 80));
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel adminLabel = new JLabel("Admin Panel");
        adminLabel.setFont(new Font("Arial", Font.BOLD, 16));
        adminLabel.setForeground(Color.WHITE);
        adminLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(adminLabel);

        JLabel nameLabel = new JLabel(adminUser.getUsername());
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        nameLabel.setForeground(new Color(200, 200, 200));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(nameLabel);

        sidebar.add(Box.createVerticalStrut(30));

        addMenuButton(sidebar, "Dashboard", "WELCOME");
        addMenuButton(sidebar, "Manage Students", "STUDENTS");
        addMenuButton(sidebar, "Manage Companies", "COMPANIES");
        addMenuButton(sidebar, "Manage Job Postings", "JOBS");
        addMenuButton(sidebar, "Manage Applications", "APPLICATIONS");

        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = new JButton("Logout");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(170, 40));
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> logout());
        sidebar.add(logoutButton);

        sidebar.add(Box.createVerticalStrut(20));
        return sidebar;
    }

    private void addMenuButton(JPanel sidebar, String label, String panelName) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBackground(new Color(70, 90, 110));
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
        button.addActionListener(e -> cardLayout.show(contentPanel, panelName));
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(10));
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel titleLabel = new JLabel("Welcome to Admin Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        JLabel welcomeMsg = new JLabel("Hello, " + adminUser.getUsername() + "!");
        welcomeMsg.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 1;
        panel.add(welcomeMsg, gbc);

        JLabel infoLabel = new JLabel("<html><br>Select an option from the menu to manage:<br>" +
                "• Students<br>• Companies<br>• Job Postings<br>• Applications<br></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 2;
        panel.add(infoLabel, gbc);

        return panel;
    }

    private void showProfileDialog() {
        JDialog dialog = new JDialog(this, "Admin Profile", true);
        dialog.setSize(420, 220);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(adminUser.getUsername()), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(adminUser.getEmail()), gbc);

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

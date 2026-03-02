package com.placement.admin.ui;

import com.placement.admin.dao.AdminCompanyDao;
import com.placement.admin.dao.AdminCompanyDao.CompanyRow;
import com.placement.common.service.EmailService;

import jakarta.mail.MessagingException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageCompaniesPanel extends JPanel {

    private final AdminCompanyDao dao = new AdminCompanyDao();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(18);

    private JButton addCompanyBtn;
    private JButton deleteCompanyBtn;

    public ManageCompaniesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(AdminTheme.BG);

        add(createTopBar(), BorderLayout.NORTH);

        String[] cols = {"User ID", "Username", "Email", "Company", "Phone", "Website", "Industry", "Size"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> updateDeleteButtonState());

        AdminTheme.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        AdminTheme.styleScrollPane(sp);
        add(sp, BorderLayout.CENTER);

        load(null);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(AdminTheme.SURFACE);
        bar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel s = new JLabel("Search:");
        AdminTheme.styleLabel(s);
        bar.add(s);
        AdminTheme.styleField(searchField);
        bar.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> load(searchField.getText()));
        AdminTheme.styleButton(searchBtn, AdminTheme.ACCENT);
        bar.add(searchBtn);

        addCompanyBtn = new JButton("Add Company");
        addCompanyBtn.addActionListener(e -> addCompany());
        AdminTheme.styleButton(addCompanyBtn, AdminTheme.ACCENT);
        bar.add(addCompanyBtn);

        deleteCompanyBtn = new JButton("Delete Company");
        deleteCompanyBtn.setEnabled(false);
        deleteCompanyBtn.addActionListener(e -> deleteSelectedCompany());
        AdminTheme.styleButton(deleteCompanyBtn, new Color(220, 38, 38));
        bar.add(deleteCompanyBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            load(null);
        });
        AdminTheme.styleButton(refreshBtn, AdminTheme.MUTED_BUTTON);
        bar.add(refreshBtn);

        return bar;
    }

    private void load(String keyword) {
        tableModel.setRowCount(0);
        List<CompanyRow> rows = dao.listAll(keyword);
        for (CompanyRow r : rows) {
            tableModel.addRow(new Object[]{
                    r.userId,
                    r.username,
                    r.email,
                    r.companyName,
                    r.phone,
                    r.website,
                    r.industry,
                    r.companySize
            });
        }
    }


    private void updateDeleteButtonState() {
        if (deleteCompanyBtn == null) return;
        deleteCompanyBtn.setEnabled(table.getSelectedRow() >= 0);
    }

    private void addCompany() {
        JTextField username = new JTextField();
        JTextField email = new JTextField();
        JTextField companyName = new JTextField();
        JTextField phone = new JTextField();
        JTextField website = new JTextField();
        JTextField industry = new JTextField();
        JTextField size = new JTextField();
        JTextField address = new JTextField();

        AdminTheme.styleField(username);
        AdminTheme.styleField(email);
        AdminTheme.styleField(companyName);
        AdminTheme.styleField(phone);
        AdminTheme.styleField(website);
        AdminTheme.styleField(industry);
        AdminTheme.styleField(size);
        AdminTheme.styleField(address);

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.setBackground(AdminTheme.BG);

        JLabel l1 = new JLabel("Username:"); AdminTheme.styleLabel(l1); p.add(l1); p.add(username);
        JLabel l2 = new JLabel("Email:"); AdminTheme.styleLabel(l2); p.add(l2); p.add(email);
        JLabel l3 = new JLabel("Company Name:"); AdminTheme.styleLabel(l3); p.add(l3); p.add(companyName);
        JLabel l4 = new JLabel("Phone:"); AdminTheme.styleLabel(l4); p.add(l4); p.add(phone);
        JLabel l5 = new JLabel("Website:"); AdminTheme.styleLabel(l5); p.add(l5); p.add(website);
        JLabel l6 = new JLabel("Industry:"); AdminTheme.styleLabel(l6); p.add(l6); p.add(industry);
        JLabel l7 = new JLabel("Company Size:"); AdminTheme.styleLabel(l7); p.add(l7); p.add(size);
        JLabel l8 = new JLabel("Address:"); AdminTheme.styleLabel(l8); p.add(l8); p.add(address);

        int ok = JOptionPane.showConfirmDialog(this, p, "Add Company", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        // Generate a simple password (admin doesn't have to type)
        String password = generatePassword(10);

        try {
            int userId = dao.createCompanyAccount(
                    username.getText(),
                    email.getText(),
                    password,
                    companyName.getText(),
                    phone.getText(),
                    website.getText(),
                    industry.getText(),
                    size.getText(),
                    address.getText()
            );

            // Email credentials
            try {
                EmailService emailService = new EmailService();
                emailService.sendCompanyAccountCreatedEmail(email.getText().trim(), username.getText().trim(), password);
            } catch (IllegalStateException missingCreds) {
                JOptionPane.showMessageDialog(this,
                        "Company created (User ID: " + userId + "), but email was not sent (missing email configuration).\n" +
                                missingCreds.getMessage(),
                        "Email Not Sent",
                        JOptionPane.WARNING_MESSAGE);
            } catch (MessagingException ex) {
                JOptionPane.showMessageDialog(this,
                        "Company created (User ID: " + userId + "), but email failed to send: " + ex.getMessage(),
                        "Email Failed",
                        JOptionPane.WARNING_MESSAGE);
            }

            JOptionPane.showMessageDialog(this,
                    "Company created successfully!\n\n" +
                            "User ID: " + userId + "\n" +
                            "Username: " + username.getText().trim() + "\n" +
                            "Password: " + password,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            load(searchField.getText());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedCompany() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        int userId = (int) tableModel.getValueAt(row, 0);
        String email = String.valueOf(tableModel.getValueAt(row, 2));
        String companyName = String.valueOf(tableModel.getValueAt(row, 3));

        int confirm = JOptionPane.showConfirmDialog(this, "Delete company user ID " + userId + " (" + companyName + ")?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Send confirmation email before deletion (so we still have the address)
        if (email != null && !email.isBlank()) {
            try {
                EmailService emailService = new EmailService();
                emailService.sendCompanyAccountDeletedEmail(email.trim(), companyName);
            } catch (IllegalStateException missingCreds) {
                // ignore; still delete
            } catch (MessagingException ex) {
                // ignore; still delete
            }
        }

        try {
            boolean ok = dao.deleteCompanyAccount(userId);
            JOptionPane.showMessageDialog(this, ok ? "Company deleted." : "Delete failed.");
            load(searchField.getText());
            table.clearSelection();
            updateDeleteButtonState();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String generatePassword(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$";
        StringBuilder sb = new StringBuilder();
        java.util.Random r = new java.util.Random();
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}

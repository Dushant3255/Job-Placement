package com.placement.admin.ui;

import com.placement.admin.dao.AdminApplicationDao;
import com.placement.admin.dao.AdminApplicationDao.ApplicationRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageApplicationsPanel extends JPanel {

    private final AdminApplicationDao dao = new AdminApplicationDao();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(18);

    public ManageApplicationsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        add(createTopBar(), BorderLayout.NORTH);

        String[] cols = {"Application ID", "Job ID", "Company", "Job Title", "Student", "Student Email", "Status", "Applied"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

        load(null);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(new Color(200, 200, 200));
        bar.setBorder(new EmptyBorder(10, 10, 10, 10));

        bar.add(new JLabel("Search:"));
        bar.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> load(searchField.getText()));
        bar.add(searchBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            load(null);
        });
        bar.add(refreshBtn);

        return bar;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bar.setBackground(new Color(200, 200, 200));
        bar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton updateStatusBtn = new JButton("Update Status");
        updateStatusBtn.addActionListener(e -> updateStatus());
        bar.add(updateStatusBtn);

        return bar;
    }

    private void load(String keyword) {
        tableModel.setRowCount(0);
        List<ApplicationRow> rows = dao.listAll(keyword);
        for (ApplicationRow r : rows) {
            tableModel.addRow(new Object[]{
                    r.applicationId,
                    r.jobId,
                    r.companyName,
                    r.jobTitle,
                    r.studentUsername,
                    r.studentEmail,
                    r.status,
                    r.appliedAt
            });
        }
    }

    private void updateStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application.");
            return;
        }

        long applicationId = ((Number) tableModel.getValueAt(selectedRow, 0)).longValue();
        String currentStatus = String.valueOf(tableModel.getValueAt(selectedRow, 6));

        String[] options = {"APPLIED", "SHORTLISTED", "REJECTED", "HIRED"};
        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "Update application status:",
                "Current Status: " + currentStatus,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                currentStatus
        );

        if (newStatus != null && !newStatus.equals(currentStatus)) {
            boolean ok = dao.updateStatus(applicationId, newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Status updated successfully!");
                load(searchField.getText());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

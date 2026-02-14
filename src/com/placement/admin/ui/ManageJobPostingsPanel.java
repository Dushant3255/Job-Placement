package com.placement.admin.ui;

import com.placement.admin.dao.AdminJobListingDao;
import com.placement.admin.dao.AdminJobListingDao.JobRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageJobPostingsPanel extends JPanel {

    private final AdminJobListingDao dao = new AdminJobListingDao();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(18);

    public ManageJobPostingsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        add(createTopBar(), BorderLayout.NORTH);

        String[] cols = {"Job ID", "Company", "Title", "Department", "Min GPA", "Min Year", "Status", "Posted"};
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
        
        JButton addOffCampusBtn = new JButton("Add Off Campus Job");
        addOffCampusBtn.addActionListener(e -> openAddOffCampusDialog());
        bar.add(addOffCampusBtn);


        return bar;
    }
    
    
    private void load(String keyword) {
        tableModel.setRowCount(0);
        List<JobRow> rows = dao.listAll(keyword);
        for (JobRow r : rows) {
            tableModel.addRow(new Object[]{
                    r.jobId,
                    r.companyName,
                    r.title,
                    r.department,
                    r.minGpa,
                    r.minYear,
                    r.status,
                    r.postedAt
            });
        }
    }

    private void updateStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job listing.");
            return;
        }

        long jobId = ((Number) tableModel.getValueAt(selectedRow, 0)).longValue();
        String currentStatus = String.valueOf(tableModel.getValueAt(selectedRow, 6));

        String[] options = {"OPEN", "CLOSED"};
        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "Update job status:",
                "Current Status: " + currentStatus,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                currentStatus
        );

        if (newStatus != null && !newStatus.equals(currentStatus)) {
            boolean ok = dao.updateStatus(jobId, newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Status updated successfully!");
                load(searchField.getText());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void openAddOffCampusDialog() {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Add Off Campus Job",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        JTextField companyField = new JTextField(22);
        JTextField titleField = new JTextField(22);
        JTextField deptField = new JTextField(22);
        JTextArea descArea = new JTextArea(5, 22);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JTextField minGpaField = new JTextField(22);
        JTextField minYearField = new JTextField(22);

        // Row helper
        java.util.function.BiConsumer<String, Component> addRow = (label, comp) -> {
            gbc.gridx = 0;
            form.add(new JLabel(label), gbc);
            gbc.gridx = 1;
            form.add(comp, gbc);
            gbc.gridy++;
        };

        addRow.accept("Company Name *", companyField);
        addRow.accept("Job Title *", titleField);
        addRow.accept("Department", deptField);
        addRow.accept("Description *", new JScrollPane(descArea));
        addRow.accept("Min GPA", minGpaField);
        addRow.accept("Min Year", minYearField);

        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");

        save.addActionListener(ev -> {
            String company = companyField.getText().trim();
            String title = titleField.getText().trim();
            String dept = deptField.getText().trim();
            String desc = descArea.getText().trim();

            if (company.isEmpty() || title.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill required fields (*).");
                return;
            }

            Double minGpa = null;
            if (!minGpaField.getText().trim().isEmpty()) {
                try { minGpa = Double.parseDouble(minGpaField.getText().trim()); }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Min GPA must be a number.");
                    return;
                }
            }

            Integer minYear = null;
            if (!minYearField.getText().trim().isEmpty()) {
                try { minYear = Integer.parseInt(minYearField.getText().trim()); }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Min Year must be an integer.");
                    return;
                }
            }

            boolean ok = dao.insertOffCampusJob(
                    company,
                    title,
                    dept.isEmpty() ? null : dept,
                    desc,
                    minGpa,
                    minYear
            );

            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Off campus job added!");
                dialog.dispose();
                load(searchField.getText());
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add job.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancel.addActionListener(ev -> dialog.dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(save);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(form, BorderLayout.CENTER);
        dialog.getContentPane().add(buttons, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

}

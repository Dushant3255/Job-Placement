package com.placement.student.ui;

import com.placement.student.controller.ApplicationController;
import com.placement.student.dao.ApplicationDAO;
import com.placement.student.dao.ApplicationDAOImpl;
import com.placement.student.dao.JobListingDAO;
import com.placement.student.dao.JobListingDAOImpl;
import com.placement.student.model.Application;
import com.placement.student.service.ApplicationService;
import com.placement.student.service.JobSearchService;
import com.placement.student.service.ServiceException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyApplicationsView extends JPanel {

    private final long studentId;
    private final ApplicationController controller;

    private final DefaultTableModel model;
    private final JTable table;

    private final CardLayout card = new CardLayout();
    private final JPanel tableOrEmpty = new JPanel(card);

    public MyApplicationsView(long studentId) {
        
        setLayout(new BorderLayout());
        setBackground(StudentTheme.BG);
this.studentId = studentId;

        ApplicationDAO appDAO = new ApplicationDAOImpl();
        JobListingDAO jobDAO = new JobListingDAOImpl();
        // ApplicationService needs jobDAO (already used for checks)
        ApplicationService service = new ApplicationService(appDAO, jobDAO);
        this.controller = new ApplicationController(service);

        model = new DefaultTableModel(new Object[]{"Application ID", "Company", "Job Title", "Status", "Applied At"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        StudentTheme.styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(StudentTheme.statusChipRenderer());

        add(StudentTheme.header("My Applications", "Track applied companies and application status."), BorderLayout.NORTH);

        JPanel center = StudentTheme.contentPanel();
        center.setLayout(new BorderLayout(12, 12));
        center.add(buildTopBar(), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        tableOrEmpty.add(sp, "TABLE");
        tableOrEmpty.add(StudentTheme.emptyState("No applications yet", "Apply to a job listing to see it here."), "EMPTY");
        center.add(tableOrEmpty, BorderLayout.CENTER);
        center.add(buildActions(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(StudentTheme.BG);

        JLabel title = new JLabel("Student ID: " + studentId);
        title.setFont(StudentTheme.fontBold(13));
        p.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        StudentTheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> refresh());
        p.add(refreshBtn, BorderLayout.EAST);

        return p;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        p.setBackground(StudentTheme.BG);

        JButton withdrawBtn = new JButton("Withdraw");
        JButton closeBtn = new JButton("Close");

        StudentTheme.styleSecondaryButton(withdrawBtn);
        StudentTheme.styleSecondaryButton(closeBtn);

        withdrawBtn.addActionListener(e -> withdrawSelected());
        closeBtn.addActionListener(e -> StudentNav.goHome(this));

        p.add(withdrawBtn);
        p.add(closeBtn);
        return p;
    }

    private void refresh() {
        try {
            List<Application> list = controller.viewMyApplications(studentId);
            model.setRowCount(0);
            for (Application a : list) {
                model.addRow(new Object[]{
                        a.getApplicationId(),
                        a.getCompanyName(),
                        a.getJobTitle(),
                        a.getStatus(),
                        a.getAppliedAt()
                });
            }
            if (model.getRowCount() == 0) card.show(tableOrEmpty, "EMPTY");
            else card.show(tableOrEmpty, "TABLE");
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private Long selectedApplicationId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        Object v = model.getValueAt(row, 0);
        if (v == null) return null;
        return Long.parseLong(v.toString());
    }

    private void withdrawSelected() {
        Long appId = selectedApplicationId();
        if (appId == null) {
            UiUtil.error("Select an application to withdraw.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, "Withdraw application " + appId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            boolean success = controller.withdrawApplication(studentId, appId);
            if (success) {
                UiUtil.info("Application withdrawn.");
                refresh();
            } else {
                UiUtil.error("Unable to withdraw (maybe already processed).");
            }
            if (model.getRowCount() == 0) card.show(tableOrEmpty, "EMPTY");
            else card.show(tableOrEmpty, "TABLE");
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }
}

package com.placement.student.ui;

import com.placement.student.controller.ApplicationController;
import com.placement.student.controller.JobController;
import com.placement.student.dao.*;
import com.placement.student.model.JobListing;
import com.placement.student.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class JobListingsView extends JPanel {

    private final long studentId;

    private final JobController jobController;
    private final ApplicationController applicationController;

    private final DefaultTableModel model;
    private final JTable table;

    private final JTextField deptField = new JTextField(12);
    private final JTextField gpaField = new JTextField(6);
    private final JTextField yearField = new JTextField(6);

    // empty-state support
    private final CardLayout card = new CardLayout();
    private final JPanel tableOrEmpty = new JPanel(card);

    public JobListingsView(long studentId) {
        
        setLayout(new BorderLayout());
        setBackground(StudentTheme.BG);
this.studentId = studentId;

        // --- wiring ---
        JobListingDAO jobDAO = new JobListingDAOImpl();
        ApplicationDAO appDAO = new ApplicationDAOImpl();
        JobSearchService jobService = new JobSearchService(jobDAO);
        ApplicationService applicationService = new ApplicationService(appDAO, jobDAO);
        this.jobController = new JobController(jobService);
        this.applicationController = new ApplicationController(applicationService);

        model = new DefaultTableModel(new Object[]{"Job ID", "Company", "Title", "Dept", "Min GPA", "Min Year", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        StudentTheme.styleTable(table);
        // Status chip
        table.getColumnModel().getColumn(6).setCellRenderer(StudentTheme.statusChipRenderer());

        add(StudentTheme.header("Job Listings", "Browse positions, filter, and apply."), BorderLayout.NORTH);

        JPanel center = StudentTheme.contentPanel();
        center.setLayout(new BorderLayout(12, 12));
        center.add(buildFilters(), BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        tableOrEmpty.add(sp, "TABLE");
        tableOrEmpty.add(StudentTheme.emptyState("No job listings found", "Try All (Past+Current) or adjust your filters."), "EMPTY");
        center.add(tableOrEmpty, BorderLayout.CENTER);

        center.add(buildActions(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        loadAllOpenJobs();
    }

    private JPanel buildFilters() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.setBackground(StudentTheme.BG);

        StudentTheme.styleField(deptField);
        StudentTheme.styleField(gpaField);
        StudentTheme.styleField(yearField);

        JButton allBtn = new JButton("All Open");
        JButton allAnyBtn = new JButton("All (Past+Current)");
        JButton deptBtn = new JButton("Search Dept");
        JButton eligBtn = new JButton("Eligible For Me");

        StudentTheme.styleSecondaryButton(deptBtn);
        StudentTheme.styleSecondaryButton(eligBtn);
        StudentTheme.styleSecondaryButton(allBtn);
        StudentTheme.styleSecondaryButton(allAnyBtn);

        p.add(new JLabel("Dept"));
        p.add(deptField);
        p.add(deptBtn);

        p.add(new JLabel("GPA"));
        p.add(gpaField);
        p.add(new JLabel("Year"));
        p.add(yearField);
        p.add(eligBtn);

        p.add(allBtn);
        p.add(allAnyBtn);

        allBtn.addActionListener(e -> loadAllOpenJobs());
        allAnyBtn.addActionListener(e -> loadAllJobs());
        deptBtn.addActionListener(e -> loadByDepartment());
        eligBtn.addActionListener(e -> loadByEligibility());

        return p;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        p.setBackground(StudentTheme.BG);

        JButton detailsBtn = new JButton("View Details");
        JButton applyBtn = new JButton("Apply");
        JButton closeBtn = new JButton("Close");

        StudentTheme.styleSecondaryButton(detailsBtn);
        StudentTheme.stylePrimaryButton(applyBtn);
        StudentTheme.styleSecondaryButton(closeBtn);

        detailsBtn.addActionListener(e -> viewSelectedDetails());
        applyBtn.addActionListener(e -> applyToSelected());
        closeBtn.addActionListener(e -> StudentNav.goHome(this));

        p.add(detailsBtn);
        p.add(applyBtn);
        p.add(closeBtn);
        return p;
    }

    private void loadAllOpenJobs() {
        try {
            List<JobListing> jobs = jobController.viewAllOpenJobs();
            fillTable(jobs);
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void loadAllJobs() {
        try {
            List<JobListing> jobs = jobController.viewAllJobs();
            fillTable(jobs);
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void loadByDepartment() {
        try {
            String dept = deptField.getText().trim();
            List<JobListing> jobs = jobController.searchByDepartment(dept);
            fillTable(jobs);
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void loadByEligibility() {
        try {
            double gpa = Double.parseDouble(gpaField.getText().trim());
            int year = Integer.parseInt(yearField.getText().trim());
            List<JobListing> jobs = jobController.filterByEligibility(gpa, year);
            fillTable(jobs);
        } catch (NumberFormatException ex) {
            UiUtil.error("Enter valid GPA and Year.");
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void fillTable(List<JobListing> jobs) {
        model.setRowCount(0);
        for (JobListing j : jobs) {
            model.addRow(new Object[]{
                    j.getJobId(),
                    j.getCompanyName(),
                    j.getTitle(),
                    j.getDepartment(),
                    j.getMinGpa(),
                    j.getMinYear(),
                    j.getStatus()
            });
        }

        if (model.getRowCount() == 0) card.show(tableOrEmpty, "EMPTY");
        else card.show(tableOrEmpty, "TABLE");
    }

    private long getSelectedJobId() {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        Object v = model.getValueAt(row, 0);
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.parseLong(String.valueOf(v));
    }

    private void viewSelectedDetails() {
        long jobId = getSelectedJobId();
        if (jobId <= 0) {
            UiUtil.error("Select a job first.");
            return;
        }

        try {
            JobListing j = jobController.viewJobDetails(jobId);
            UiUtil.info(
                    "Company: " + j.getCompanyName() + "\n" +
                    "Title: " + j.getTitle() + "\n" +
                    "Department: " + j.getDepartment() + "\n" +
                    "Status: " + j.getStatus() + "\n\n" +
                    "Description:\n" + j.getDescription()
            );
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void applyToSelected() {
        long jobId = getSelectedJobId();
        if (jobId <= 0) {
            UiUtil.error("Select a job first.");
            return;
        }

        if (!UiUtil.confirm("Apply to job ID " + jobId + "?")) return;

        try {
            long applicationId = applicationController.applyForJob(studentId, jobId);
            UiUtil.info("Applied successfully. Application ID: " + applicationId);
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }
}

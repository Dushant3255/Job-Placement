package com.placement.student.ui;

import com.placement.student.controller.ApplicationController;
import com.placement.student.controller.JobController;
import com.placement.student.dao.*;
import com.placement.student.service.*;
import com.placement.student.model.JobListing;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class JobListingsView extends BaseFrame {

    private final long studentId;

    // light wiring for now (later move to bootstrap)
    private final JobController jobController;
    private final ApplicationController applicationController;

    private final DefaultTableModel model;
    private final JTable table;

    private final JTextField deptField = new JTextField(12);
    private final JTextField gpaField = new JTextField(5);
    private final JTextField yearField = new JTextField(5);

    public JobListingsView(long studentId) {
        super("Job Listings");
        this.studentId = studentId;

        // --- light wiring (temporary) ---
        JobListingDAO jobDAO = new JobListingDAOImpl();
        ApplicationDAO appDAO = new ApplicationDAOImpl();
        JobSearchService jobService = new JobSearchService(jobDAO);
        ApplicationService applicationService = new ApplicationService(appDAO, jobDAO);
        this.jobController = new JobController(jobService);
        this.applicationController = new ApplicationController(applicationService);
        // --------------------------------

        model = new DefaultTableModel(new Object[]{"Job ID", "Company", "Title", "Dept", "Min GPA", "Min Year"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);

        add(buildFilters(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        loadAllOpenJobs();

        setVisible(true);
    }

    private JPanel buildFilters() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton allBtn = new JButton("All Open");
        JButton deptBtn = new JButton("Search Dept");
        JButton eligBtn = new JButton("Eligible For Me");

        p.add(new JLabel("Dept:"));
        p.add(deptField);
        p.add(deptBtn);

        p.add(new JLabel("GPA:"));
        p.add(gpaField);
        p.add(new JLabel("Year:"));
        p.add(yearField);
        p.add(eligBtn);

        p.add(allBtn);

        allBtn.addActionListener(e -> loadAllOpenJobs());
        deptBtn.addActionListener(e -> loadByDepartment());
        eligBtn.addActionListener(e -> loadByEligibility());

        return p;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton detailsBtn = new JButton("View Details");
        JButton applyBtn = new JButton("Apply");
        JButton closeBtn = new JButton("Close");

        detailsBtn.addActionListener(e -> viewSelectedDetails());
        applyBtn.addActionListener(e -> applyToSelected());
        closeBtn.addActionListener(e -> dispose());

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
                    j.getMinYear()
            });
        }
    }

    private long getSelectedJobId() {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        return (long) model.getValueAt(row, 0);
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
                    "Department: " + j.getDepartment() + "\n\n" +
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

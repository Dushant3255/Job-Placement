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

    // Filters
    private final JTextField deptField = new JTextField(16);
    private final JTextField gpaField  = new JTextField(8);
    private final JTextField yearField = new JTextField(8);

    // Empty state
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

        // Status column shows student's status for job if applied; else job status
        model = new DefaultTableModel(new Object[]{
                "Job ID", "Company", "Title", "Dept", "Min GPA", "Min Year", "Status"
        }, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        StudentTheme.styleTable(table);
        table.getColumnModel().getColumn(6).setCellRenderer(StudentTheme.statusChipRenderer());

        add(StudentTheme.header("Job Listings", "Browse positions, filter, and apply."), BorderLayout.NORTH);

        JPanel center = StudentTheme.contentPanel();
        center.setLayout(new BorderLayout(12, 12));
        center.add(buildFilters(), BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        tableOrEmpty.add(sp, "TABLE");
        tableOrEmpty.add(StudentTheme.emptyState(
                "No job listings found",
                "Adjust filters or clear them to see more results."), "EMPTY");
        center.add(tableOrEmpty, BorderLayout.CENTER);

        center.add(buildActions(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        loadAllOpenJobs();
    }

    /**
     * Filters UI:
     * - Two rows (so buttons never wrap and disappear)
     * - Clear Filters always visible
     */
    private JPanel buildFilters() {
        JPanel parent = new JPanel();
        parent.setBackground(StudentTheme.BG);
        parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        row1.setBackground(StudentTheme.BG);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        row2.setBackground(StudentTheme.BG);

        StudentTheme.styleField(deptField);
        StudentTheme.styleField(gpaField);
        StudentTheme.styleField(yearField);

        JButton allOpenBtn   = new JButton("All Open");
        JButton allAnyBtn    = new JButton("All (Past+Current)");
        JButton deptSearchBtn = new JButton("Search Dept");
        JButton eligibleBtn  = new JButton("Eligible For Me");
        JButton clearBtn     = new JButton("Clear Filters");

        StudentTheme.styleSecondaryButton(allOpenBtn);
        StudentTheme.styleSecondaryButton(allAnyBtn);
        StudentTheme.styleSecondaryButton(deptSearchBtn);
        StudentTheme.styleSecondaryButton(eligibleBtn);
        StudentTheme.styleSecondaryButton(clearBtn);

        // Row 1 (Dept + general list buttons)
        row1.add(new JLabel("Dept"));
        row1.add(deptField);
        row1.add(deptSearchBtn);
        row1.add(allOpenBtn);
        row1.add(allAnyBtn);

        // Row 2 (Eligibility + clear)
        row2.add(new JLabel("GPA"));
        row2.add(gpaField);
        row2.add(new JLabel("Year"));
        row2.add(yearField);
        row2.add(eligibleBtn);
        row2.add(clearBtn);

        // Actions
        allOpenBtn.addActionListener(e -> loadAllOpenJobs());
        allAnyBtn.addActionListener(e -> loadAllJobs());
        deptSearchBtn.addActionListener(e -> loadByDepartment());
        eligibleBtn.addActionListener(e -> loadByEligibility());

        // Clear filters: reset fields + reload open jobs
        clearBtn.addActionListener(e -> {
            deptField.setText("");
            gpaField.setText("");
            yearField.setText("");
            loadAllOpenJobs();
        });

        parent.add(row1);
        parent.add(row2);
        return parent;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        p.setBackground(StudentTheme.BG);

        JButton detailsBtn = new JButton("View Details");
        JButton applyBtn   = new JButton("Apply");
        JButton clearSelBtn = new JButton("Clear Selection");
        JButton closeBtn   = new JButton("Close");

        StudentTheme.styleSecondaryButton(detailsBtn);
        StudentTheme.stylePrimaryButton(applyBtn);
        StudentTheme.styleSecondaryButton(clearSelBtn);
        StudentTheme.styleSecondaryButton(closeBtn);

        detailsBtn.addActionListener(e -> viewSelectedDetails());
        applyBtn.addActionListener(e -> applyToSelected());
        clearSelBtn.addActionListener(e -> table.clearSelection());
        closeBtn.addActionListener(e -> StudentNav.goHome(this));

        p.add(detailsBtn);
        p.add(applyBtn);
        p.add(clearSelBtn);
        p.add(closeBtn);
        return p;
    }

    private void loadAllOpenJobs() {
        try {
            fillTable(jobController.viewAllOpenJobs());
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void loadAllJobs() {
        try {
            fillTable(jobController.viewAllJobs());
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void loadByDepartment() {
        try {
            String dept = deptField.getText() == null ? "" : deptField.getText().trim();
            fillTable(jobController.searchByDepartment(dept));
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void loadByEligibility() {
        try {
            double gpa = Double.parseDouble(gpaField.getText().trim());
            int year = Integer.parseInt(yearField.getText().trim());
            fillTable(jobController.filterByEligibility(gpa, year));
        } catch (NumberFormatException ex) {
            UiUtil.error("Enter valid GPA and Year.");
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void fillTable(List<JobListing> jobs) {
        model.setRowCount(0);

        for (JobListing j : jobs) {
            // If student already applied, show application status, else job status
            String myStatus = null;
            try {
                myStatus = applicationController.getStatusForJob(studentId, j.getJobId());
            } catch (Exception ignore) {}

            String statusToShow = (myStatus == null || myStatus.trim().isEmpty())
                    ? j.getStatus()
                    : myStatus;

            model.addRow(new Object[]{
                    j.getJobId(),
                    j.getCompanyName(),
                    j.getTitle(),
                    j.getDepartment(),
                    j.getMinGpa(),
                    j.getMinYear(),
                    statusToShow
            });
        }

        table.clearSelection();

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

    private int getSelectedRowIndex() {
        return table.getSelectedRow();
    }

    private void viewSelectedDetails() {
        long jobId = getSelectedJobId();
        if (jobId <= 0) {
            UiUtil.error("Select a job first.");
            return;
        }

        try {
            JobListing j = jobController.viewJobDetails(jobId);
            String skills = j.getSkills();
            if (skills == null) skills = "";

            UiUtil.info(
                    "Company: " + j.getCompanyName() + "\n" +
                    "Title: " + j.getTitle() + "\n" +
                    "Department: " + j.getDepartment() + "\n" +
                    "Status: " + j.getStatus() + "\n" +
                    (skills.trim().isEmpty() ? "" : ("Skills: " + skills + "\n")) +
                    "\nDescription:\n" + j.getDescription()
            );
        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void applyToSelected() {
        long jobId = getSelectedJobId();
        int row = getSelectedRowIndex();

        if (jobId <= 0 || row < 0) {
            UiUtil.error("Select a job first.");
            return;
        }

        // UI guard: prevent applying twice
        try {
            String myStatus = applicationController.getStatusForJob(studentId, jobId);
            if (myStatus != null && !myStatus.trim().isEmpty()) {
                UiUtil.error("You already applied for this job. Current status: " + myStatus);
                return;
            }
        } catch (Exception ignore) {}

        if (!UiUtil.confirm("Apply to job ID " + jobId + "?")) return;

        try {
            long applicationId = applicationController.applyForJob(studentId, jobId);
            UiUtil.info("Applied successfully. Application ID: " + applicationId);

            // Update status immediately
            model.setValueAt("APPLIED", row, 6);

        } catch (ServiceException ex) {
            UiUtil.error(ex.getMessage());
        }
    }
}

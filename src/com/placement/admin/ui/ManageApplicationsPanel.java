package com.placement.admin.ui;

import com.placement.admin.dao.AdminApplicationDao;
import com.placement.admin.dao.AdminApplicationDao.ApplicationRow;
import com.placement.common.service.EmailService;

import jakarta.mail.MessagingException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

        String[] cols = {
                "Application ID", "Job ID", "Company", "Job Title",
                "Student", "Student Email",
                "App Status", "Interview At", "Offer Status",
                "Admin Confirmed", "Applied"
        };

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

        JButton confirmBtn = new JButton("Confirm Company Action");
        confirmBtn.addActionListener(e -> confirmCompanyAction());
        bar.add(confirmBtn);

        JButton uploadOfferBtn = new JButton("Upload Offer Letter");
        uploadOfferBtn.addActionListener(e -> uploadOfferLetter());
        bar.add(uploadOfferBtn);

        JButton declineBtn = new JButton("Decline (Reject)");
        declineBtn.addActionListener(e -> declineApplicant());
        bar.add(declineBtn);

        JButton updateStatusBtn = new JButton("Update Status");
        updateStatusBtn.addActionListener(e -> updateStatus());
        bar.add(updateStatusBtn);

        return bar;
    }

    private void load(String keyword) {
        tableModel.setRowCount(0);
        List<ApplicationRow> rows = dao.listAll(keyword);

        for (ApplicationRow r : rows) {
            String adminConfirmedText = (r.adminConfirmed == 1) ? "YES" : "NO";
            tableModel.addRow(new Object[]{
                    r.applicationId,
                    r.jobId,
                    r.companyName,
                    r.jobTitle,
                    r.studentUsername,
                    r.studentEmail,
                    r.status,
                    r.interviewScheduledAt == null ? "" : r.interviewScheduledAt,
                    r.offerStatus == null ? "" : r.offerStatus,
                    adminConfirmedText,
                    r.appliedAt
            });
        }
    }

    private long getSelectedApplicationId() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application.");
            return -1;
        }
        return ((Number) tableModel.getValueAt(selectedRow, 0)).longValue();
    }

    private String getSelectedStudentEmail() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return null;
        return String.valueOf(tableModel.getValueAt(selectedRow, 5));
    }

    private String getSelectedStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return null;
        return String.valueOf(tableModel.getValueAt(selectedRow, 6));
    }

    private String getSelectedOfferStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return null;
        return String.valueOf(tableModel.getValueAt(selectedRow, 8));
    }

    /**
     * Requirement #1 and #4:
     * - If company has SHORTLISTED / REJECTED / INTERVIEW_SCHEDULED, admin can confirm.
     * - If interview scheduled, confirm updates application status to PENDING.
     */
    private void confirmCompanyAction() {
        long applicationId = getSelectedApplicationId();
        if (applicationId < 0) return;

        String currentStatus = getSelectedStatus();
        if (currentStatus == null || currentStatus.isBlank()) return;

        if (!(currentStatus.equalsIgnoreCase("SHORTLISTED")
                || currentStatus.equalsIgnoreCase("REJECTED")
                || currentStatus.equalsIgnoreCase("INTERVIEW_SCHEDULED"))) {
            JOptionPane.showMessageDialog(this,
                    "This action is intended for company statuses: SHORTLISTED, REJECTED, INTERVIEW_SCHEDULED.");
            return;
        }

        boolean ok = dao.confirmCompanyAction(applicationId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Confirmed.");
            load(searchField.getText());
        } else {
            JOptionPane.showMessageDialog(this, "Failed to confirm.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Requirement #2:
     * - If shortlisted, admin uploads offer letter.
     * - Offer status remains PENDING.
     * - Email sent to applicant confirming they received an offer letter.
     * - Student acceptance (in student module) updates offer to ACCEPTED, which later allows admin to update to HIRED.
     */
    private void uploadOfferLetter() {
        long applicationId = getSelectedApplicationId();
        if (applicationId < 0) return;

        String status = getSelectedStatus();
        if (status == null || !status.equalsIgnoreCase("SHORTLISTED")) {
            JOptionPane.showMessageDialog(this, "Offer letters can only be uploaded for SHORTLISTED applicants.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Offer Letter (PDF)");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selected = chooser.getSelectedFile();
        if (selected == null || !selected.exists()) return;

        // Copy into project data folder for predictable relative paths.
        // Stored path will be like: data/offers/<applicationId>-<filename>
        try {
            File offersDir = new File("data", "offers");
            if (!offersDir.exists()) offersDir.mkdirs();

            String safeName = selected.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = new File(offersDir, applicationId + "-" + safeName).toPath();
            Files.copy(selected.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            // Create offer row in DB (PENDING) and attach letter path
            long offerId = dao.createOffer(applicationId, null, null, target.toString());
            if (offerId <= 0) {
                JOptionPane.showMessageDialog(this, "Failed to save offer in database.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Email notification
            String to = getSelectedStudentEmail();
            if (to != null && !to.isBlank()) {
                try {
                    EmailService email = new EmailService();
                    email.send(
                            to,
                            "Offer Letter Available",
                            "You have received a letter of offer for your application (ID: " + applicationId + ").\n"
                                    + "Please log in to the Job Placement System to view the offer and respond.\n\n"
                                    + "Status: PENDING"
                    );
                } catch (IllegalStateException missingCreds) {
                    // Email config missing - still keep DB changes
                    JOptionPane.showMessageDialog(this,
                            "Offer saved, but email was not sent (missing email configuration).\n"
                                    + missingCreds.getMessage(),
                            "Email Not Sent",
                            JOptionPane.WARNING_MESSAGE);
                } catch (MessagingException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Offer saved, but email failed to send: " + ex.getMessage(),
                            "Email Failed",
                            JOptionPane.WARNING_MESSAGE);
                }
            }

            JOptionPane.showMessageDialog(this, "Offer letter uploaded and offer created (PENDING).");
            load(searchField.getText());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to upload offer letter: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Requirement #3:
     * - One button rejects the applicant and emails them.
     */
    private void declineApplicant() {
        long applicationId = getSelectedApplicationId();
        if (applicationId < 0) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Reject this applicant? This will set status to REJECTED and send an email.",
                "Confirm Reject",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = dao.declineApplicant(applicationId);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to reject applicant.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String to = getSelectedStudentEmail();
        if (to != null && !to.isBlank()) {
            try {
                EmailService email = new EmailService();
                email.send(
                        to,
                        "Application Update",
                        "Your application (ID: " + applicationId + ") has been declined.\n"
                                + "Status: REJECTED"
                );
            } catch (IllegalStateException missingCreds) {
                JOptionPane.showMessageDialog(this,
                        "Applicant rejected, but email was not sent (missing email configuration).\n"
                                + missingCreds.getMessage(),
                        "Email Not Sent",
                        JOptionPane.WARNING_MESSAGE);
            } catch (MessagingException ex) {
                JOptionPane.showMessageDialog(this,
                        "Applicant rejected, but email failed to send: " + ex.getMessage(),
                        "Email Failed",
                        JOptionPane.WARNING_MESSAGE);
            }
        }

        JOptionPane.showMessageDialog(this, "Applicant rejected.");
        load(searchField.getText());
    }

    /**
     * Keep admin override, but tuned to the required flow:
     * - After student accepts offer, admin may update to HIRED.
     */
    private void updateStatus() {
        long applicationId = getSelectedApplicationId();
        if (applicationId < 0) return;

        String currentStatus = getSelectedStatus();
        String offerStatus = getSelectedOfferStatus();

        // Allow HIRED only if offer was accepted (offer_status == ACCEPTED)
        String[] options;
        if ("ACCEPTED".equalsIgnoreCase(offerStatus) || "OFFER_ACCEPTED".equalsIgnoreCase(currentStatus)) {
            options = new String[]{"HIRED"};
        } else {
            options = new String[]{"APPLIED", "SHORTLISTED", "REJECTED", "PENDING"};
        }

        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "Update application status:",
                "Current Status: " + currentStatus,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                currentStatus
        );

        if (newStatus != null && !newStatus.equalsIgnoreCase(currentStatus)) {
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

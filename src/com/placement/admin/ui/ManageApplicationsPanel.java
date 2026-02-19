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
import java.util.List;

public class ManageApplicationsPanel extends JPanel {

    private final AdminApplicationDao dao = new AdminApplicationDao();

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(18);

    private JButton uploadOfferBtn;
    private JButton confirmStatusBtn;

    // Column indexes in tableModel
    private static final int COL_APPLICATION_ID = 0;
    private static final int COL_COMPANY = 2;
    private static final int COL_JOB_TITLE = 3;
    private static final int COL_STUDENT = 4;
    private static final int COL_STUDENT_EMAIL = 5;
    private static final int COL_APP_STATUS = 6;

    private static final int COL_OFFER_STATUS = 13;
    private static final int COL_ADMIN_CONFIRMED = 14;

    public ManageApplicationsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 245));

        add(createTopBar(), BorderLayout.NORTH);

        String[] cols = {
                "Application ID", "Job ID", "Company", "Job Title",
                "Student", "Student Email",
                "App Status",
                "Interview At", "Interview Mode", "Office Location", "Meeting Link", "Interview Status", "Interview Notes",
                "Offer Status",
                "Admin Confirmed",
                "Applied"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

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
        // Right-aligned with nicer spacing
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bar.setBackground(new Color(200, 200, 200));
        bar.setBorder(new EmptyBorder(10, 10, 10, 10));

        confirmStatusBtn = new JButton("Confirm Status");
        confirmStatusBtn.addActionListener(e -> confirmStatus());
        bar.add(confirmStatusBtn);

        uploadOfferBtn = new JButton("Upload Offer Letter");
        uploadOfferBtn.setEnabled(false);
        uploadOfferBtn.addActionListener(e -> uploadOfferLetter());
        bar.add(uploadOfferBtn);

        Dimension btnSize = new Dimension(180, 30);
        confirmStatusBtn.setPreferredSize(btnSize);
        uploadOfferBtn.setPreferredSize(btnSize);

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
                    r.interviewMode == null ? "" : r.interviewMode,
                    r.interviewLocation == null ? "" : r.interviewLocation,
                    r.interviewMeetingLink == null ? "" : r.interviewMeetingLink,
                    r.interviewStatus == null ? "" : r.interviewStatus,
                    r.interviewNotes == null ? "" : r.interviewNotes,

                    r.offerStatus == null ? "" : r.offerStatus,
                    adminConfirmedText,
                    r.appliedAt
            });
        }

        updateButtonStates();
    }

    private void updateButtonStates() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            if (uploadOfferBtn != null) uploadOfferBtn.setEnabled(false);
            if (confirmStatusBtn != null) confirmStatusBtn.setEnabled(false);
            return;
        }

        String st = getSelectedStatus();
        boolean canConfirm = st != null && (
                st.equalsIgnoreCase("SHORTLISTED")
                        || st.equalsIgnoreCase("REJECTED")
                        || st.equalsIgnoreCase("INTERVIEW_SCHEDULED")
        );

        boolean isConfirmed = isSelectedAdminConfirmed();

        // Grey out Confirm if already confirmed
        if (confirmStatusBtn != null) confirmStatusBtn.setEnabled(canConfirm && !isConfirmed);

        // Upload becomes clickable only AFTER status is confirmed AND shortlisted
        boolean isShortlisted = st != null && st.equalsIgnoreCase("SHORTLISTED");
        if (uploadOfferBtn != null) uploadOfferBtn.setEnabled(isShortlisted && isConfirmed);
}

    private boolean isSelectedAdminConfirmed() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return false;
        String val = String.valueOf(tableModel.getValueAt(selectedRow, COL_ADMIN_CONFIRMED));
        return val != null && val.equalsIgnoreCase("YES");
    }

private String getSelectedCompanyName() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) return null;
    return String.valueOf(tableModel.getValueAt(selectedRow, COL_COMPANY));
}

private String getSelectedJobTitle() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) return null;
    return String.valueOf(tableModel.getValueAt(selectedRow, COL_JOB_TITLE));
}

private String getSelectedStudentUsername() {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) return null;
    return String.valueOf(tableModel.getValueAt(selectedRow, COL_STUDENT));
}

private long getSelectedApplicationId() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application.");
            return -1;
        }
        return ((Number) tableModel.getValueAt(selectedRow, COL_APPLICATION_ID)).longValue();
    }

    private String getSelectedStudentEmail() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return null;
        return String.valueOf(tableModel.getValueAt(selectedRow, COL_STUDENT_EMAIL));
    }

    private String getSelectedStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return null;
        return String.valueOf(tableModel.getValueAt(selectedRow, COL_APP_STATUS));
    }

    private String getSelectedOfferStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return null;
        return String.valueOf(tableModel.getValueAt(selectedRow, COL_OFFER_STATUS));
    }

    /**
     * Requirement #1 and #4:
     * - If company has SHORTLISTED / REJECTED / INTERVIEW_SCHEDULED, admin can confirm.
     * - If interview scheduled, confirm updates application status to PENDING.
     */
    /**
 * Confirm the current company status for the selected application.
 * Rules:
 * - If status is REJECTED: behaves like Decline (Reject) (sets REJECTED and emails student)
 * - If status is INTERVIEW_SCHEDULED: sets status to PENDING (and marks admin_confirmed)
 * - If status is SHORTLISTED: marks admin_confirmed (upload offer becomes enabled)
 */
private void confirmStatus() {
    long applicationId = getSelectedApplicationId();
    if (applicationId < 0) return;

    String currentStatus = getSelectedStatus();
    if (currentStatus == null || currentStatus.isBlank()) return;

    // Safety confirmation dialog
    String msg;
    if (currentStatus.equalsIgnoreCase("REJECTED")) {
        msg = "Confirm REJECTION for Application #" + applicationId + "?\n"
                + "This will set status to REJECTED and send a rejection email.";
    } else if (currentStatus.equalsIgnoreCase("INTERVIEW_SCHEDULED")) {
        msg = "Confirm INTERVIEW_SCHEDULED for Application #" + applicationId + "?\n"
                + "This will set the application status to PENDING.";
    } else if (currentStatus.equalsIgnoreCase("SHORTLISTED")) {
        msg = "Confirm SHORTLISTED for Application #" + applicationId + "?\n"
                + "After confirmation, Upload Offer Letter will be enabled.";
    } else {
        msg = "Confirm current status for Application #" + applicationId + " (" + currentStatus + ")?";
    }

    int choice = JOptionPane.showConfirmDialog(
            this,
            msg,
            "Confirm Status",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );
    if (choice != JOptionPane.YES_OPTION) return;

    if (currentStatus.equalsIgnoreCase("REJECTED")) {
        // Do what Decline button does (DB + email)
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

        JOptionPane.showMessageDialog(this, "Status confirmed: REJECTED.");
        load(searchField.getText());
        table.clearSelection();
        updateButtonStates();
        return;
    }

    if (currentStatus.equalsIgnoreCase("INTERVIEW_SCHEDULED")) {
        boolean ok = dao.confirmCompanyAction(applicationId); // DAO already sets to PENDING for interview scheduled
        if (ok) {
            JOptionPane.showMessageDialog(this, "Status confirmed: PENDING.");
            load(searchField.getText());
            table.clearSelection();
            updateButtonStates();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to confirm status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return;
    }

    if (currentStatus.equalsIgnoreCase("SHORTLISTED")) {
        boolean ok = dao.confirmCompanyAction(applicationId); // marks admin_confirmed
        if (ok) {
            JOptionPane.showMessageDialog(this, "Status confirmed: SHORTLISTED.");
            load(searchField.getText());
            table.clearSelection();
            updateButtonStates();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to confirm status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return;
    }

    JOptionPane.showMessageDialog(this,
            "Confirm Status is intended for: SHORTLISTED, REJECTED, INTERVIEW_SCHEDULED.");
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
    if (status == null || !status.equalsIgnoreCase("SHORTLISTED") || !isSelectedAdminConfirmed()) {
        JOptionPane.showMessageDialog(this, "Offer letters can only be uploaded for SHORTLISTED applicants.");
        return;
    }

    String companyName = getSelectedCompanyName();
    String jobTitle = getSelectedJobTitle();
    String applicantName = getSelectedStudentUsername(); // username for now (student profile page can show full name later)

    try {
        File offersDir = new File("data", "offers");
        if (!offersDir.exists()) offersDir.mkdirs();

        // Ensure a default template exists
        File templateFile = new File(offersDir, "default_offer_letter_template.txt");
        if (!templateFile.exists()) {
            String defaultTemplate =
                    "LETTER OF OFFER\n\n"
                            + "Company: {{COMPANY_NAME}}\n"
                            + "Application ID: {{APPLICATION_ID}}\n"
                            + "Applicant: {{APPLICANT_NAME}}\n"
                            + "Job Title: {{JOB_TITLE}}\n\n"
                            + "Dear {{APPLICANT_NAME}},\n\n"
                            + "We are pleased to offer you the position of {{JOB_TITLE}} at {{COMPANY_NAME}}.\n"
                            + "Please log in to the Job Placement System to review and accept your offer.\n\n"
                            + "Regards,\n"
                            + "{{COMPANY_NAME}}\n";
            Files.writeString(templateFile.toPath(), defaultTemplate);
        }

        String template = Files.readString(templateFile.toPath());

        String personalized = template
                .replace("{{COMPANY_NAME}}", companyName == null ? "" : companyName)
                .replace("{{APPLICATION_ID}}", String.valueOf(applicationId))
                .replace("{{APPLICANT_NAME}}", applicantName == null ? "" : applicantName)
                .replace("{{JOB_TITLE}}", jobTitle == null ? "" : jobTitle);

        // Create a per-application letter file (no file explorer)
        File outFile = new File(offersDir, "offer_" + applicationId + ".txt");
        Files.writeString(outFile.toPath(), personalized);

        // Create offer row in DB (PENDING) and attach letter path
        long offerId = dao.createOffer(applicationId, null, null, outFile.toString());
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
                                + "Company: " + (companyName == null ? "" : companyName) + "\n"
                                + "Job: " + (jobTitle == null ? "" : jobTitle) + "\n\n"
                                + "Please log in to the Job Placement System to view the offer and respond.\n"
                                + "Status: PENDING"
                );
            } catch (IllegalStateException missingCreds) {
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

        JOptionPane.showMessageDialog(this, "Offer letter generated and offer created (PENDING).");
        load(searchField.getText());
        table.clearSelection();
        updateButtonStates();

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Failed to generate offer letter: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
}

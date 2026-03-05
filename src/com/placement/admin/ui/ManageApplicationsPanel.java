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
    private static final int COL_OFFER_STATUS = 7;
    private static final int COL_ADMIN_CONFIRMED = 8;

    // Status constants (keep consistent across UI/DB)
    private static final String STATUS_SHORTLISTED = "SHORTLISTED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_INTERVIEW_SCHEDULED = "INTERVIEW_SCHEDULED";

    public ManageApplicationsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(AdminTheme.BG);

        add(createTopBar(), BorderLayout.NORTH);

        String[] cols = {
                "Application ID", "Job ID", "Company", "Job Title",
                "Student", "Student Email",
                "App Status",
                "Offer Status",
                "Admin Confirmed",
                "Applied"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        AdminTheme.styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        AdminTheme.styleScrollPane(sp);

        add(sp, BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

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

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            load(null);
        });
        AdminTheme.styleButton(refreshBtn, AdminTheme.MUTED_BUTTON);
        bar.add(refreshBtn);

        return bar;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bar.setBackground(AdminTheme.SURFACE);
        bar.setBorder(new EmptyBorder(10, 10, 10, 10));

        confirmStatusBtn = new JButton("Confirm Status");
        confirmStatusBtn.addActionListener(e -> confirmStatus());
        AdminTheme.styleButton(confirmStatusBtn, AdminTheme.ACCENT);
        bar.add(confirmStatusBtn);

        uploadOfferBtn = new JButton("Upload Offer Letter");
        uploadOfferBtn.setEnabled(false);
        uploadOfferBtn.addActionListener(e -> uploadOfferLetter());
        AdminTheme.styleButton(uploadOfferBtn, AdminTheme.MUTED_BUTTON);
        bar.add(uploadOfferBtn);

        Dimension btnSize = new Dimension(180, 30);
        confirmStatusBtn.setPreferredSize(btnSize);
        uploadOfferBtn.setPreferredSize(btnSize);

        updateButtonStates();
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
                    (r.offerStatus == null ? "" : r.offerStatus),
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

        String status = getSelectedStatus();
        boolean isConfirmed = isSelectedAdminConfirmed();

        boolean canConfirm = status != null && (
                status.equalsIgnoreCase(STATUS_SHORTLISTED)
                        || status.equalsIgnoreCase(STATUS_REJECTED)
                        || status.equalsIgnoreCase(STATUS_INTERVIEW_SCHEDULED)
        );

        // Confirm enabled only if confirmable AND not already confirmed
        if (confirmStatusBtn != null) confirmStatusBtn.setEnabled(canConfirm && !isConfirmed);

        // Upload enabled only if shortlisted AND admin confirmed
        boolean isShortlisted = status != null && status.equalsIgnoreCase(STATUS_SHORTLISTED);
        if (uploadOfferBtn != null) uploadOfferBtn.setEnabled(isShortlisted && isConfirmed);
    }

    private boolean isSelectedAdminConfirmed() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return false;

        String val = String.valueOf(tableModel.getValueAt(selectedRow, COL_ADMIN_CONFIRMED));
        return val != null && val.equalsIgnoreCase("YES");
    }

    private long getSelectedApplicationId() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an application.");
            return -1;
        }

        Object v = tableModel.getValueAt(selectedRow, COL_APPLICATION_ID);
        if (v instanceof Number) return ((Number) v).longValue();

        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid Application ID in table.", "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
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

    @SuppressWarnings("unused")
    private String getSelectedOfferStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) return null;
        return String.valueOf(tableModel.getValueAt(selectedRow, COL_OFFER_STATUS));
    }

    /**
     * Confirm company status for selected application.
     * Rules:
     * - REJECTED: set rejected in DB and email student.
     * - INTERVIEW_SCHEDULED: DAO confirms and sets application status to PENDING.
     * - SHORTLISTED: mark admin_confirmed (enables upload offer).
     */
    private void confirmStatus() {
        long applicationId = getSelectedApplicationId();
        if (applicationId < 0) return;

        String currentStatus = getSelectedStatus();
        if (currentStatus == null || currentStatus.isBlank()) return;

        String msg;
        if (currentStatus.equalsIgnoreCase(STATUS_REJECTED)) {
            msg = "Confirm REJECTION for Application #" + applicationId + "?\n"
                    + "This will set status to REJECTED and send a rejection email.";
        } else if (currentStatus.equalsIgnoreCase(STATUS_INTERVIEW_SCHEDULED)) {
            msg = "Confirm INTERVIEW_SCHEDULED for Application #" + applicationId + "?\n"
                    + "This will set the application status to PENDING.";
        } else if (currentStatus.equalsIgnoreCase(STATUS_SHORTLISTED)) {
            msg = "Confirm SHORTLISTED for Application #" + applicationId + "?\n"
                    + "After confirmation, Upload Offer Letter will be enabled.";
        } else {
            JOptionPane.showMessageDialog(this,
                    "Confirm Status is only for: SHORTLISTED, REJECTED, INTERVIEW_SCHEDULED.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                msg,
                "Confirm Status",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) return;

        // If REJECTED: behave like Decline (DB + email)
        if (currentStatus.equalsIgnoreCase(STATUS_REJECTED)) {
            boolean ok = dao.declineApplicant(applicationId);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Failed to reject applicant.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            sendRejectionEmail(applicationId);
            JOptionPane.showMessageDialog(this, "Status confirmed: REJECTED.");

            reloadAndResetSelection();
            return;
        }

        // INTERVIEW_SCHEDULED or SHORTLISTED: DAO handles admin_confirmed and/or PENDING logic
        boolean ok = dao.confirmCompanyAction(applicationId);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to confirm status.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentStatus.equalsIgnoreCase(STATUS_INTERVIEW_SCHEDULED)) {
            JOptionPane.showMessageDialog(this, "Status confirmed: PENDING.");
        } else {
            JOptionPane.showMessageDialog(this, "Status confirmed: SHORTLISTED.");
        }

        reloadAndResetSelection();
    }

    private void sendRejectionEmail(long applicationId) {
        String to = getSelectedStudentEmail();
        if (to == null || to.isBlank()) return;

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

    /**
     * Upload offer letter only when:
     * - App status = SHORTLISTED
     * - Admin confirmed = YES
     */
    private void uploadOfferLetter() {
        long applicationId = getSelectedApplicationId();
        if (applicationId < 0) return;

        String status = getSelectedStatus();
        if (status == null || !status.equalsIgnoreCase(STATUS_SHORTLISTED) || !isSelectedAdminConfirmed()) {
            JOptionPane.showMessageDialog(this, "Offer letters can only be uploaded for SHORTLISTED applicants.");
            return;
        }

        String companyName = getSelectedCompanyName();
        String jobTitle = getSelectedJobTitle();
        String applicantName = getSelectedStudentUsername();

        try {
            File offersDir = new File("data", "offers");
            if (!offersDir.exists()) offersDir.mkdirs();

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

            File outFile = new File(offersDir, "offer_" + applicationId + ".txt");
            Files.writeString(outFile.toPath(), personalized);

            long offerId = dao.createOffer(applicationId, null, null, outFile.toString());
            if (offerId <= 0) {
                JOptionPane.showMessageDialog(this, "Failed to save offer in database.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            sendOfferEmail(applicationId, companyName, jobTitle);

            JOptionPane.showMessageDialog(this, "Offer letter generated and offer created (PENDING).");
            reloadAndResetSelection();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to generate offer letter: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendOfferEmail(long applicationId, String companyName, String jobTitle) {
        String to = getSelectedStudentEmail();
        if (to == null || to.isBlank()) return;

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

    private void reloadAndResetSelection() {
        load(searchField.getText());
        table.clearSelection();
        updateButtonStates();
    }
}
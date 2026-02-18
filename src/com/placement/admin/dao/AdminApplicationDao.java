package com.placement.admin.dao;

import com.placement.common.db.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminApplicationDao {

    public static class ApplicationRow {
        public long applicationId;
        public long jobId;
        public int studentId;
        public String studentUsername;
        public String studentEmail;
        public String companyName;
        public String jobTitle;

        // Company-driven status stored in applications.status
        public String status;

        // Admin confirmation flags
        public int adminConfirmed;
        public String adminConfirmedAt;

        // Optional related info
        public String interviewScheduledAt; // from interviews table (latest)
        public String offerStatus;          // from offers table (latest)
        public String offerIssuedAt;        // from offers table (latest)
        public String offerLetterPath;      // offers.letter_path (latest)

        public String appliedAt;
    }

    public List<ApplicationRow> listAll(String keyword) {

        String base = """
            SELECT
              a.application_id,
              a.job_id,
              a.student_id,
              u.username AS student_username,
              u.email AS student_email,
              j.company_name,
              j.title AS job_title,
              a.status,
              a.applied_at,
              a.admin_confirmed,
              a.admin_confirmed_at,

              -- Latest interview scheduled time (if any)
              (
                SELECT i.scheduled_at
                FROM interviews i
                WHERE i.application_id = a.application_id
                ORDER BY i.interview_id DESC
                LIMIT 1
              ) AS interview_scheduled_at,

              -- Latest offer fields (if any)
              (
                SELECT o.status
                FROM offers o
                WHERE o.application_id = a.application_id
                ORDER BY o.offer_id DESC
                LIMIT 1
              ) AS offer_status,

              (
                SELECT o.issued_at
                FROM offers o
                WHERE o.application_id = a.application_id
                ORDER BY o.offer_id DESC
                LIMIT 1
              ) AS offer_issued_at,

              (
                SELECT o.letter_path
                FROM offers o
                WHERE o.application_id = a.application_id
                ORDER BY o.offer_id DESC
                LIMIT 1
              ) AS offer_letter_path

            FROM applications a
            LEFT JOIN users u ON u.id = a.student_id
            LEFT JOIN job_listings j ON j.job_id = a.job_id
        """;

        String where = "";
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            where = " WHERE (u.username LIKE ? OR u.email LIKE ? OR j.company_name LIKE ? OR j.title LIKE ? OR a.status LIKE ?)";
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        String sql = base + where + " ORDER BY a.application_id DESC";

        List<ApplicationRow> out = new ArrayList<>();
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ApplicationRow r = new ApplicationRow();
                    r.applicationId = rs.getLong("application_id");
                    r.jobId = rs.getLong("job_id");
                    r.studentId = rs.getInt("student_id");
                    r.studentUsername = rs.getString("student_username");
                    r.studentEmail = rs.getString("student_email");
                    r.companyName = rs.getString("company_name");
                    r.jobTitle = rs.getString("job_title");
                    r.status = rs.getString("status");
                    r.appliedAt = rs.getString("applied_at");

                    r.adminConfirmed = rs.getInt("admin_confirmed");
                    r.adminConfirmedAt = rs.getString("admin_confirmed_at");

                    r.interviewScheduledAt = rs.getString("interview_scheduled_at");
                    r.offerStatus = rs.getString("offer_status");
                    r.offerIssuedAt = rs.getString("offer_issued_at");
                    r.offerLetterPath = rs.getString("offer_letter_path");

                    out.add(r);
                }
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("List applications failed: " + e.getMessage(), e);
        }
    }

    public boolean updateStatus(long applicationId, String newStatus) {
        String sql = "UPDATE applications SET status=? WHERE application_id=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setLong(2, applicationId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Update application status failed: " + e.getMessage(), e);
        }
    }

    /**
     * Admin confirms whatever company has done (shortlisted/rejected/interview scheduled).
     * - Marks admin_confirmed=1 and admin_confirmed_at=now.
     * - If the status is INTERVIEW_SCHEDULED, per requirement we set application status to PENDING.
     */
    public boolean confirmCompanyAction(long applicationId) {
        try (Connection con = DB.getConnection()) {

            String currentStatus = null;
            try (PreparedStatement ps = con.prepareStatement("SELECT status FROM applications WHERE application_id=?")) {
                ps.setLong(1, applicationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) currentStatus = rs.getString(1);
                }
            }

            if (currentStatus != null && currentStatus.equalsIgnoreCase("INTERVIEW_SCHEDULED")) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE applications SET status='PENDING', admin_confirmed=1, admin_confirmed_at=datetime('now') WHERE application_id=?")) {
                    ps.setLong(1, applicationId);
                    return ps.executeUpdate() == 1;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE applications SET admin_confirmed=1, admin_confirmed_at=datetime('now') WHERE application_id=?")) {
                ps.setLong(1, applicationId);
                return ps.executeUpdate() == 1;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Confirm company action failed: " + e.getMessage(), e);
        }
    }

    /**
     * Admin declines/rejects an applicant (single button).
     * Also marks admin_confirmed=1.
     */
    public boolean declineApplicant(long applicationId) {
        String sql = "UPDATE applications SET status='REJECTED', admin_confirmed=1, admin_confirmed_at=datetime('now') WHERE application_id=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, applicationId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Decline applicant failed: " + e.getMessage(), e);
        }
    }

    /**
     * Admin uploads an offer letter for a shortlisted applicant.
     * Creates an offer row with PENDING status and attaches letter_path.
     * Also sets the application status to PENDING and marks admin_confirmed=1.
     */
    public long createOffer(long applicationId, Double packageLpa, String joiningDate, String letterPath) {
        try (Connection con = DB.getConnection()) {
            con.setAutoCommit(false);

            long offerId = -1;

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO offers(application_id, package_lpa, joining_date, status, letter_path) VALUES(?, ?, ?, 'PENDING', ?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                ps.setLong(1, applicationId);
                if (packageLpa == null) ps.setNull(2, Types.REAL); else ps.setDouble(2, packageLpa);
                ps.setString(3, joiningDate);
                ps.setString(4, letterPath);

                int rows = ps.executeUpdate();
                if (rows != 1) throw new SQLException("Offer insert failed");

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    offerId = keys.next() ? keys.getLong(1) : -1;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE applications SET status='PENDING', admin_confirmed=1, admin_confirmed_at=datetime('now') WHERE application_id=?")) {
                ps.setLong(1, applicationId);
                ps.executeUpdate();
            }

            con.commit();
            return offerId;

        } catch (SQLException e) {
            throw new RuntimeException("Create offer failed: " + e.getMessage(), e);
        }
    }
}

package com.placement.company.dao;

import com.placement.common.db.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompanyApplicationDao {

    public static class ApplicationRow {
        public long applicationId;
        public long studentId;
        public String studentName;
        public String email;
        public Double gpa;
        public Integer yearOfStudy;
        public String status;
        public String appliedAt;
        public String resumePath;
    }

    public List<ApplicationRow> listApplicantsForJob(String companyName, long jobId) {
        String sql =
                "SELECT\n" +
                "  a.application_id,\n" +
                "  a.student_id,\n" +
                "  a.status,\n" +
                "  a.applied_at,\n" +
                "  a.resume_path,\n" +
                "  u.email,\n" +
                "  s.first_name,\n" +
                "  s.last_name,\n" +
                "  ad.gpa,\n" +
                "  ad.year_of_study\n" +
                "FROM applications a\n" +
                "JOIN job_listings j ON a.job_id = j.job_id\n" +
                "JOIN users u ON a.student_id = u.id\n" +
                "LEFT JOIN students s ON s.user_id = u.id\n" +
                "LEFT JOIN academic_details ad ON ad.student_id = u.id\n" +
                "WHERE j.company_name = ? AND a.job_id = ?\n" +
                "ORDER BY a.applied_at DESC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, companyName);
            ps.setLong(2, jobId);

            try (ResultSet rs = ps.executeQuery()) {
                List<ApplicationRow> out = new ArrayList<>();
                while (rs.next()) {
                    ApplicationRow r = new ApplicationRow();
                    r.applicationId = rs.getLong("application_id");
                    r.studentId = rs.getLong("student_id");
                    r.status = rs.getString("status");
                    r.appliedAt = rs.getString("applied_at");
                    r.resumePath = rs.getString("resume_path");

                    String fn = rs.getString("first_name");
                    String ln = rs.getString("last_name");
                    String name = ((fn == null ? "" : fn) + " " + (ln == null ? "" : ln)).trim();
                    r.studentName = name.isEmpty() ? ("Student #" + r.studentId) : name;

                    r.email = rs.getString("email");

                    Object g = rs.getObject("gpa");
                    r.gpa = (g == null) ? null : ((Number) g).doubleValue();

                    Object y = rs.getObject("year_of_study");
                    r.yearOfStudy = (y == null) ? null : ((Number) y).intValue();

                    out.add(r);
                }
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("List applicants failed: " + e.getMessage(), e);
        }
    }

    public String getApplicationStatus(long applicationId) {
        String sql = "SELECT status FROM applications WHERE application_id=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Get application status failed: " + e.getMessage(), e);
        }
    }

    public boolean updateApplicationStatus(long applicationId, String newStatus) {
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

    private boolean interviewExists(long applicationId) {
        String sql = "SELECT 1 FROM interviews WHERE application_id=? LIMIT 1";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Interview check failed: " + e.getMessage(), e);
        }
    }

    public long scheduleInterview(long applicationId,
                                 String scheduledAt,
                                 String mode,
                                 String location,
                                 String meetingLink,
                                 String notes) {

        String current = getApplicationStatus(applicationId);
        if (current != null && current.equalsIgnoreCase("REJECTED")) {
            throw new RuntimeException("Cannot schedule interview for a rejected applicant.");
        }

        // Prevent scheduling twice for the same application
        if (interviewExists(applicationId)) {
            throw new RuntimeException("An interview is already scheduled for this application.");
        }

        String m = mode == null ? "" : mode.trim();
        boolean isOnline = m.equalsIgnoreCase("Online");

        // Normalize inputs & enforce consistency:
        // - Online: meeting_link required, location MUST be NULL
        // - Face-to-face: location required, meeting_link MUST be NULL
        if (isOnline) {
            meetingLink = meetingLink == null ? null : meetingLink.trim();
            if (meetingLink == null || meetingLink.trim().isEmpty()) {
                throw new RuntimeException("Meeting link is required for Online interviews.");
            }
            location = null;
        } else {
            location = location == null ? null : location.trim();
            if (location == null || location.trim().isEmpty()) {
                throw new RuntimeException("Office location is required for Face-to-face interviews.");
            }
            meetingLink = null;
        }

        notes = notes == null ? null : notes.trim();
        if (notes != null && notes.isEmpty()) notes = null;

        String sql =
                "INSERT INTO interviews (application_id, scheduled_at, mode, location, meeting_link, status, notes) " +
                "VALUES (?, ?, ?, ?, ?, 'SCHEDULED', ?)";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, applicationId);
            ps.setString(2, scheduledAt);
            ps.setString(3, mode);
            ps.setString(4, location);   // NULL when online
            ps.setString(5, meetingLink); // NULL when face-to-face
            ps.setString(6, notes);

            int rows = ps.executeUpdate();
            if (rows != 1) return -1;

            updateApplicationStatus(applicationId, "INTERVIEW_SCHEDULED");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }

        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("unique") || msg.contains("constraint")) {
                throw new RuntimeException("An interview is already scheduled for this application.", e);
            }
            throw new RuntimeException("Schedule interview failed: " + e.getMessage(), e);
        }
    }
}

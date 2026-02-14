package com.placement.admin.dao;

import com.placement.common.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        public String status;
        public String appliedAt;
    }

    public List<ApplicationRow> listAll(String keyword) {
        String base = """
            SELECT a.application_id,
                   a.job_id,
                   a.student_id,
                   u.username AS student_username,
                   u.email AS student_email,
                   j.company_name,
                   j.title AS job_title,
                   a.status,
                   a.applied_at
            FROM applications a
            LEFT JOIN users u ON u.id = a.student_id
            LEFT JOIN job_listings j ON j.job_id = a.job_id
            WHERE 1=1
        """;

        boolean hasFilter = keyword != null && !keyword.trim().isEmpty();
        String filter = hasFilter ? """
            AND (
                u.username LIKE ? OR u.email LIKE ?
                OR j.company_name LIKE ? OR j.title LIKE ?
                OR a.status LIKE ?
            )
        """ : "";

        String sql = base + filter + " ORDER BY a.applied_at DESC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (hasFilter) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
                ps.setString(4, like);
                ps.setString(5, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<ApplicationRow> out = new ArrayList<>();
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
                    out.add(r);
                }
                return out;
            }

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
}

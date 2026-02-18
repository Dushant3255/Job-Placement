package com.placement.student.dao;

import com.placement.common.db.DB;
import com.placement.student.model.Application;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAOImpl implements ApplicationDAO {

    @Override
    public long apply(long studentId, long jobId) {
        // ✅ Use APPLIED (matches company/admin side)
        // ✅ Save resume_path automatically from student_documents (if available)
        String sql = "INSERT INTO applications (student_id, job_id, status, resume_path) VALUES (?,?, 'APPLIED', ?)";

        try (Connection con = DB.getConnection()) {

            String cvPath = getStudentCvPath(con, studentId);

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, studentId);
                ps.setLong(2, jobId);
                ps.setString(3, cvPath); // can be null
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
                return -1;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Apply failed (maybe already applied): " + e.getMessage(), e);
        }
    }

    @Override
    public List<Application> getByStudent(long studentId) {
        // ✅ Join job_listings to fetch company + job title for Student UI tables
        String sql =
                "SELECT a.*, " +
                "       j.company_name AS company_name, " +
                "       j.title AS job_title " +
                "FROM applications a " +
                "JOIN job_listings j ON j.job_id = a.job_id " +
                "WHERE a.student_id=? " +
                "ORDER BY a.applied_at DESC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Application> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Get applications failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean withdraw(long applicationId, long studentId) {
        String sql = "UPDATE applications SET status='WITHDRAWN' WHERE application_id=? AND student_id=?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, applicationId);
            ps.setLong(2, studentId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Withdraw failed: " + e.getMessage(), e);
        }
    }

    // ----------------- helpers -----------------

    private String getStudentCvPath(Connection con, long studentId) throws SQLException {
        String sql = "SELECT cv_path FROM student_documents WHERE student_id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("cv_path") : null;
            }
        }
    }

    private Application map(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setApplicationId(rs.getLong("application_id"));
        a.setStudentId(rs.getLong("student_id"));
        a.setJobId(rs.getLong("job_id"));

        // ✅ SQLite may store datetime as TEXT, so parse safely
        a.setAppliedAt(readTimestampSafe(rs, "applied_at"));

        a.setStatus(rs.getString("status"));

        // ✅ these fields were added in Step 6
        try { a.setCompanyName(rs.getString("company_name")); } catch (SQLException ignore) {}
        try { a.setJobTitle(rs.getString("job_title")); } catch (SQLException ignore) {}

        return a;
    }

    private Timestamp readTimestampSafe(ResultSet rs, String col) {
        try {
            Timestamp ts = rs.getTimestamp(col);
            if (ts != null) return ts;
        } catch (SQLException ignore) {}

        try {
            String s = rs.getString(col);
            if (s == null || s.isBlank()) return null;

            // Common SQLite datetime('now') format: "YYYY-MM-DD HH:MM:SS"
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse(s.trim(), f);
            return Timestamp.valueOf(ldt);
        } catch (Exception ignore) {
            return null;
        }
    }
}

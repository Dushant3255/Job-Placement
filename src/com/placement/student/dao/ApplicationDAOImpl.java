package com.placement.student.dao;

import com.placement.student.model.Application;
import com.placement.common.db.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAOImpl implements ApplicationDAO {

    @Override
    public long apply(long studentId, long jobId) {
        String sql = "INSERT INTO applications (student_id, job_id, status) VALUES (?,?, 'SUBMITTED')";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, studentId);
            ps.setLong(2, jobId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return -1;

        } catch (SQLException e) {
            throw new RuntimeException("Apply failed (maybe already applied): " + e.getMessage(), e);
        }
    }

    @Override
    public List<Application> getByStudent(long studentId) {
        String sql = "SELECT * FROM applications WHERE student_id=? ORDER BY applied_at DESC";

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

    private Application map(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setApplicationId(rs.getLong("application_id"));
        a.setStudentId(rs.getLong("student_id"));
        a.setJobId(rs.getLong("job_id"));
        a.setAppliedAt(rs.getTimestamp("applied_at"));
        a.setStatus(rs.getString("status"));
        return a;
    }
}
package com.placement.student.dao;

import com.placement.common.db.DB;
import com.placement.student.model.OffCampusJob;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffCampusJobDAOImpl implements OffCampusJobDAO {

    @Override
    public long add(OffCampusJob job) throws SQLException {
        String sql = """
            INSERT INTO off_campus_jobs (student_id, company_name, role_title, applied_date, status, notes)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, job.getStudentId());
            ps.setString(2, job.getCompanyName());
            ps.setString(3, job.getRoleTitle());
            ps.setString(4, job.getAppliedDate());
            ps.setString(5, job.getStatus());
            ps.setString(6, job.getNotes());

            int rows = ps.executeUpdate();
            if (rows == 0) return 0;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            return 0;
        }
    }

    @Override
    public List<OffCampusJob> getByStudent(long studentId) throws SQLException {
        List<OffCampusJob> list = new ArrayList<>();
        String sql = "SELECT * FROM off_campus_jobs WHERE student_id = ? ORDER BY offcampus_id DESC";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OffCampusJob o = new OffCampusJob();
                    o.setOffCampusId(rs.getLong("offcampus_id"));
                    o.setStudentId(rs.getLong("student_id"));
                    o.setCompanyName(rs.getString("company_name"));
                    o.setRoleTitle(rs.getString("role_title"));
                    o.setAppliedDate(rs.getString("applied_date"));
                    o.setStatus(rs.getString("status"));
                    o.setNotes(rs.getString("notes"));
                    list.add(o);
                }
            }
        }

        return list;
    }

    @Override
    public boolean update(OffCampusJob job) throws SQLException {
        String sql = """
            UPDATE off_campus_jobs
            SET company_name = ?, role_title = ?, applied_date = ?, status = ?, notes = ?
            WHERE offcampus_id = ? AND student_id = ?
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, job.getCompanyName());
            ps.setString(2, job.getRoleTitle());
            ps.setString(3, job.getAppliedDate());
            ps.setString(4, job.getStatus());
            ps.setString(5, job.getNotes());
            ps.setLong(6, job.getOffCampusId());
            ps.setLong(7, job.getStudentId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(long offcampusId, long studentId) throws SQLException {
        String sql = "DELETE FROM off_campus_jobs WHERE offcampus_id = ? AND student_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, offcampusId);
            ps.setLong(2, studentId);

            return ps.executeUpdate() > 0;
        }
    }
}

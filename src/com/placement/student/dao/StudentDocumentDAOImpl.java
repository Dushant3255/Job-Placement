package com.placement.student.dao;

import com.placement.common.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDocumentDAOImpl implements StudentDocumentDAO {

    @Override
    public String getCvPath(long studentId) {
        String sql = "SELECT cv_path FROM student_documents WHERE student_id=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("cv_path") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Get CV failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveOrUpdateCvPath(long studentId, String cvPath) {
        String sql =
                "INSERT INTO student_documents(student_id, cv_path, updated_at) VALUES(?, ?, datetime('now')) " +
                "ON CONFLICT(student_id) DO UPDATE SET cv_path=excluded.cv_path, updated_at=datetime('now')";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setString(2, cvPath);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Save CV failed: " + e.getMessage(), e);
        }
    }
}

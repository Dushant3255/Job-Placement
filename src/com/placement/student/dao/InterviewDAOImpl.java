package com.placement.student.dao;

import com.placement.student.model.Interview;
import com.placement.common.db.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InterviewDAOImpl implements InterviewDAO {

    @Override
    public List<Interview> getInterviewsForStudent(long studentId) {
        String sql =
            "SELECT i.* FROM interviews i " +
            "JOIN applications a ON a.application_id = i.application_id " +
            "WHERE a.student_id=? " +
            "ORDER BY i.scheduled_at ASC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Interview> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Get interviews failed: " + e.getMessage(), e);
        }
    }

    private Interview map(ResultSet rs) throws SQLException {
        Interview i = new Interview();
        i.setInterviewId(rs.getLong("interview_id"));
        i.setApplicationId(rs.getLong("application_id"));
        i.setScheduledAt(rs.getTimestamp("scheduled_at"));
        i.setMeetingLink(rs.getString("meeting_link"));
        i.setMode(rs.getString("mode"));
        i.setStatus(rs.getString("status"));
        return i;
    }
}
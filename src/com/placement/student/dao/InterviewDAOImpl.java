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

        // ✅ SQLite stores scheduled_at as TEXT; parse to Timestamp safely
        i.setScheduledAt(readTimestampSafe(rs, "scheduled_at"));

        i.setMeetingLink(rs.getString("meeting_link"));
        i.setMode(rs.getString("mode"));
        i.setStatus(rs.getString("status"));
        return i;
    }

    private Timestamp readTimestampSafe(ResultSet rs, String col) throws SQLException {
        // Try JDBC timestamp first (works if stored as real timestamp)
        try {
            Timestamp ts = rs.getTimestamp(col);
            if (ts != null) return ts;
        } catch (SQLException ignore) {}

        // Fallback: parse TEXT
        String s = rs.getString(col);
        if (s == null || s.isBlank()) return null;

        s = s.trim();

        // Handle common SQLite formats:
        // "yyyy-MM-dd HH:mm:ss"
        // "yyyy-MM-dd HH:mm:ss.SSS"
        // "yyyy-MM-ddTHH:mm:ss"
        // "yyyy-MM-ddTHH:mm:ss.SSS"
        s = s.replace('T', ' ');

        // If string has no seconds (rare), add ":00"
        if (s.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
            s = s + ":00";
        }

        try {
            return Timestamp.valueOf(s);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

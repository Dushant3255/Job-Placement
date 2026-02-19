package com.placement.company.dao;

import com.placement.common.db.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompanyInterviewDao {

    public static class MeetingRow {
        public long interviewId;
        public long applicationId;
        public long jobId;
        public String jobTitle;

        public long studentId;
        public String studentName;
        public String studentEmail;

        public String scheduledAt;
        public String mode;
        public String status;
        public String meetingLink;
        public String location;
        public String notes;
    }

    /**
     * List company interviews (scheduled meetings) across all jobs.
     * If statusFilter is null/blank or "All", no status filter is applied.
     */
    public List<MeetingRow> listMeetings(String companyName, String statusFilter) {
        boolean filter = !isBlank(statusFilter) && !"All".equalsIgnoreCase(statusFilter);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
           .append(" i.interview_id, ")
           .append(" i.application_id, ")
           .append(" i.scheduled_at, ")
           .append(" i.mode, ")
           .append(" i.status, ")
           .append(" i.meeting_link, ")
           .append(" i.location, ")
           .append(" i.notes, ")
           .append(" j.job_id, ")
           .append(" j.title AS job_title, ")
           .append(" a.student_id, ")
           .append(" u.email, ")
           .append(" s.first_name, ")
           .append(" s.last_name ")
           .append("FROM interviews i ")
           .append("JOIN applications a ON a.application_id = i.application_id ")
           .append("JOIN job_listings j ON j.job_id = a.job_id ")
           .append("JOIN users u ON u.id = a.student_id ")
           .append("LEFT JOIN students s ON s.user_id = u.id ")
           .append("WHERE j.company_name = ? ");

        if (filter) sql.append(" AND i.status = ? ");
        sql.append(" ORDER BY i.scheduled_at ASC");

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            ps.setString(1, companyName);
            if (filter) ps.setString(2, statusFilter.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                List<MeetingRow> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("List meetings failed: " + e.getMessage(), e);
        }
    }

    private MeetingRow map(ResultSet rs) throws SQLException {
        MeetingRow r = new MeetingRow();
        r.interviewId = rs.getLong("interview_id");
        r.applicationId = rs.getLong("application_id");
        r.scheduledAt = rs.getString("scheduled_at");
        r.mode = rs.getString("mode");
        r.status = rs.getString("status");
        r.meetingLink = rs.getString("meeting_link");
        r.location = rs.getString("location");
        r.notes = rs.getString("notes");

        r.jobId = rs.getLong("job_id");
        r.jobTitle = rs.getString("job_title");

        r.studentId = rs.getLong("student_id");
        r.studentEmail = rs.getString("email");

        String fn = rs.getString("first_name");
        String ln = rs.getString("last_name");
        String name = ((fn == null ? "" : fn) + " " + (ln == null ? "" : ln)).trim();
        r.studentName = isBlank(name) ? ("Student #" + r.studentId) : name;
        return r;
    }

    /** Mark a meeting's status, e.g. to COMPLETED. */
    public boolean updateMeetingStatus(long interviewId, String newStatus) {
        if (isBlank(newStatus)) throw new IllegalArgumentException("Status cannot be empty");
        String status = newStatus.trim().toUpperCase();
        String sql = "UPDATE interviews SET status=? WHERE interview_id=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, interviewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Update meeting status failed: " + e.getMessage(), e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

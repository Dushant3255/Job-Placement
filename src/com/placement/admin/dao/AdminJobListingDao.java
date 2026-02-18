package com.placement.admin.dao;

import com.placement.common.db.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminJobListingDao {

    public static class JobRow {
        public long jobId;
        public String companyName;
        public String title;
        public String department;
        public Double minGpa;
        public Integer minYear;
        public String status;
        public String postedAt;

        public int positionsAvailable;
        public int hiredCount;
    }

    public List<JobRow> listAll(String keyword) {
        String base = """
            SELECT job_id, company_name, title, department, min_gpa, min_year,
                   positions_available, hired_count,
                   status, posted_at
            FROM job_listings
            WHERE 1=1
        """;

        boolean hasFilter = keyword != null && !keyword.trim().isEmpty();
        String filter = hasFilter ? """
            AND (
                company_name LIKE ? OR title LIKE ? OR department LIKE ? OR status LIKE ?
            )
        """ : "";

        String sql = base + filter + " ORDER BY job_id DESC";

        List<JobRow> out = new ArrayList<>();
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (hasFilter) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
                ps.setString(4, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JobRow r = new JobRow();
                    r.jobId = rs.getLong("job_id");
                    r.companyName = rs.getString("company_name");
                    r.title = rs.getString("title");
                    r.department = rs.getString("department");
                    r.minGpa = (Double) rs.getObject("min_gpa");
                    r.minYear = (Integer) rs.getObject("min_year");
                    r.positionsAvailable = rs.getInt("positions_available");
                    r.hiredCount = rs.getInt("hired_count");
                    r.status = rs.getString("status");
                    r.postedAt = rs.getString("posted_at");
                    out.add(r);
                }
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("List job listings failed: " + e.getMessage(), e);
        }
    }

    public boolean updateStatus(long jobId, String newStatus) {
        String sql = "UPDATE job_listings SET status=? WHERE job_id=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setLong(2, jobId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Update job status failed: " + e.getMessage(), e);
        }
    }

    // Existing helper used by admin panel for off-campus integration (kept)
    public boolean insertOffCampusJob(
            String companyName,
            String title,
            String department,
            String description,
            Double minGpa,
            Integer minYear,
            int positionsAvailable
    ) {
        String sql = """
            INSERT INTO job_listings
            (company_name, title, department, description, min_gpa, min_year, eligibility_rule, status, positions_available, hired_count)
            VALUES (?, ?, ?, ?, ?, ?, 'OFF_CAMPUS', 'OPEN', ?, 0)
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, companyName);
            ps.setString(2, title);
            ps.setString(3, department);
            ps.setString(4, description);
            ps.setObject(5, minGpa);
            ps.setObject(6, minYear);
            ps.setInt(7, positionsAvailable);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Insert off campus job failed: " + e.getMessage(), e);
        }
    }
}

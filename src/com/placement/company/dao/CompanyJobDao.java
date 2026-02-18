package com.placement.company.dao;

import com.placement.common.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CompanyJobDao {

    public static class JobRow {
        public long jobId;
        public String title;
        public String department;
        public String description;
        public Double minGpa;
        public Integer minYear;
        public String eligibilityRule;
        public String status;
        
        public int positionsAvailable;
        public int hiredCount;
public String postedAt; // stored as TEXT in SQLite (datetime('now'))
    }

    public List<JobRow> listByCompanyName(String companyName) {
        String sql = """
            SELECT job_id, title, department, description,
                   min_gpa, min_year, eligibility_rule,
                   positions_available, hired_count,
                   status, posted_at
            FROM job_listings
            WHERE company_name = ?
            ORDER BY posted_at DESC
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, companyName);

            try (ResultSet rs = ps.executeQuery()) {
                List<JobRow> out = new ArrayList<>();
                while (rs.next()) {
                    JobRow r = new JobRow();
                    r.jobId = rs.getLong("job_id");
                    r.title = rs.getString("title");
                    r.department = rs.getString("department");
                    r.description = rs.getString("description");

                    Object g = rs.getObject("min_gpa");
                    r.minGpa = (g == null) ? null : ((Number) g).doubleValue();

                    Object y = rs.getObject("min_year");
                    r.minYear = (y == null) ? null : ((Number) y).intValue();

                    r.eligibilityRule = rs.getString("eligibility_rule");
                    r.status = rs.getString("status");

                    r.positionsAvailable = rs.getInt("positions_available");
                    r.hiredCount = rs.getInt("hired_count");
                    r.postedAt = rs.getString("posted_at");

                    out.add(r);
                }
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Company list jobs failed: " + e.getMessage(), e);
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
            throw new RuntimeException("Company update job status failed: " + e.getMessage(), e);
        }
    }

    // STEP 2.5: INSERT NEW JOB (Post Job dialog)
    public long insertJob(
            String companyName,
            String title,
            String department,
            String description,
            Double minGpa,
            Integer minYear,
            String eligibilityRule,
            int positionsAvailable
    ) {
        String sql = """
            INSERT INTO job_listings
            (company_name, title, department, description, min_gpa, min_year, eligibility_rule, positions_available, hired_count, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 'OPEN')
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, companyName);
            ps.setString(2, title);
            ps.setString(3, department);
            ps.setString(4, description);

            if (minGpa == null) ps.setNull(5, Types.REAL);
            else ps.setDouble(5, minGpa);

            if (minYear == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, minYear);

            ps.setString(7, (eligibilityRule == null || eligibilityRule.isBlank())
                    ? "GENERAL"
                    : eligibilityRule.trim()
            );

            int rows = ps.executeUpdate();
            if (rows != 1) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Insert job failed: " + e.getMessage(), e);
        }
    }

    // ✅ STEP (Edit Job): UPDATE JOB DETAILS
    public boolean updateJob(
            long jobId,
            String companyName,
            String title,
            String department,
            String description,
            Double minGpa,
            Integer minYear,
            String eligibilityRule,
            int positionsAvailable,
            String status
    ) {
        String sql = """
            UPDATE job_listings
            SET title=?,
                department=?,
                description=?,
                min_gpa=?,
                min_year=?,
                eligibility_rule=?,
                positions_available=?,
                status=?
            WHERE job_id=? AND company_name=?
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, department);
            ps.setString(3, description);

            if (minGpa == null) ps.setNull(4, Types.REAL);
            else ps.setDouble(4, minGpa);

            if (minYear == null) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, minYear);

            ps.setString(6, (eligibilityRule == null || eligibilityRule.isBlank())
                    ? "GENERAL"
                    : eligibilityRule.trim()
            );

            ps.setInt(7, positionsAvailable);
            ps.setString(8, (status == null || status.isBlank()) ? "OPEN" : status.trim().toUpperCase());
            ps.setLong(9, jobId);
            ps.setString(10, companyName);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Update job failed: " + e.getMessage(), e);
        }
    }

    public int countActiveJobs(String companyName) {
        String sql = "SELECT COUNT(*) FROM job_listings WHERE company_name=? AND status='OPEN'";
        return count(sql, companyName);
    }

    public int countTotalApplicants(String companyName) {
        String sql = """
            SELECT COUNT(*)
            FROM applications a
            JOIN job_listings j ON a.job_id = j.job_id
            WHERE j.company_name = ?
        """;
        return count(sql, companyName);
    }

    public int countOffersMade(String companyName) {
        String sql = """
            SELECT COUNT(*)
            FROM offers o
            JOIN applications a ON o.application_id = a.application_id
            JOIN job_listings j ON a.job_id = j.job_id
            WHERE j.company_name = ?
        """;
        return count(sql, companyName);
    }

    public int countPendingReviews(String companyName) {
        String sql = """
            SELECT COUNT(*)
            FROM applications a
            JOIN job_listings j ON a.job_id = j.job_id
            WHERE j.company_name = ? AND a.status='APPLIED'
        """;
        return count(sql, companyName);
    }

    private int count(String sql, String companyName) {
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, companyName);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Count failed: " + e.getMessage(), e);
        }
    }
}

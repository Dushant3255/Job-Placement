package student.dao;

import student.model.JobListing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobListingDAOImpl implements JobListingDAO {

    @Override
    public List<JobListing> getAllOpen() {
        String sql = "SELECT * FROM job_listings WHERE status='OPEN' ORDER BY posted_at DESC";
        return list(sql, ps -> {});
    }

    @Override
    public List<JobListing> searchByDepartment(String department) {
        String sql = "SELECT * FROM job_listings WHERE status='OPEN' AND department=? ORDER BY posted_at DESC";
        return list(sql, ps -> ps.setString(1, department));
    }

    @Override
    public List<JobListing> filterByEligibility(Double studentGpa, Integer studentYear) {
        String sql =
            "SELECT * FROM job_listings " +
            "WHERE status='OPEN' " +
            "AND (min_gpa IS NULL OR min_gpa <= ?) " +
            "AND (min_year IS NULL OR min_year <= ?) " +
            "ORDER BY posted_at DESC";
        return list(sql, ps -> {
            ps.setObject(1, studentGpa);
            ps.setObject(2, studentYear);
        });
    }

    @Override
    public JobListing findById(long jobId) {
        String sql = "SELECT * FROM job_listings WHERE job_id=?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, jobId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find job listing failed: " + e.getMessage(), e);
        }
    }

    private interface Binder { void bind(PreparedStatement ps) throws SQLException; }

    private List<JobListing> list(String sql, Binder binder) {
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<JobListing> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("List job listings failed: " + e.getMessage(), e);
        }
    }

    private JobListing map(ResultSet rs) throws SQLException {
        JobListing j = new JobListing();
        j.setJobId(rs.getLong("job_id"));
        j.setCompanyName(rs.getString("company_name"));
        j.setTitle(rs.getString("title"));
        j.setDepartment(rs.getString("department"));
        j.setDescription(rs.getString("description"));
        j.setMinGpa(rs.getBigDecimal("min_gpa") == null ? null : rs.getBigDecimal("min_gpa").doubleValue());
        j.setMinYear((Integer) rs.getObject("min_year"));
        j.setEligibilityRule(rs.getString("eligibility_rule"));
        j.setStatus(rs.getString("status"));
        j.setPostedAt(rs.getTimestamp("posted_at"));
        return j;
    }
}


package com.placement.company.dao;

import com.placement.common.db.DB;
import com.placement.company.model.CompanyProfile;

import java.sql.*;

public class CompanyDao {

    /** Returns company_name for user_id (used for rename propagation + delete). */
    public String getCompanyName(int userId) throws SQLException {
        String sql = "SELECT company_name FROM companies WHERE user_id=? LIMIT 1";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("company_name") : null;
            }
        }
    }

    public void createCompanyProfile(int userId, CompanyProfile profile) throws SQLException {
        String sql = """
            INSERT INTO companies (user_id, company_name, phone, website, industry, company_size, address)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, profile.getCompanyName());
            ps.setString(3, profile.getPhone());
            ps.setString(4, profile.getWebsite());
            ps.setString(5, profile.getIndustry());
            ps.setString(6, profile.getCompanySize());
            ps.setString(7, profile.getAddress());
            ps.executeUpdate();
        }
    }

    public CompanyProfile findByUserId(int userId) throws SQLException {
        String sql = """
            SELECT company_name, phone, website, industry, company_size, address
            FROM companies
            WHERE user_id = ?
            LIMIT 1
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new CompanyProfile(
                        rs.getString("company_name"),
                        rs.getString("phone"),
                        rs.getString("website"),
                        rs.getString("industry"),
                        rs.getString("company_size"),
                        rs.getString("address")
                );
            }
        }
    }

    public void updateCompanyProfile(int userId, CompanyProfile profile) throws SQLException {
        // IMPORTANT:
        // job_listings are linked by company_name TEXT.
        // If company name changes, propagate to job_listings.
        String oldName = getCompanyName(userId);
        String newName = profile.getCompanyName();

        try (Connection con = DB.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement("""
                UPDATE companies
                SET company_name = ?,
                    phone = ?,
                    website = ?,
                    industry = ?,
                    company_size = ?,
                    address = ?
                WHERE user_id = ?
            """)) {
                ps.setString(1, newName);
                ps.setString(2, profile.getPhone());
                ps.setString(3, profile.getWebsite());
                ps.setString(4, profile.getIndustry());
                ps.setString(5, profile.getCompanySize());
                ps.setString(6, profile.getAddress());
                ps.setInt(7, userId);
                ps.executeUpdate();
            }

            if (oldName != null && newName != null && !oldName.equals(newName)) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE job_listings SET company_name=? WHERE company_name=?")) {
                    ps.setString(1, newName);
                    ps.setString(2, oldName);
                    ps.executeUpdate();
                }
            }

            con.commit();
        }
    }

    /** Deletes company + all their jobs/applications/interviews/offers + user account. */
    public void deleteCompanyAccount(int userId) throws SQLException {
        String companyName = getCompanyName(userId);

        try (Connection con = DB.getConnection()) {
            con.setAutoCommit(false);

            if (companyName != null && !companyName.isBlank()) {
                try (PreparedStatement ps = con.prepareStatement("""
                    DELETE FROM interviews
                    WHERE application_id IN (
                        SELECT a.application_id
                        FROM applications a
                        JOIN job_listings j ON a.job_id = j.job_id
                        WHERE j.company_name = ?
                    )
                """)) {
                    ps.setString(1, companyName);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement("""
                    DELETE FROM offers
                    WHERE application_id IN (
                        SELECT a.application_id
                        FROM applications a
                        JOIN job_listings j ON a.job_id = j.job_id
                        WHERE j.company_name = ?
                    )
                """)) {
                    ps.setString(1, companyName);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement("""
                    DELETE FROM applications
                    WHERE job_id IN (
                        SELECT job_id FROM job_listings WHERE company_name = ?
                    )
                """)) {
                    ps.setString(1, companyName);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM job_listings WHERE company_name = ?")) {
                    ps.setString(1, companyName);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM companies WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM users WHERE id = ? AND role='COMPANY'")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            con.commit();
        }
    }

    public void updateLogoPath(int userId, String logoPath) throws SQLException {
        String sql = "UPDATE companies SET logo_path = ? WHERE user_id = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, logoPath);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public String getLogoPath(int userId) throws SQLException {
        String sql = "SELECT logo_path FROM companies WHERE user_id = ? LIMIT 1";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("logo_path") : null;
            }
        }
    }
}

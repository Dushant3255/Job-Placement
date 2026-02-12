package com.placement.company.dao;

import com.placement.common.db.DB;
import com.placement.company.model.CompanyProfile;

import java.sql.*;

public class CompanyDao {

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
        String sql = """
            UPDATE companies
            SET company_name = ?,
                phone = ?,
                website = ?,
                industry = ?,
                company_size = ?,
                address = ?
            WHERE user_id = ?
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, profile.getCompanyName());
            ps.setString(2, profile.getPhone());
            ps.setString(3, profile.getWebsite());
            ps.setString(4, profile.getIndustry());
            ps.setString(5, profile.getCompanySize());
            ps.setString(6, profile.getAddress());
            ps.setInt(7, userId);

            ps.executeUpdate();
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
                if (!rs.next()) return null;
                return rs.getString("logo_path");
            }
        }
    }
}

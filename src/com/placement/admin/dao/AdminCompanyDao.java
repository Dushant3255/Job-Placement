package com.placement.admin.dao;

import com.placement.common.db.DB;
import com.placement.common.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AdminCompanyDao {

    public static class CompanyRow {
        public int userId;
        public String username;
        public String email;
        public String companyName;
        public String phone;
        public String website;
        public String industry;
        public String companySize;
    }

    public List<CompanyRow> listAll(String keyword) {
        String base = """
            SELECT u.id AS user_id,
                   u.username,
                   u.email,
                   c.company_name,
                   c.phone,
                   c.website,
                   c.industry,
                   c.company_size
            FROM users u
            LEFT JOIN companies c ON c.user_id = u.id
            WHERE u.role = 'COMPANY'
        """;

        boolean hasFilter = keyword != null && !keyword.trim().isEmpty();
        String filter = hasFilter ? """
            AND (
                u.username LIKE ?
                OR u.email LIKE ?
                OR c.company_name LIKE ?
                OR c.industry LIKE ?
            )
        """ : "";

        String sql = base + filter + " ORDER BY u.id DESC";

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
                List<CompanyRow> out = new ArrayList<>();
                while (rs.next()) {
                    CompanyRow r = new CompanyRow();
                    r.userId = rs.getInt("user_id");
                    r.username = rs.getString("username");
                    r.email = rs.getString("email");
                    r.companyName = rs.getString("company_name");
                    r.phone = rs.getString("phone");
                    r.website = rs.getString("website");
                    r.industry = rs.getString("industry");
                    r.companySize = rs.getString("company_size");
                    out.add(r);
                }
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("List companies failed: " + e.getMessage(), e);
        }
    }


public int createCompanyAccount(String username,
                                String email,
                                String plainPassword,
                                String companyName,
                                String phone,
                                String website,
                                String industry,
                                String companySize,
                                String address) {
    if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
    if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
    if (plainPassword == null || plainPassword.isBlank()) throw new IllegalArgumentException("Password is required");
    if (companyName == null || companyName.isBlank()) throw new IllegalArgumentException("Company name is required");

    String un = username.trim();
    String em = email.trim();
    String hash = PasswordUtil.hash(plainPassword);

    try (Connection con = DB.getConnection()) {
        con.setAutoCommit(false);

        int userId;
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users(role, username, email, password_hash, is_verified) VALUES('COMPANY', ?, ?, ?, 1)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, un);
            ps.setString(2, em);
            ps.setString(3, hash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Failed to create company user");
                userId = keys.getInt(1);
            }
        }

        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO companies(user_id, company_name, phone, website, industry, company_size, address) VALUES(?, ?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, userId);
            ps.setString(2, companyName.trim());
            ps.setString(3, phone);
            ps.setString(4, website);
            ps.setString(5, industry);
            ps.setString(6, companySize);
            ps.setString(7, address);
            ps.executeUpdate();
        }

        con.commit();
        return userId;

    } catch (SQLException e) {
        throw new RuntimeException("Create company failed: " + e.getMessage(), e);
    }
}

public boolean deleteCompanyAccount(int companyUserId) {
    String sql = "DELETE FROM users WHERE id = ? AND role = 'COMPANY'";
    try (Connection con = DB.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, companyUserId);
        return ps.executeUpdate() == 1;

    } catch (SQLException e) {
        throw new RuntimeException("Delete company failed: " + e.getMessage(), e);
    }
}

public CompanyRow getCompanyByUserId(int companyUserId) {
    String sql = """
        SELECT u.id AS user_id,
               u.username,
               u.email,
               c.company_name,
               c.phone,
               c.website,
               c.industry,
               c.company_size
        FROM users u
        LEFT JOIN companies c ON c.user_id = u.id
        WHERE u.id = ? AND u.role = 'COMPANY'
        LIMIT 1
    """;

    try (Connection con = DB.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, companyUserId);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            CompanyRow r = new CompanyRow();
            r.userId = rs.getInt("user_id");
            r.username = rs.getString("username");
            r.email = rs.getString("email");
            r.companyName = rs.getString("company_name");
            r.phone = rs.getString("phone");
            r.website = rs.getString("website");
            r.industry = rs.getString("industry");
            r.companySize = rs.getString("company_size");
            return r;
        }
    } catch (SQLException e) {
        throw new RuntimeException("Fetch company failed: " + e.getMessage(), e);
    }
}
}

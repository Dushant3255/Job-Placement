package com.placement.admin.dao;

import com.placement.common.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}

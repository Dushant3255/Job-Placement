package com.placement.common.dao;

import com.placement.common.db.DB;
import com.placement.common.model.User;
import com.placement.common.model.UserRole;

import java.sql.*;

public class UserDao {

    public int createUser(UserRole role, String username, String email, String passwordHash, boolean verified) throws SQLException {
        String sql = """
            INSERT INTO users (role, username, email, password_hash, is_verified)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, role.name());
            ps.setString(2, username.trim());
            ps.setString(3, email.trim());
            ps.setString(4, passwordHash);
            ps.setInt(5, verified ? 1 : 0);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to create user (no generated key).");
    }

    /** Find user by username OR email (used for login and OTP resolve). */
    public User findByUsernameOrEmail(String login) throws SQLException {
        String sql = """
            SELECT id, role, username, email, is_verified
            FROM users
            WHERE username = ? OR email = ?
            LIMIT 1
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String v = login.trim();
            ps.setString(1, v);
            ps.setString(2, v);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new User(
                        rs.getInt("id"),
                        UserRole.valueOf(rs.getString("role")),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getInt("is_verified") == 1
                );
            }
        }
    }

    /** Convenience: find user by email only. */
    public User findByEmail(String email) throws SQLException {
        String sql = """
            SELECT id, role, username, email, is_verified
            FROM users
            WHERE email = ?
            LIMIT 1
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new User(
                        rs.getInt("id"),
                        UserRole.valueOf(rs.getString("role")),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getInt("is_verified") == 1
                );
            }
        }
    }

    /** You already had this (kept): gets password_hash by user id. */
    public String getPasswordHashByUserId(int userId) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getString("password_hash");
            }
        }
    }

    /** ✅ Needed for login: get password_hash using username OR email. */
    public String getPasswordHashByUsernameOrEmail(String login) throws SQLException {
        String sql = """
            SELECT password_hash
            FROM users
            WHERE username = ? OR email = ?
            LIMIT 1
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String v = login.trim();
            ps.setString(1, v);
            ps.setString(2, v);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getString("password_hash");
            }
        }
    }

    public void setVerifiedByEmail(String email, boolean verified) throws SQLException {
        String sql = "UPDATE users SET is_verified = ? WHERE email = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, verified ? 1 : 0);
            ps.setString(2, email.trim());
            ps.executeUpdate();
        }
    }
    
    public int updatePasswordHashByUserId(int userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate(); // should be 1
        }
    }
    
    public String getEmailByUserId(int userId) throws SQLException {
        String sql = "SELECT email FROM users WHERE id=?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("email") : null;
            }
        }
    }



}

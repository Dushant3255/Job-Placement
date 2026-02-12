package com.placement.common.dao;

import com.placement.common.db.DB;

import java.sql.*;

public class OtpDao {

    public void createOtp(String email, String purpose, String otpHash, String expiresAt) throws SQLException {
        String sql = """
            INSERT INTO otp_codes (email, purpose, otp_hash, expires_at)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ps.setString(2, purpose);
            ps.setString(3, otpHash);
            ps.setString(4, expiresAt);
            ps.executeUpdate();
        }
    }

    public OtpRow findLatestActive(String email, String purpose) throws SQLException {
        String sql = """
            SELECT id, otp_hash, attempts
            FROM otp_codes
            WHERE email = ?
              AND purpose = ?
              AND used_at IS NULL
              AND expires_at > datetime('now')
            ORDER BY id DESC
            LIMIT 1
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ps.setString(2, purpose);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new OtpRow(
                        rs.getInt("id"),
                        rs.getString("otp_hash"),
                        rs.getInt("attempts")
                );
            }
        }
    }

    public void markUsed(int id) throws SQLException {
        String sql = "UPDATE otp_codes SET used_at = datetime('now') WHERE id = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void incrementAttempts(int id) throws SQLException {
        String sql = "UPDATE otp_codes SET attempts = attempts + 1 WHERE id = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static class OtpRow {
        public final int id;
        public final String otpHash;
        public final int attempts;

        public OtpRow(int id, String otpHash, int attempts) {
            this.id = id;
            this.otpHash = otpHash;
            this.attempts = attempts;
        }
    }
}

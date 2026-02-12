package com.placement.student.dao;

import com.placement.common.db.DB;
import com.placement.student.model.StudentProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDao {

    public void createStudentProfile(int userId, StudentProfile profile) throws SQLException {
        String sql = """
            INSERT INTO students (user_id, first_name, last_name, gender, profile_image_path)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getGender());
            ps.setString(5, profile.getProfileImagePath()); // null ok
            ps.executeUpdate();
        }
    }

    public StudentProfile findByUserId(int userId) throws SQLException {
        String sql = """
            SELECT first_name, last_name, gender, profile_image_path
            FROM students
            WHERE user_id = ?
            LIMIT 1
        """;

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new StudentProfile(
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("gender"),
                        rs.getString("profile_image_path")
                );
            }
        }
    }

    public void updateProfileImagePath(int userId, String path) throws SQLException {
        String sql = "UPDATE students SET profile_image_path = ? WHERE user_id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, path);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}

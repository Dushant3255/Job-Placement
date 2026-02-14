package com.placement.admin.dao;

import com.placement.common.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminStudentDao {

    public static class StudentRow {
        public int userId;
        public String username;
        public String email;
        public String firstName;
        public String lastName;
        public String program;
        public Integer yearOfStudy;
        public Double gpa;
        public Double cgpa;

        public String fullName() {
            String fn = firstName == null ? "" : firstName;
            String ln = lastName == null ? "" : lastName;
            return (fn + " " + ln).trim();
        }
    }

    public List<StudentRow> listAll(String nameOrUsernameOrEmail) {
        String base = """
            SELECT u.id AS user_id,
                   u.username,
                   u.email,
                   s.first_name,
                   s.last_name,
                   a.program,
                   a.year_of_study,
                   a.gpa,
                   a.cgpa
            FROM users u
            LEFT JOIN students s ON s.user_id = u.id
            LEFT JOIN academic_details a ON a.student_id = u.id
            WHERE u.role = 'STUDENT'
        """;

        String filter = "";
        boolean hasFilter = nameOrUsernameOrEmail != null && !nameOrUsernameOrEmail.trim().isEmpty();
        if (hasFilter) {
            filter = """
                AND (
                    u.username LIKE ?
                    OR u.email LIKE ?
                    OR s.first_name LIKE ?
                    OR s.last_name LIKE ?
                )
            """;
        }

        String sql = base + filter + " ORDER BY u.id DESC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (hasFilter) {
                String like = "%" + nameOrUsernameOrEmail.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
                ps.setString(4, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<StudentRow> out = new ArrayList<>();
                while (rs.next()) {
                    StudentRow r = new StudentRow();
                    r.userId = rs.getInt("user_id");
                    r.username = rs.getString("username");
                    r.email = rs.getString("email");
                    r.firstName = rs.getString("first_name");
                    r.lastName = rs.getString("last_name");
                    r.program = rs.getString("program");
                    r.yearOfStudy = (Integer) rs.getObject("year_of_study");
                    r.gpa = (Double) rs.getObject("gpa");
                    r.cgpa = (Double) rs.getObject("cgpa");
                    out.add(r);
                }
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("List students failed: " + e.getMessage(), e);
        }
    }
}

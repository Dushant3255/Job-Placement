package com.placement.student.dao;

import com.placement.common.db.DB;
import com.placement.student.model.AcademicDetails;

import java.sql.*;

public class AcademicDetailsDAOImpl implements AcademicDetailsDAO {

    @Override
    public void addOrUpdate(long studentId, AcademicDetails details) throws SQLException {
        String sql = """
            INSERT INTO academic_details
                (student_id, program, year_of_study, gpa, cgpa, backlogs, graduation_year, eligibility_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(student_id) DO UPDATE SET
                program = excluded.program,
                year_of_study = excluded.year_of_study,
                gpa = excluded.gpa,
                cgpa = excluded.cgpa,
                backlogs = excluded.backlogs,
                graduation_year = excluded.graduation_year,
                eligibility_status = excluded.eligibility_status
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, studentId);
            ps.setString(2, details.getProgram());
            ps.setInt(3, details.getYearOfStudy());
            ps.setDouble(4, details.getGpa());
            ps.setDouble(5, details.getCgpa());
            ps.setInt(6, details.getBacklogs());
            ps.setInt(7, details.getGraduationYear());
            ps.setString(8, details.getEligibilityStatus());

            ps.executeUpdate();
        }
    }

    @Override
    public AcademicDetails getByStudentId(long studentId) throws SQLException {
        String sql = "SELECT * FROM academic_details WHERE student_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                AcademicDetails d = new AcademicDetails();
                d.setProgram(rs.getString("program"));
                d.setYearOfStudy(rs.getInt("year_of_study"));
                d.setGpa(rs.getDouble("gpa"));
                d.setCgpa(rs.getDouble("cgpa"));
                d.setBacklogs(rs.getInt("backlogs"));
                d.setGraduationYear(rs.getInt("graduation_year"));
                d.setEligibilityStatus(rs.getString("eligibility_status"));
                return d;
            }
        }
    }
}

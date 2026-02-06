package student.dao;

import student.model.AcademicDetails;

import java.sql.*;

public class AcademicDetailsDAOImpl implements AcademicDetailsDAO {

    @Override
    public AcademicDetails getByStudentId(long studentId) {
        String sql = "SELECT * FROM academic_details WHERE student_id=?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                AcademicDetails a = new AcademicDetails();
                a.setStudentId(rs.getLong("student_id"));
                a.setProgram(rs.getString("program"));
                a.setYearOfStudy((Integer) rs.getObject("year_of_study"));
                a.setGpa(rs.getBigDecimal("gpa") == null ? null : rs.getBigDecimal("gpa").doubleValue());
                a.setCgpa(rs.getBigDecimal("cgpa") == null ? null : rs.getBigDecimal("cgpa").doubleValue());
                a.setBacklogs((Integer) rs.getObject("backlogs"));
                a.setGraduationYear((Integer) rs.getObject("graduation_year"));
                a.setEligibilityStatus(rs.getString("eligibility_status"));
                return a;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Get academic details failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean upsert(AcademicDetails a) {
        String sql =
            "INSERT INTO academic_details (student_id, program, year_of_study, gpa, cgpa, backlogs, graduation_year, eligibility_status) " +
            "VALUES (?,?,?,?,?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE program=VALUES(program), year_of_study=VALUES(year_of_study), gpa=VALUES(gpa), cgpa=VALUES(cgpa), " +
            "backlogs=VALUES(backlogs), graduation_year=VALUES(graduation_year), eligibility_status=VALUES(eligibility_status)";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, a.getStudentId());
            ps.setString(2, a.getProgram());
            ps.setObject(3, a.getYearOfStudy());
            ps.setObject(4, a.getGpa());
            ps.setObject(5, a.getCgpa());
            ps.setObject(6, a.getBacklogs());
            ps.setObject(7, a.getGraduationYear());
            ps.setString(8, a.getEligibilityStatus());

            return ps.executeUpdate() >= 1;

        } catch (SQLException e) {
            throw new RuntimeException("Upsert academic details failed: " + e.getMessage(), e);
        }
    }
}
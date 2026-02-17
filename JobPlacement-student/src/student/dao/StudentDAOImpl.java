package student.dao;

import student.model.Student;

import java.sql.*;

public class StudentDAOImpl implements StudentDAO {

    @Override
    public long register(Student s, String rawPassword) {
        String sql = "INSERT INTO students (username,email,password_hash,first_name,last_name,gender,phone,department) " +
                     "VALUES (?,?,?,?,?,?,?)";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getUsername());
            ps.setString(2, s.getEmail());
            ps.setString(3, PasswordUtil.sha256(rawPassword));
            ps.setString(4, s.getFirstName());
            ps.setString(5, s.getLastName());
            ps.setString(6, s.getGender());
            ps.setString(7, s.getPhone());
            ps.setString(8, s.getDepartment());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return -1;

        } catch (SQLException e) {
            throw new RuntimeException("Register failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Student login(String usernameOrEmail, String rawPassword) {
        String sql = "SELECT * FROM students WHERE (username = ? OR email = ?) AND password_hash = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);
            ps.setString(3, PasswordUtil.sha256(rawPassword));

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapStudent(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Student findById(long studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapStudent(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find student failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateProfile(Student s) {
        String sql = "UPDATE students SET email=?, first_name=?, last_name=?, phone=?, department=? WHERE student_id=?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, s.getEmail());
            ps.setString(2, s.getFirstName());
            ps.setString(3, s.getLastName());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getDepartment());
            ps.setLong(6, s.getStudentId());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Update profile failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean changePassword(long studentId, String oldRawPassword, String newRawPassword) {
        String checkSql = "SELECT 1 FROM students WHERE student_id=? AND password_hash=?";
        String updateSql = "UPDATE students SET password_hash=? WHERE student_id=?";

        try (Connection con = DB.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setLong(1, studentId);
                check.setString(2, PasswordUtil.sha256(oldRawPassword));
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) { con.rollback(); return false; }
                }
            }

            try (PreparedStatement upd = con.prepareStatement(updateSql)) {
                upd.setString(1, PasswordUtil.sha256(newRawPassword));
                upd.setLong(2, studentId);
                boolean ok = upd.executeUpdate() == 1;
                con.commit();
                return ok;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Change password failed: " + e.getMessage(), e);
        }
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getLong("student_id"));
        s.setUsername(rs.getString("username"));
        s.setEmail(rs.getString("email"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setPhone(rs.getString("phone"));
        s.setDepartment(rs.getString("department"));
        return s;
    }
}

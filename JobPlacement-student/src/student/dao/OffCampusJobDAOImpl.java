package student.dao;

import student.model.OffCampusJob;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffCampusJobDAOImpl implements OffCampusJobDAO {

    @Override
    public long add(OffCampusJob j) {
        String sql = "INSERT INTO off_campus_jobs (student_id, company_name, role_title, applied_date, status, notes) " +
                     "VALUES (?,?,?,?,?,?)";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, j.getStudentId());
            ps.setString(2, j.getCompanyName());
            ps.setString(3, j.getRoleTitle());
            ps.setDate(4, j.getAppliedDate());
            ps.setString(5, j.getStatus());


            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
            return -1;

        } catch (SQLException e) {
            throw new RuntimeException("Add off-campus job failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<OffCampusJob> getByStudent(long studentId) {
        String sql = "SELECT * FROM off_campus_jobs WHERE student_id=? ORDER BY offcampus_id DESC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                List<OffCampusJob> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Get off-campus jobs failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(OffCampusJob j) {
        String sql =
            "UPDATE off_campus_jobs SET company_name=?, role_title=?, applied_date=?, status=?, notes=? " +
            "WHERE offcampus_id=? AND student_id=?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, j.getCompanyName());
            ps.setString(2, j.getRoleTitle());
            ps.setDate(3, j.getAppliedDate());
            ps.setString(4, j.getStatus());
            ps.setLong(6, j.getOffcampusId());
            ps.setLong(7, j.getStudentId());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Update off-campus job failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(long offcampusId, long studentId) {
        String sql = "DELETE FROM off_campus_jobs WHERE offcampus_id=? AND student_id=?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, offcampusId);
            ps.setLong(2, studentId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Delete off-campus job failed: " + e.getMessage(), e);
        }
    }

    private OffCampusJob map(ResultSet rs) throws SQLException {
        OffCampusJob j = new OffCampusJob();
        j.setOffcampusId(rs.getLong("offcampus_id"));
        j.setStudentId(rs.getLong("student_id"));
        j.setCompanyName(rs.getString("company_name"));
        j.setRoleTitle(rs.getString("role_title"));
        j.setAppliedDate(rs.getDate("applied_date"));
        j.setStatus(rs.getString("status"));
        return j;
    }
}
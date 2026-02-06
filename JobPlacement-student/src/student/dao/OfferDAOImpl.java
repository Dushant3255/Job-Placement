package student.dao;

import student.model.Offer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OfferDAOImpl implements OfferDAO {

    @Override
    public List<Offer> getOffersForStudent(long studentId) {
        String sql =
            "SELECT o.* FROM offers o " +
            "JOIN applications a ON a.application_id = o.application_id " +
            "WHERE a.student_id=? " +
            "ORDER BY o.offered_at DESC";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Offer> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Get offers failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(long offerId, long studentId, String newStatus) {
        if (!"ACCEPTED".equalsIgnoreCase(newStatus) && !"REJECTED".equalsIgnoreCase(newStatus)) {
            throw new IllegalArgumentException("Status must be ACCEPTED or REJECTED.");
        }

        String sql =
            "UPDATE offers o " +
            "JOIN applications a ON a.application_id = o.application_id " +
            "SET o.status=? " +
            "WHERE o.offer_id=? AND a.student_id=? AND o.status='PENDING'";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus.toUpperCase());
            ps.setLong(2, offerId);
            ps.setLong(3, studentId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Update offer status failed: " + e.getMessage(), e);
        }
    }

    private Offer map(ResultSet rs) throws SQLException {
        Offer o = new Offer();
        o.setOfferId(rs.getLong("offer_id"));
        o.setApplicationId(rs.getLong("application_id"));
        o.setOfferedAt(rs.getTimestamp("offered_at"));
        o.setOfferLetterUrl(rs.getString("offer_letter_url"));
        o.setStatus(rs.getString("status"));
        return o;
    }
}
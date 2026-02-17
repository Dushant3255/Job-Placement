package com.placement.student.dao;

import com.placement.common.db.DB;
import com.placement.student.model.Offer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OfferDAOImpl implements OfferDAO {

    @Override
    public List<Offer> getOffersForStudent(long studentId) throws SQLException {
        List<Offer> offers = new ArrayList<>();

        String sql = """
            SELECT o.*
            FROM offers o
            JOIN applications a ON o.application_id = a.application_id
            WHERE a.student_id = ?
            ORDER BY o.issued_at DESC
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    offers.add(map(rs));
                }
            }
        }

        return offers;
    }

    @Override
    public boolean updateStatus(long offerId, long studentId, String status) throws SQLException {
        String sql = """
            UPDATE offers
            SET status = ?
            WHERE offer_id = ?
              AND application_id IN (
                  SELECT application_id FROM applications WHERE student_id = ?
              )
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setLong(2, offerId);
            ps.setLong(3, studentId);

            return ps.executeUpdate() > 0;
        }
    }

    private Offer map(ResultSet rs) throws SQLException {
        Offer o = new Offer();
        o.setOfferId(rs.getLong("offer_id"));
        o.setApplicationId(rs.getLong("application_id"));
        o.setJoiningDate(rs.getString("joining_date"));
        o.setStatus(rs.getString("status"));
        o.setIssuedAt(rs.getString("issued_at"));
        return o;
    }
}

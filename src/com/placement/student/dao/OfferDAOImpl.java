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

    try (Connection conn = DB.getConnection()) {
        conn.setAutoCommit(false);

        // 1) Update offer status (only if it belongs to the student's application)
        String updateOffer = """
            UPDATE offers
            SET status = ?
            WHERE offer_id = ?
              AND application_id IN (
                  SELECT application_id FROM applications WHERE student_id = ?
              )
        """;

        int changed;
        try (PreparedStatement ps = conn.prepareStatement(updateOffer)) {
            ps.setString(1, status);
            ps.setLong(2, offerId);
            ps.setLong(3, studentId);
            changed = ps.executeUpdate();
        }

        if (changed <= 0) {
            conn.rollback();
            return false;
        }

        // 2) If student ACCEPTED, mark application as OFFER_ACCEPTED and increment hired count
        if ("ACCEPTED".equalsIgnoreCase(status)) {

            Long jobId = null;
            Long applicationId = null;

            // find application + job for this offer
            try (PreparedStatement ps = conn.prepareStatement("""
                SELECT a.application_id, a.job_id
                FROM offers o
                JOIN applications a ON a.application_id = o.application_id
                WHERE o.offer_id = ? AND a.student_id = ?
            """)) {
                ps.setLong(1, offerId);
                ps.setLong(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        applicationId = rs.getLong("application_id");
                        jobId = rs.getLong("job_id");
                    }
                }
            }

            if (applicationId != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE applications SET status='OFFER_ACCEPTED' WHERE application_id=?")) {
                    ps.setLong(1, applicationId);
                    ps.executeUpdate();
                }
            }

            if (jobId != null) {
                // increment hired_count
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE job_listings SET hired_count = COALESCE(hired_count,0) + 1 WHERE job_id=?")) {
                    ps.setLong(1, jobId);
                    ps.executeUpdate();
                }

                // auto-close if hired_count >= positions_available (and positions_available > 0)
                try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE job_listings
                    SET status = 'CLOSED'
                    WHERE job_id = ?
                      AND positions_available > 0
                      AND hired_count >= positions_available
                """)) {
                    ps.setLong(1, jobId);
                    ps.executeUpdate();
                }
            }
        }

        conn.commit();
        return true;}
    }

    private Offer map(ResultSet rs) throws SQLException {
        Offer o = new Offer();
        o.setOfferId(rs.getLong("offer_id"));
        o.setApplicationId(rs.getLong("application_id"));
        o.setPackageLpa(rs.getDouble("package_lpa"));
        o.setJoiningDate(rs.getString("joining_date"));
        o.setStatus(rs.getString("status"));
        o.setLetterPath(rs.getString("letter_path"));
        return o;
    }
}

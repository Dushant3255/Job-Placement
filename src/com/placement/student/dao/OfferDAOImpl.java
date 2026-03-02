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
        if (status == null || status.isBlank()) return false;

        String newStatus = status.trim().toUpperCase();

        try (Connection con = DB.getConnection()) {
            con.setAutoCommit(false);

            try {
                // 1) Verify ownership + fetch related ids
                OfferContext ctx = getOfferContext(con, offerId, studentId);
                if (ctx == null) {
                    con.rollback();
                    return false;
                }

                String prevOfferStatus = (ctx.offerStatus == null) ? "" : ctx.offerStatus.trim().toUpperCase();

                // 2) Update offer status
                if (!updateOfferStatus(con, offerId, studentId, newStatus)) {
                    con.rollback();
                    return false;
                }

                // 3) If ACCEPTED and wasn't already accepted -> update application + hired_count (+ auto-close)
                if ("ACCEPTED".equals(newStatus) && !"ACCEPTED".equals(prevOfferStatus)) {
                    updateApplicationStatusAccepted(con, ctx.applicationId, studentId);
                    incrementHiredAndMaybeClose(con, ctx.jobId);
                }

                con.commit();
                return true;

            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // ----------------- helpers -----------------

    private static class OfferContext {
        long applicationId;
        long jobId;
        String offerStatus;
    }

    private OfferContext getOfferContext(Connection con, long offerId, long studentId) throws SQLException {
        String sql = """
            SELECT o.application_id, a.job_id, o.status AS offer_status
            FROM offers o
            JOIN applications a ON o.application_id = a.application_id
            WHERE o.offer_id = ? AND a.student_id = ?
            LIMIT 1
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, offerId);
            ps.setLong(2, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                OfferContext ctx = new OfferContext();
                ctx.applicationId = rs.getLong("application_id");
                ctx.jobId = rs.getLong("job_id");
                ctx.offerStatus = rs.getString("offer_status");
                return ctx;
            }
        }
    }

    private boolean updateOfferStatus(Connection con, long offerId, long studentId, String newStatus) throws SQLException {
        String sql = """
            UPDATE offers
            SET status = ?
            WHERE offer_id = ?
              AND application_id IN (
                  SELECT application_id FROM applications WHERE student_id = ?
              )
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, offerId);
            ps.setLong(3, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private void updateApplicationStatusAccepted(Connection con, long applicationId, long studentId) throws SQLException {
        // Only set the application status when accepted (keeps admin/company filters stable)
        String sql = """
            UPDATE applications
            SET status = 'OFFER_ACCEPTED'
            WHERE application_id = ? AND student_id = ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            ps.setLong(2, studentId);
            ps.executeUpdate();
        }
    }

    private void incrementHiredAndMaybeClose(Connection con, long jobId) throws SQLException {
        // increment hired_count
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE job_listings SET hired_count = COALESCE(hired_count,0) + 1 WHERE job_id = ?")) {
            ps.setLong(1, jobId);
            ps.executeUpdate();
        }

        int hired = 0;
        int positions = 0;

        // read counts
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COALESCE(hired_count,0) AS hired_count, COALESCE(positions_available,0) AS positions_available FROM job_listings WHERE job_id=?")) {
            ps.setLong(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hired = rs.getInt("hired_count");
                    positions = rs.getInt("positions_available");
                }
            }
        }

        // auto-close if positions_available > 0 and filled
        if (positions > 0 && hired >= positions) {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE job_listings SET status='CLOSED' WHERE job_id=?")) {
                ps.setLong(1, jobId);
                ps.executeUpdate();
            }
        }
    }

    private Offer map(ResultSet rs) throws SQLException {
        Offer o = new Offer();
        o.setOfferId(rs.getLong("offer_id"));
        o.setApplicationId(rs.getLong("application_id"));
        o.setPackageLpa(rs.getDouble("package_lpa"));
        o.setJoiningDate(rs.getString("joining_date"));
        o.setStatus(rs.getString("status"));
        o.setIssuedAt(rs.getString("issued_at"));

        // ✅ Offer letter path (requires offers.letter_path column + Offer.setLetterPath)
        try {
            o.setletterPath(rs.getString("letter_path"));
        } catch (SQLException ignore) { }

        return o;
    }
}

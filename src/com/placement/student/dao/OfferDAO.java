package com.placement.student.dao;

import com.placement.student.model.Offer;

import java.sql.SQLException;
import java.util.List;

public interface OfferDAO {
    List<Offer> getOffersForStudent(long studentId) throws SQLException;

    boolean updateStatus(long offerId, long studentId, String newStatus) throws SQLException;
    // newStatus should be: ACCEPTED or REJECTED (or PENDING if you want)
}

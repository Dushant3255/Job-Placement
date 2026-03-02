package com.placement.student.service;

import com.placement.student.dao.OfferDAO;
import com.placement.student.model.Offer;

import java.sql.SQLException;
import java.util.List;

public class OfferService {

    private final OfferDAO offerDAO;

    public OfferService(OfferDAO offerDAO) {
        this.offerDAO = offerDAO;
    }

    public List<Offer> getOffers(long studentId) {
        try {
            return offerDAO.getOffersForStudent(studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch offers", e);
        }
    }

    public boolean acceptOffer(long studentId, long offerId) {
        return updateOfferStatus(studentId, offerId, StudentStatuses.OfferStatus.ACCEPTED);
    }

    public boolean rejectOffer(long studentId, long offerId) {
        return updateOfferStatus(studentId, offerId, StudentStatuses.OfferStatus.REJECTED);
    }

    private boolean updateOfferStatus(long studentId, long offerId, String newStatus) {
        if (!StudentStatuses.OfferStatus.ACCEPTED.equals(newStatus) &&
            !StudentStatuses.OfferStatus.REJECTED.equals(newStatus) &&
            !StudentStatuses.OfferStatus.PENDING.equals(newStatus)) {
            throw new ServiceException("Invalid offer status: " + newStatus);
        }

        try {
            return offerDAO.updateStatus(offerId, studentId, newStatus);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update offer status", e);
        }
    }
}

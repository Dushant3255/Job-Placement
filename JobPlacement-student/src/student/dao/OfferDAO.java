package student.dao;

import student.model.Offer;
import java.util.List;

public interface OfferDAO {
    List<Offer> getOffersForStudent(long studentId);
    boolean updateStatus(long offerId, long studentId, String newStatus); // ACCEPTED / REJECTED only
}
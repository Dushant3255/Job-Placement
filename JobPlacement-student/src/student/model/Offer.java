package student.model;

import java.sql.Timestamp;

public class Offer {
    private long offerId;
    private long applicationId;
    private Timestamp offeredAt;
    private String offerLetterUrl;
    private String status; // PENDING/ACCEPTED/REJECTED

    public Offer() {}

    // Getters/Setters
    public long getOfferId() { return offerId; }
    public void setOfferId(long offerId) { this.offerId = offerId; }

    public long getApplicationId() { return applicationId; }
    public void setApplicationId(long applicationId) { this.applicationId = applicationId; }

    public Timestamp getOfferedAt() { return offeredAt; }
    public void setOfferedAt(Timestamp offeredAt) { this.offeredAt = offeredAt; }

    public String getOfferLetterUrl() { return offerLetterUrl; }
    public void setOfferLetterUrl(String offerLetterUrl) { this.offerLetterUrl = offerLetterUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
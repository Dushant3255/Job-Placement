package com.placement.student.model;

public class Offer {
    private long offerId;
    private long applicationId;
    private double packageLpa;
    private String joiningDate;  // store as TEXT
    private String status;       // PENDING / ACCEPTED / REJECTED
    private String issuedAt;     // store as TEXT (datetime)
    private String letterPath;  // path to uploaded offer letter (PDF)

    public Offer() {}

    public long getOfferId() { return offerId; }
    public void setOfferId(long offerId) { this.offerId = offerId; }

    public long getApplicationId() { return applicationId; }
    public void setApplicationId(long applicationId) { this.applicationId = applicationId; }

    public double getPackageLpa() { return packageLpa; }
    public void setPackageLpa(double packageLpa) { this.packageLpa = packageLpa; }

    public String getJoiningDate() { return joiningDate; }
    public void setJoiningDate(String joiningDate) { this.joiningDate = joiningDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIssuedAt() { return issuedAt; }
    public void setIssuedAt(String issuedAt) { this.issuedAt = issuedAt; }


public String getLetterPath() { return letterPath; }
public void setLetterPath(String letterPath) { this.letterPath = letterPath; }

}

package com.placement.student.model;

import java.sql.Timestamp;

public class Interview {
    private long interviewId;
    private long applicationId;
    private Timestamp scheduledAt;
    private String meetingLink;
    private String mode; // online/ in-person
    private String status;

    public Interview() {}

    // Getters/Setters
    public long getInterviewId() { return interviewId; }
    public void setInterviewId(long interviewId) { this.interviewId = interviewId; }

    public long getApplicationId() { return applicationId; }
    public void setApplicationId(long applicationId) { this.applicationId = applicationId; }

    public Timestamp getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Timestamp scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

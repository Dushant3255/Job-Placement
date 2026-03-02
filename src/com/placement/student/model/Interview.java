package com.placement.student.model;

import java.sql.Timestamp;

public class Interview {
    private long interviewId;
    private long applicationId;
    private Timestamp scheduledAt;

    private String mode;          // Online / Face-to-face
    private String location;      // Office location (Face-to-face) - NULL when Online
    private String meetingLink;   // Meeting link (Online) - NULL when Face-to-face
    private String status;
    private String notes;

    public Interview() {}

    public long getInterviewId() { return interviewId; }
    public void setInterviewId(long interviewId) { this.interviewId = interviewId; }

    public long getApplicationId() { return applicationId; }
    public void setApplicationId(long applicationId) { this.applicationId = applicationId; }

    public Timestamp getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Timestamp scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

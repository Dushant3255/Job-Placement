package com.placement.student.model;

public class OffCampusJob {
    private long offCampusId;
    private long studentId;
    private String companyName;
    private String roleTitle;
    private String appliedDate;
    private String status;
    private String notes; // ✅ add

    public long getOffCampusId() { return offCampusId; }
    public void setOffCampusId(long offCampusId) { this.offCampusId = offCampusId; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public String getAppliedDate() { return appliedDate; }
    public void setAppliedDate(String appliedDate) { this.appliedDate = appliedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

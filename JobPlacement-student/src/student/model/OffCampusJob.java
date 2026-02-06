package student.model;

import java.sql.Date;

public class OffCampusJob {
    private long offcampusId;
    private long studentId;
    private String companyName;
    private String roleTitle;
    private Date appliedDate;
    private String status;

    public OffCampusJob() {}

    // Getters/Setters
    public long getOffcampusId() { return offcampusId; }
    public void setOffcampusId(long offcampusId) { this.offcampusId = offcampusId; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public Date getAppliedDate() { return appliedDate; }
    public void setAppliedDate(Date appliedDate) { this.appliedDate = appliedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}

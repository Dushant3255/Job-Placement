package student.model;

import java.sql.Timestamp;

public class Application {
    private long applicationId;
    private long studentId;
    private long jobId;
    private Timestamp appliedAt;
    private String status; // SUBMITTED/SHORTLISTED/REJECTED/OFFERED/WITHDRAWN

    public Application() {}

    // Getters/Setters
    public long getApplicationId() { return applicationId; }
    public void setApplicationId(long applicationId) { this.applicationId = applicationId; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public long getJobId() { return jobId; }
    public void setJobId(long jobId) { this.jobId = jobId; }

    public Timestamp getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Timestamp appliedAt) { this.appliedAt = appliedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
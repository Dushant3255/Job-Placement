package com.placement.student.model;

import java.sql.Timestamp;

public class JobListing {
    private long jobId;
    private String companyName;
    private String title;
    private String department;
    private String description;
    private Double minGpa;
    private Integer minYear;
    private String eligibilityRule;
    private String status; // OPEN/CLOSED
    private Timestamp postedAt;

    public JobListing() {}

    // Getters/Setters
    public long getJobId() { return jobId; }
    public void setJobId(long jobId) { this.jobId = jobId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getMinGpa() { return minGpa; }
    public void setMinGpa(Double minGpa) { this.minGpa = minGpa; }

    public Integer getMinYear() { return minYear; }
    public void setMinYear(Integer minYear) { this.minYear = minYear; }

    public String getEligibilityRule() { return eligibilityRule; }
    public void setEligibilityRule(String eligibilityRule) { this.eligibilityRule = eligibilityRule; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getPostedAt() { return postedAt; }
    public void setPostedAt(Timestamp postedAt) { this.postedAt = postedAt; }
}

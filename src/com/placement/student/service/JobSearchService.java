package com.placement.student.service;

import com.placement.student.dao.JobListingDAO;
import com.placement.student.model.JobListing;

import java.util.List;

public class JobSearchService {

    private final JobListingDAO jobListingDAO;

    public JobSearchService(JobListingDAO jobListingDAO) {
        this.jobListingDAO = jobListingDAO;
    }

    public List<JobListing> getAllOpenJobs() {
        return jobListingDAO.getAllOpen();
    }

    public List<JobListing> viewAllJobs() {
        return jobListingDAO.getAll();
    }

    public List<JobListing> searchByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new ServiceException("Department is required");
        }
        return jobListingDAO.searchByDepartment(department.trim());
    }

    public List<JobListing> filterByEligibility(Double studentGpa, Integer studentYear) {
        if (studentGpa == null || studentYear == null) {
            throw new ServiceException("Student GPA and year are required for eligibility filtering");
        }
        return jobListingDAO.filterByEligibility(studentGpa, studentYear);
    }

    public JobListing getJobById(long jobId) {
        JobListing job = jobListingDAO.findById(jobId);
        if (job == null) throw new ServiceException("Job listing not found: " + jobId);
        return job;
    }
}

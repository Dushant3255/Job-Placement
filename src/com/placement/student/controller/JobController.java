package com.placement.student.controller;

import com.placement.student.model.JobListing;
import com.placement.student.service.JobSearchService;

import java.util.List;

public class JobController {

    private final JobSearchService jobSearchService;

    public JobController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }

    public List<JobListing> viewAllOpenJobs() {
        return jobSearchService.getAllOpenJobs();
    }

    public List<JobListing> viewAllJobs() {
        return jobSearchService.viewAllJobs();
    }

    public List<JobListing> searchByDepartment(String department) {
        return jobSearchService.searchByDepartment(department);
    }

    public List<JobListing> filterByEligibility(Double gpa, Integer year) {
        return jobSearchService.filterByEligibility(gpa, year);
    }

    public JobListing viewJobDetails(long jobId) {
        return jobSearchService.getJobById(jobId);
    }
}

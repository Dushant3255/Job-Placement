package com.placement.student.dao;

import com.placement.student.model.JobListing;
import java.util.List;

public interface JobListingDAO {
    List<JobListing> getAllOpen();
    List<JobListing> searchByDepartment(String department);
    List<JobListing> filterByEligibility(Double studentGpa, Integer studentYear);
    JobListing findById(long jobId);
}
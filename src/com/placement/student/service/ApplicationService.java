package com.placement.student.service;

import com.placement.student.dao.ApplicationDAO;
import com.placement.student.dao.JobListingDAO;
import com.placement.student.model.Application;
import com.placement.student.model.JobListing;

import java.util.List;

public class ApplicationService {

    private final ApplicationDAO applicationDAO;
    private final JobListingDAO jobListingDAO;

    public ApplicationService(ApplicationDAO applicationDAO, JobListingDAO jobListingDAO) {
        this.applicationDAO = applicationDAO;
        this.jobListingDAO = jobListingDAO;
    }

    public long apply(long studentId, long jobId) {
        JobListing job = jobListingDAO.findById(jobId);
        if (job == null) throw new ServiceException("Job does not exist");
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) throw new ServiceException("Job is not open");

        // DAO throws RuntimeException if already applied; we rethrow as ServiceException for clean UI handling
        try {
            long applicationId = applicationDAO.apply(studentId, jobId);
            if (applicationId <= 0) throw new ServiceException("Application failed (no id returned)");
            return applicationId;
        } catch (RuntimeException ex) {
            throw new ServiceException(ex.getMessage(), ex);
        }
    }

    public List<Application> getMyApplications(long studentId) {
        return applicationDAO.getByStudent(studentId);
    }

    public boolean withdraw(long studentId, long applicationId) {
        return applicationDAO.withdraw(applicationId, studentId);
    }
}

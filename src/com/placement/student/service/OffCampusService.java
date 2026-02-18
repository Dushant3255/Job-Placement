package com.placement.student.service;

import com.placement.student.dao.OffCampusJobDAO;
import com.placement.student.model.OffCampusJob;

import java.sql.SQLException;
import java.util.List;

public class OffCampusService {

    private final OffCampusJobDAO offCampusJobDAO;

    public OffCampusService(OffCampusJobDAO offCampusJobDAO) {
        this.offCampusJobDAO = offCampusJobDAO;
    }

    public long add(OffCampusJob job) {
        validate(job);
        try {
            return offCampusJobDAO.add(job);
        } catch (SQLException e) {
            throw new ServiceException("Failed to add off-campus job", e);
        }
    }

    public List<OffCampusJob> getMyOffCampusJobs(long studentId) {
        try {
            return offCampusJobDAO.getByStudent(studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch off-campus jobs", e);
        }
    }

    public boolean update(OffCampusJob job) {
        validate(job);
        if (job.getOffCampusId() <= 0) throw new ServiceException("OffCampusId is required");
        try {
            return offCampusJobDAO.update(job);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update off-campus job", e);
        }
    }

    public boolean delete(long studentId, long offCampusId) {
        try {
            return offCampusJobDAO.delete(offCampusId, studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete off-campus job", e);
        }
    }

    private void validate(OffCampusJob job) {
        if (job == null) throw new ServiceException("Off-campus job cannot be null");
        if (job.getStudentId() <= 0) throw new ServiceException("StudentId is required");
        if (isBlank(job.getCompanyName())) throw new ServiceException("Company name is required");
        if (isBlank(job.getRoleTitle())) throw new ServiceException("Role title is required");
        if (isBlank(job.getAppliedDate())) throw new ServiceException("Applied date is required");
        if (isBlank(job.getStatus())) throw new ServiceException("Status is required");
        // notes can be null/blank
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

package com.placement.student.service;

import com.placement.student.dao.AcademicDetailsDAO;
import com.placement.student.model.AcademicDetails;

import java.sql.SQLException;

public class AcademicDetailsService {

    private final AcademicDetailsDAO academicDetailsDAO;

    public AcademicDetailsService(AcademicDetailsDAO academicDetailsDAO) {
        this.academicDetailsDAO = academicDetailsDAO;
    }

    public void addOrUpdate(long studentId, AcademicDetails details) {
        validate(details);

        try {
            academicDetailsDAO.addOrUpdate(studentId, details);
        } catch (SQLException e) {
            throw new ServiceException("Failed to save academic details", e);
        }
    }

    public AcademicDetails getByStudentId(long studentId) {
        try {
            return academicDetailsDAO.getByStudentId(studentId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch academic details", e);
        }
    }

    private void validate(AcademicDetails d) {
        if (d == null) throw new ServiceException("Academic details cannot be null");
        if (d.getYearOfStudy() <= 0) throw new ServiceException("Year of study must be > 0");
        if (d.getGpa() < 0 || d.getGpa() > 4.0) throw new ServiceException("GPA must be between 0 and 4.0");
        if (d.getCgpa() < 0 || d.getCgpa() > 4.0) throw new ServiceException("CGPA must be between 0 and 4.0");
        if (d.getBacklogs() < 0) throw new ServiceException("Backlogs cannot be negative");
        if (d.getGraduationYear() < 1900) throw new ServiceException("Graduation year looks invalid");
    }
}

package com.placement.student.service;

import com.placement.student.dao.StudentDao;
import com.placement.student.model.StudentProfile;

import java.sql.SQLException;

public class StudentProfileService {

    private final StudentDao studentDao;

    public StudentProfileService(StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    public void createProfile(long userId, StudentProfile profile) {
        if (profile == null) throw new ServiceException("Profile cannot be null");
        if (isBlank(profile.getFirstName()) || isBlank(profile.getLastName()) || isBlank(profile.getGender())) {
            throw new ServiceException("First name, last name and gender are required");
        }

        try {
            studentDao.createStudentProfile(toIntExact(userId), profile);
        } catch (SQLException e) {
            throw new ServiceException("Failed to create profile", e);
        }
    }

    public StudentProfile getProfile(long userId) {
        try {
            return studentDao.findByUserId(toIntExact(userId));
        } catch (SQLException e) {
            throw new ServiceException("Failed to fetch profile", e);
        }
    }

    public void updateProfileImage(long userId, String imagePath) {
        // imagePath can be null if you want to clear it
        try {
            studentDao.updateProfileImagePath(toIntExact(userId), imagePath);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update profile image", e);
        }
    }

    private static int toIntExact(long value) {
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new ServiceException("UserId out of int range: " + value);
        }
        return (int) value;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

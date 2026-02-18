package com.placement.student.controller;

import com.placement.student.model.AcademicDetails;
import com.placement.student.model.StudentProfile;
import com.placement.student.service.AcademicDetailsService;
import com.placement.student.service.StudentProfileService;

public class ProfileController {

    private final StudentProfileService profileService;
    private final AcademicDetailsService academicService;

    public ProfileController(StudentProfileService profileService, AcademicDetailsService academicService) {
        this.profileService = profileService;
        this.academicService = academicService;
    }

    public void createProfile(long userId, StudentProfile profile) {
        profileService.createProfile(userId, profile);
    }

    public StudentProfile viewProfile(long userId) {
        return profileService.getProfile(userId);
    }

    public void updateProfileImage(long userId, String path) {
        profileService.updateProfileImage(userId, path);
    }

    public void addOrUpdateAcademic(long studentId, AcademicDetails details) {
        academicService.addOrUpdate(studentId, details);
    }

    public AcademicDetails viewAcademic(long studentId) {
        return academicService.getByStudentId(studentId);
    }
}

package com.placement.student.controller;

import com.placement.student.model.Application;
import com.placement.student.service.ApplicationService;

import java.util.List;

public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public long applyForJob(long studentId, long jobId) {
        return applicationService.apply(studentId, jobId);
    }

    public List<Application> viewMyApplications(long studentId) {
        return applicationService.getMyApplications(studentId);
    }

    public boolean withdrawApplication(long studentId, long applicationId) {
        return applicationService.withdraw(studentId, applicationId);
    }
}

package com.placement.student.controller;

import com.placement.student.model.OffCampusJob;
import com.placement.student.service.OffCampusService;

import java.util.List;

public class OffCampusController {

    private final OffCampusService offCampusService;

    public OffCampusController(OffCampusService offCampusService) {
        this.offCampusService = offCampusService;
    }

    public long addOffCampusJob(OffCampusJob job) {
        return offCampusService.add(job);
    }

    public List<OffCampusJob> viewOffCampusJobs(long studentId) {
        return offCampusService.getMyOffCampusJobs(studentId);
    }

    public boolean updateOffCampusJob(OffCampusJob job) {
        return offCampusService.update(job);
    }

    public boolean deleteOffCampusJob(long studentId, long offCampusId) {
        return offCampusService.delete(studentId, offCampusId);
    }
}

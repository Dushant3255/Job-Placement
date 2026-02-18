package com.placement.student.controller;

import com.placement.student.model.Interview;
import com.placement.student.service.InterviewService;

import java.util.List;

public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    public List<Interview> viewMyInterviews(long studentId) {
        return interviewService.getInterviews(studentId);
    }
}

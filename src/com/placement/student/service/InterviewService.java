package com.placement.student.service;

import com.placement.student.dao.InterviewDAO;
import com.placement.student.model.Interview;

import java.util.List;

public class InterviewService {

    private final InterviewDAO interviewDAO;

    public InterviewService(InterviewDAO interviewDAO) {
        this.interviewDAO = interviewDAO;
    }

    public List<Interview> getInterviews(long studentId) {
        return interviewDAO.getInterviewsForStudent(studentId);
    }
}

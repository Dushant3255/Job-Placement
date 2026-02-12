package com.placement.student.dao;

import com.placement.student.model.Interview;
import java.util.List;

public interface InterviewDAO {
    List<Interview> getInterviewsForStudent(long studentId);
}

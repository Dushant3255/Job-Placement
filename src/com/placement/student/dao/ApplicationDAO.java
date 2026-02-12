package com.placement.student.dao;

import com.placement.student.model.Application;
import java.util.List;

public interface ApplicationDAO {
    long apply(long studentId, long jobId);
    List<Application> getByStudent(long studentId);
    boolean withdraw(long applicationId, long studentId);
}

package com.placement.student.dao;

import com.placement.student.model.Application;

import java.util.List;

public interface ApplicationDAO {
    long apply(long studentId, long jobId);
    List<Application> getByStudent(long studentId);
    boolean withdraw(long applicationId, long studentId);

    /** True if this student has ever applied for this job (any status). */
    boolean hasApplied(long studentId, long jobId);

    /** Returns the student's application status for a given job, or null if not applied. */
    String getStatusForJob(long studentId, long jobId);
}

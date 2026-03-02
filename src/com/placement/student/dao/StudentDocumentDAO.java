package com.placement.student.dao;

public interface StudentDocumentDAO {
    String getCvPath(long studentId);
    void saveOrUpdateCvPath(long studentId, String cvPath);
}

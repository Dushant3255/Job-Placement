package com.placement.student.dao;

import com.placement.student.model.AcademicDetails;
import java.sql.SQLException;

public interface AcademicDetailsDAO {
    void addOrUpdate(long studentId, AcademicDetails details) throws SQLException;
    AcademicDetails getByStudentId(long studentId) throws SQLException;
}

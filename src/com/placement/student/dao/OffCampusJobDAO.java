package com.placement.student.dao;

import com.placement.student.model.OffCampusJob;

import java.sql.SQLException;
import java.util.List;

public interface OffCampusJobDAO {
    long add(OffCampusJob job) throws SQLException;
    List<OffCampusJob> getByStudent(long studentId) throws SQLException;
    boolean update(OffCampusJob job) throws SQLException;   // update status/notes/etc
    boolean delete(long offcampusId, long studentId) throws SQLException;
}

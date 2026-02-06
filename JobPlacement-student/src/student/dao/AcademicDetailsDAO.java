package student.dao;

import student.model.AcademicDetails;

public interface AcademicDetailsDAO {
    AcademicDetails getByStudentId(long studentId);
    boolean upsert(AcademicDetails details); // insert if not exists, else update
}
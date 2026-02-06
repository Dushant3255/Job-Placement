package student.dao;

import student.model.Student;

public interface StudentDAO {
    long register(Student student, String rawPassword);
    Student login(String usernameOrEmail, String rawPassword);
    Student findById(long studentId);

    boolean updateProfile(Student student);      // name/phone/department/email (optional)
    boolean changePassword(long studentId, String oldRawPassword, String newRawPassword);
}

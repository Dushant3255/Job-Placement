package student.dao;

import student.model.Interview;
import java.util.List;

public interface InterviewDAO {
    List<Interview> getInterviewsForStudent(long studentId);
}

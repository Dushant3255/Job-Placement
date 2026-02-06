package student.dao;

import student.model.OffCampusJob;
import java.util.List;

public interface OffCampusJobDAO {
    long add(OffCampusJob job);
    List<OffCampusJob> getByStudent(long studentId);
    boolean update(OffCampusJob job);   // update status/notes/etc
    boolean delete(long offcampusId, long studentId);
}
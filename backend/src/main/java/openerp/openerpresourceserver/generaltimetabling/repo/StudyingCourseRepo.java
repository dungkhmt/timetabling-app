package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.StudyingCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyingCourseRepo extends JpaRepository<StudyingCourse, String> {
    List<StudyingCourse> findAllBySchoolId(String schoolId);

    @Query(value = "SELECT DISTINCT s.school_id FROM courses s",nativeQuery = true)
    List<String> getDistinctInSchoolId();
}

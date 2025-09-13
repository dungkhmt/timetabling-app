package openerp.openerpresourceserver.teacherassignment.repo;

import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenedClassRepo extends JpaRepository<OpenedClass, Long> {
    List<OpenedClass> findAllBySemester(String semester);

    @Query("SELECT oc FROM OpenedClass oc " +
            "LEFT JOIN FETCH oc.batchClass " +
            "LEFT JOIN FETCH oc.timeClasses " +
            "JOIN FETCH oc.studyingCourse " +
            "WHERE oc.semester = :semester")
    List<OpenedClass> findAllBySemesterWithDto(@Param("semester") String semester);

    @Query("SELECT oc FROM OpenedClass oc " +
            "LEFT JOIN FETCH oc.batchClass " +
            "LEFT JOIN FETCH oc.timeClasses " +
            "JOIN FETCH oc.studyingCourse sc " +
            "WHERE oc.semester = :semester and  sc.schoolId = :schoolId ")
    List<OpenedClass> findAllBySemesterAndSchoolIdWithDto(@Param("semester") String semester,@Param("schoolId") String schoolId);

    @Query("SELECT oc FROM OpenedClass oc " +
            "JOIN FETCH oc.batchClass bc " +
            "LEFT JOIN FETCH oc.timeClasses " +
            "WHERE bc.id.batchId = :batchId")
    List<OpenedClass> findAllByBatchIdWithDto(@Param("batchId") Long batchId);

    @Query(value = "select distinct semester from teacherclassassignment_opened_classes;", nativeQuery = true)
    List<String> getDistinctInSemester();

    List<OpenedClass> findAllBySemesterAndCourseId(String semester, String courseId);
}

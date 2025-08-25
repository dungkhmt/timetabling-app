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
            "WHERE oc.semester = :semester")
    List<OpenedClass> findAllBySemesterWithDto(@Param("semester") String semester);

    @Query(value = "select distinct semester from teacherclassassignment_opened_classes;", nativeQuery = true)
    List<String> getDistinctInSemester();
}

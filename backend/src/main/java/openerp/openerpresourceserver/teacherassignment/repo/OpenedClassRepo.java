package openerp.openerpresourceserver.teacherassignment.repo;

import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenedClassRepo extends JpaRepository<OpenedClass, Long> {
    List<OpenedClass> findAllBySemester(String semester);

    @Query(value = "select distinct semester from teacherclassassignment_opened_classes;", nativeQuery = true)
    List<String> getDistinctInSemester();
}

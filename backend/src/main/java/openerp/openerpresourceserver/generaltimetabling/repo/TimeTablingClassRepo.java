package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeTablingClassRepo extends JpaRepository<TimeTablingClass, Long> {
    List<TimeTablingClass> findAllBySemester(String semester);
    List<TimeTablingClass> findAllByBatchId(Long batchId);
    List<TimeTablingClass> findAllByBatchIdIn(List<Long> batchIds);



    List<TimeTablingClass> findAllByRefClassId(Long refClassId);

    List<TimeTablingClass> findAllByRefClassIdIn(List<Long> refClassIds);


    List<TimeTablingClass> findAllByParentClassId(Long parentClassId);

    List<TimeTablingClass> findAllByIdIn(List<Long> ids);

    @Query(value = "SELECT nextval('timetabling_class_seq')", nativeQuery = true)
    Long getNextReferenceValue();
}

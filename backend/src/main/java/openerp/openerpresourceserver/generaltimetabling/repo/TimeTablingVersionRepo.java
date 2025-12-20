package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingTimeTableVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeTablingVersionRepo extends JpaRepository<TimeTablingTimeTableVersion, Long> {
    List<TimeTablingTimeTableVersion> findBySemesterAndNameContaining(String semester, String name);
    List<TimeTablingTimeTableVersion> findByNameContaining(String name);
    List<TimeTablingTimeTableVersion> findAll();
    List<TimeTablingTimeTableVersion> findAllBySemester(String semester);
    List<TimeTablingTimeTableVersion> findAllByBatchId(Long batchId);

    List<TimeTablingTimeTableVersion> findAllByStatusAndBatchIdIn(String status, List<Long> batchIds);
    List<TimeTablingTimeTableVersion> findAllByStatusAndBatchId(String status, Long batchId);


}

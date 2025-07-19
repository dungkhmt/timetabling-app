package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeTablingBatchRepo extends JpaRepository<TimeTablingBatch, Long> {
    public List<TimeTablingBatch> findAllByCreatedByUserIdAndSemester(String createdByUserId, String semester);
    public List<TimeTablingBatch> findAllBySemester(String semester);

    @Query(value = "SELECT nextval('timetabling_batch_seq')", nativeQuery = true)
    Long getNextIdValue();
}

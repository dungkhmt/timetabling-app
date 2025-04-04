package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeTablingClassSegmentRepo extends JpaRepository<TimeTablingClassSegment, Long> {
    List<TimeTablingClassSegment> findAllByClassIdIn(List<Long> classIds);
    @Query(value = "SELECT nextval('timetabling_class_segment_seq')", nativeQuery = true)
    Long getNextReferenceValue();
}

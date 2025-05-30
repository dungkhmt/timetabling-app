package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeTablingClassSegmentRepo extends JpaRepository<TimeTablingClassSegment, Long> {
    List<TimeTablingClassSegment> findAllByClassIdIn(List<Long> classIds);

    List<TimeTablingClassSegment> findAllByClassId(Long classId);

    List<TimeTablingClassSegment> findAllByVersionId(Long versionId);

    List<TimeTablingClassSegment> findAllByVersionIdAndClassIdIn(Long versionId, List<Long> classIds);

    List<TimeTablingClassSegment> findAllByClassIdInAndVersionId(List<Long> classIds, Long versionId);
    
    List<TimeTablingClassSegment> findAllByClassIdInAndVersionIdIsNull(List<Long> classIds);

    @Query(value = "select * from timetabling_class_segment tcs \n" +
            "where class_id in (select id from timetabling_class tc where semester = ?1) and room is not null", nativeQuery = true)
    List<TimeTablingClassSegment> findAllBySemesterAndRoomNotNull(String semester);

    @Query(value = "select * from timetabling_class_segment tcs \n" +
            "where class_id in (select id from timetabling_class tc where version_id = ?1 and semester = ?2) and room is not null", nativeQuery = true)
    List<TimeTablingClassSegment> findAllByVersionIdAndSemesterAndRoomNotNull(Long versionId, String semester);

    @Query(value = "SELECT nextval('timetabling_class_segment_seq')", nativeQuery = true)
    Long getNextReferenceValue();
}

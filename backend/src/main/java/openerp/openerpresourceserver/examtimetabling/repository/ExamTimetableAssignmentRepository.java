package openerp.openerpresourceserver.examtimetabling.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableAssignment;

@Repository
public interface ExamTimetableAssignmentRepository extends JpaRepository<ExamTimetableAssignment, UUID> {
    long countByExamTimetableIdAndRoomIdIsNotNullAndExamSessionIdIsNotNull(UUID examTimetableId);

    @Query(value = "SELECT " +
    "a.id as assignment_id, " +
    "c.id as class_id, " +
    "c.exam_class_id as exam_class_identifier, " +
    "c.class_id as orig_class_id, " +
    "c.course_id, " +
    "c.group_id, " +
    "c.course_name, " +
    "c.description, " +
    "c.number_students, " +
    "c.period, " +
    "c.management_code, " +
    "c.school, " +
    "CAST(a.room_id AS VARCHAR) as room_id, " +
    "CAST(a.exam_session_id AS VARCHAR) as session_id, " +
    "a.week_number, " +
    "a.date " +
    "FROM exam_timetable_assignment a " +
    "JOIN exam_timetabling_class c ON a.exam_timtabling_class_id = c.id " +
    "WHERE a.exam_timetable_id = :timetableId", 
    nativeQuery = true)
    List<Map<String, Object>> findAssignmentsWithDetailsByTimetableId(UUID timetableId);

    long countByExamTimetableId(UUID timetableId);

    @Query(value = "SELECT a FROM ExamTimetableAssignment a " +
            "WHERE a.examTimetableId = :timetableId AND a.roomId IS NOT NULL AND a.examSessionId IS NOT NULL AND a.date IS NOT NULL")
    List<ExamTimetableAssignment> findAssignedClassByExamTimetableId(UUID timetableId);

    List<ExamTimetableAssignment> findByExamTimetableId(UUID timetableId);

    List<ExamTimetableAssignment> findByExamTimetableIdAndExamTimetablingClassIdIn(UUID timetableId, List<UUID> classIds);

}

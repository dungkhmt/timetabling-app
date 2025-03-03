package openerp.openerpresourceserver.examtimetabling.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.AssignmentUpdateDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ConflictDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamAssignmentDTO;
import openerp.openerpresourceserver.examtimetabling.entity.ExamClass;
import openerp.openerpresourceserver.examtimetabling.entity.ExamRoom;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableAssignment;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableSession;
import openerp.openerpresourceserver.examtimetabling.repository.ExamRoomRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamTimetableAssignmentRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamTimetableSessionRepository;

@Service
@RequiredArgsConstructor
public class ExamTimetableAssignmentService {
    private final ExamTimetableAssignmentRepository assignmentRepository;
    private final ExamRoomRepository roomRepository;
    private final ExamTimetableSessionRepository sessionRepository;
    private final EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    
    public List<ConflictDTO> checkForConflicts(List<AssignmentUpdateDTO> assignmentChanges) {
        if (assignmentChanges == null || assignmentChanges.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<ConflictDTO> conflicts = new ArrayList<>();
        
        for (AssignmentUpdateDTO change : assignmentChanges) {
            // Skip if essential scheduling data is missing
            if (change.getRoomId() == null || change.getDate() == null || 
                change.getSessionId() == null || change.getAssignmentId() == null) {
                continue;
            }
            
            try {
                // First get the current assignment we're updating
                String sql = "SELECT a.id, a.exam_timtabling_class_id, c.exam_class_id " +
                             "FROM exam_timetable_assignment a " +
                             "JOIN exam_timetabling_class c ON a.exam_timtabling_class_id = c.id " +
                             "WHERE CAST(a.id AS VARCHAR) = :assignmentId";
                
                Query query = entityManager.createNativeQuery(sql);
                query.setParameter("assignmentId", change.getAssignmentId());
                
                Object[] currentAssignment = null;
                try {
                    currentAssignment = (Object[]) query.getSingleResult();
                } catch (Exception e) {
                    // Assignment not found
                    continue;
                }
                
                // Find conflicting assignments (same room, date, session but different assignment)
                String conflictSql = 
                    "SELECT a.id, c.exam_class_id, a.exam_timtabling_class_id " +
                    "FROM exam_timetable_assignment a " +
                    "JOIN exam_timetabling_class c ON a.exam_timtabling_class_id = c.id " +
                    "WHERE CAST(a.id AS VARCHAR) != :assignmentId " +
                    "AND a.deleted_at IS NULL " +
                    "AND CAST(a.room_id AS VARCHAR) = :roomId " +
                    "AND CAST(a.exam_session_id AS VARCHAR) = :sessionId " +
                    "AND a.date = :date";
                
                Query conflictQuery = entityManager.createNativeQuery(conflictSql);
                conflictQuery.setParameter("assignmentId", change.getAssignmentId());
                conflictQuery.setParameter("roomId", change.getRoomId());
                conflictQuery.setParameter("sessionId", change.getSessionId());
                
                // Parse date from dd/MM/yyyy format
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate date = LocalDate.parse(change.getDate(), formatter);
                conflictQuery.setParameter("date", date);
                
                List<Object[]> conflictResults = conflictQuery.getResultList();
                
                if (!conflictResults.isEmpty()) {
                    // We found conflicts - create a conflict entry
                    ConflictDTO conflict = new ConflictDTO();
                    
                    // Format week name
                    conflict.setWeekName("W" + change.getWeekNumber());
                    
                    // Set date as is
                    conflict.setDate(change.getDate());
                    
                    // Get room name
                    try {
                        String roomSql = "SELECT name FROM exam_room WHERE CAST(id AS VARCHAR) = :roomId";
                        Query roomQuery = entityManager.createNativeQuery(roomSql);
                        roomQuery.setParameter("roomId", change.getRoomId());
                        String roomName = (String) roomQuery.getSingleResult();
                        conflict.setRoomName(roomName);
                    } catch (Exception e) {
                        conflict.setRoomName("Unknown Room");
                    }
                    
                    // Get session name
                    try {
                        String sessionSql = 
                            "SELECT name, start_time, end_time FROM exam_timetable_session WHERE CAST(id AS VARCHAR) = :sessionId";
                        Query sessionQuery = entityManager.createNativeQuery(sessionSql);
                        sessionQuery.setParameter("sessionId", change.getSessionId());
                        Object[] sessionResult = (Object[]) sessionQuery.getSingleResult();
                        
                        String sessionName = (String) sessionResult[0];
                        Timestamp startTime = (Timestamp) sessionResult[1];
                        Timestamp endTime = (Timestamp) sessionResult[2];
                        
                        // Format session name with times
                        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
                        LocalDateTime startDateTime = startTime.toLocalDateTime();
                        LocalDateTime endDateTime = endTime.toLocalDateTime();
                        
                        String formattedSession = sessionName + " (" + 
                                                 startDateTime.format(timeFormatter) + " - " + 
                                                 endDateTime.format(timeFormatter) + ")";
                        
                        conflict.setSessionName(formattedSession);
                    } catch (Exception e) {
                        conflict.setSessionName("Unknown Session");
                    }
                    
                    // Collect all exam class IDs in conflict
                    List<String> examClassIds = new ArrayList<>();
                    
                    // Add the exam class we're trying to move
                    if (currentAssignment != null && currentAssignment[2] != null) {
                        examClassIds.add(currentAssignment[2].toString());
                    }
                    
                    // Add all conflicting exam classes
                    for (Object[] result : conflictResults) {
                        if (result[1] != null) {
                            examClassIds.add(result[1].toString());
                        }
                    }
                    
                    conflict.setExamClassIds(examClassIds);
                    conflicts.add(conflict);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Continue with next assignment change
            }
        }
        
        return conflicts;
    }
    
    @Transactional
    public void updateAssignments(List<AssignmentUpdateDTO> assignmentChanges) {
        if (assignmentChanges == null || assignmentChanges.isEmpty()) {
            return;
        }
        
        for (AssignmentUpdateDTO change : assignmentChanges) {
            try {
                // Build update query with proper type casting
                StringBuilder queryBuilder = new StringBuilder(
                    "UPDATE exam_timetable_assignment SET updated_at = NOW()");
                
                if (change.getRoomId() != null) {
                    queryBuilder.append(", room_id = CAST(:roomId AS UUID)");
                }
                
                if (change.getSessionId() != null) {
                    queryBuilder.append(", exam_session_id = CAST(:sessionId AS UUID)");
                }
                
                if (change.getWeekNumber() != null) {
                    queryBuilder.append(", week_number = :weekNumber");
                }
                
                if (change.getDate() != null) {
                    queryBuilder.append(", date = :date");
                }
                
                queryBuilder.append(" WHERE CAST(id AS VARCHAR) = :assignmentId");
                
                // Create and execute the query
                Query query = entityManager.createNativeQuery(queryBuilder.toString());
                query.setParameter("assignmentId", change.getAssignmentId());
                
                if (change.getRoomId() != null) {
                    query.setParameter("roomId", change.getRoomId());
                }
                
                if (change.getSessionId() != null) {
                    query.setParameter("sessionId", change.getSessionId());
                }
                
                if (change.getWeekNumber() != null) {
                    query.setParameter("weekNumber", change.getWeekNumber());
                }
                
                if (change.getDate() != null) {
                    // Parse date from dd/MM/yyyy format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate localDate = LocalDate.parse(change.getDate(), formatter);
                    query.setParameter("date", localDate);
                }
                
                query.executeUpdate();
            } catch (Exception e) {
                // Log error but continue with other assignments
                System.err.println("Error updating assignment: " + e.getMessage());
            }
        }
    }

    public List<ExamAssignmentDTO> getAssignmentsByTimetableId(UUID timetableId) {
        // Use native query for better control over the results
        String sql = 
            "SELECT " +
            "  a.id, " +
            "  c.exam_class_id, " +
            "  c.class_id, " +
            "  c.course_id, " +
            "  c.group_id, " +
            "  c.course_name, " +
            "  c.description, " +
            "  c.number_students, " +
            "  c.period, " +
            "  c.management_code, " +
            "  c.school, " +
            "  a.room_id, " +
            "  a.exam_session_id, " +
            "  a.week_number, " +
            "  a.date " +
            "FROM exam_timetable_assignment a " +
            "JOIN exam_timetabling_class c ON a.exam_timtabling_class_id = c.id " +
            "WHERE a.exam_timetable_id = :timetableId " +
            "AND a.deleted_at IS NULL " +
            "ORDER BY c.description";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("timetableId", timetableId);
        
        List<Object[]> results = query.getResultList();
        
        // Map results to DTOs
        return results.stream().map(row -> {
            ExamAssignmentDTO dto = new ExamAssignmentDTO();
            
            dto.setId(row[0] != null ? row[0].toString() : null);
            dto.setExamClassId(row[1] != null ? row[1].toString() : null);
            dto.setClassId(row[2] != null ? row[2].toString() : null);
            dto.setCourseId(row[3] != null ? row[3].toString() : null);
            dto.setGroupId(row[4] != null ? row[4].toString() : null);
            dto.setCourseName(row[5] != null ? row[5].toString() : null);
            dto.setDescription(row[6] != null ? row[6].toString() : null);
            dto.setNumberOfStudents(row[7] != null ? Integer.valueOf(row[7].toString()) : null);
            dto.setPeriod(row[8] != null ? row[8].toString() : null);
            dto.setManagementCode(row[9] != null ? row[9].toString() : null);
            dto.setSchool(row[10] != null ? row[10].toString() : null);
            dto.setRoomId(row[11] != null ? row[11].toString() : null);
            dto.setSessionId(row[12] != null ? row[12].toString() : null);
            dto.setWeekNumber(row[13] != null ? Integer.valueOf(row[13].toString()) : null);
            
            if (row[14] != null) {
                try {
                    LocalDate localDate = null;
                    if (row[14] instanceof java.sql.Date) {
                        localDate = ((java.sql.Date) row[14]).toLocalDate();
                    } else if (row[14] instanceof LocalDate) {
                        localDate = (LocalDate) row[14];
                    } else if (row[14] instanceof java.sql.Timestamp) {
                        localDate = ((java.sql.Timestamp) row[14]).toLocalDateTime().toLocalDate();
                    } else {
                        // Try to parse from string
                        localDate = LocalDate.parse(row[14].toString());
                    }
                    
                    if (localDate != null) {
                        dto.setDate(localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing date: " + e.getMessage());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
}

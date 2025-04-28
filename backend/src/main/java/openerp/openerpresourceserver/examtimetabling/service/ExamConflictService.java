package openerp.openerpresourceserver.examtimetabling.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.entity.ConflictExamTimetablingClass;
import openerp.openerpresourceserver.examtimetabling.entity.ExamClass;
import openerp.openerpresourceserver.examtimetabling.repository.ConflictExamTimetablingClassRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamClassRepository;

@Service
@RequiredArgsConstructor
public class ExamConflictService {
    
    private final EntityManager entityManager;
    private final ExamClassRepository examClassRepository;
    private final ConflictExamTimetablingClassRepository conflictRepository;
    
    public Map<String, Object> getConflictsForPlan(UUID examPlanId) {
      // Get conflict pairs
      String conflictSql = "SELECT " +
                        "c.id as conflict_id, " +  // Added conflict ID
                        "c.exam_timetabling_class_id_1 as class_id_1, " +
                        "c.exam_timetabling_class_id_2 as class_id_2 " +
                        "FROM conflict_exam_timetabling_class c " +
                        "JOIN exam_timetabling_class e1 ON c.exam_timetabling_class_id_1 = e1.id " +
                        "JOIN exam_timetabling_class e2 ON c.exam_timetabling_class_id_2 = e2.id " +
                        "WHERE e1.exam_plan_id = :examPlanId " +
                        "AND e2.exam_plan_id = :examPlanId";
      
      Query conflictQuery = entityManager.createNativeQuery(conflictSql);
      conflictQuery.setParameter("examPlanId", examPlanId);
      
      List<Object[]> conflictResults = conflictQuery.getResultList();
      
      List<Map<String, UUID>> conflictPairs = new ArrayList<>();
      for (Object[] row : conflictResults) {
          UUID conflictId = UUID.fromString(row[0].toString());  // Extract conflict ID
          UUID classId1 = UUID.fromString(row[1].toString());
          UUID classId2 = UUID.fromString(row[2].toString());
          
          conflictPairs.add(Map.of(
              "conflictId", conflictId,  // Include conflict ID in the response
              "classId1", classId1,
              "classId2", classId2
          ));
      }
      
      // Get all exam classes for this plan
      List<ExamClass> examClasses = examClassRepository.findByExamPlanId(examPlanId);
      
      // Return both datasets
      return Map.of(
          "conflicts", conflictPairs,
          "examClasses", examClasses
      );
    }

    @Transactional
    public int deleteConflicts(List<UUID> conflictIds) {
        if (conflictIds == null || conflictIds.isEmpty()) {
            return 0;
        }
        
        String idList = conflictIds.stream()
            .map(id -> "'" + id + "'")
            .collect(Collectors.joining(","));
        
        String sql = "DELETE FROM conflict_exam_timetabling_class WHERE id IN (" + idList + ")";
        Query query = entityManager.createNativeQuery(sql);
        int deletedCount = query.executeUpdate();
        
        return deletedCount;
    }
    
    @Transactional
    public ConflictExamTimetablingClass createConflict(UUID examClassId1, UUID examClassId2) {
        if (examClassId1 == null || examClassId2 == null) {
            throw new IllegalArgumentException("Both exam class IDs must be provided");
        }
        
        if (examClassId1.equals(examClassId2)) {
            throw new IllegalArgumentException("Cannot create a conflict between the same exam class");
        }
        
        boolean class1Exists = examClassRepository.existsById(examClassId1);
        boolean class2Exists = examClassRepository.existsById(examClassId2);
        
        if (!class1Exists || !class2Exists) {
            throw new IllegalArgumentException("One or both of the specified exam classes do not exist");
        }
        
        boolean conflictExists = conflictRepository.existsByExamTimetablingClassId1AndExamTimetablingClassId2(
            examClassId1, examClassId2) || 
            conflictRepository.existsByExamTimetablingClassId1AndExamTimetablingClassId2(
            examClassId2, examClassId1);
            
        if (conflictExists) {
            throw new IllegalArgumentException("A conflict between these exam classes already exists");
        }
        
        ConflictExamTimetablingClass conflict = new ConflictExamTimetablingClass();
        conflict.setId(UUID.randomUUID());
        conflict.setExamTimetablingClassId1(examClassId1);
        conflict.setExamTimetablingClassId2(examClassId2);
        
        return conflictRepository.save(conflict);
    }
}

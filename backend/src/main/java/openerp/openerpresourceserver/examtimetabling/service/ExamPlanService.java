package openerp.openerpresourceserver.examtimetabling.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamPlanStatisticsDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamPlanStatisticsDTO.SchoolStatistic;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamPlanWithSemesterDTO;
import openerp.openerpresourceserver.examtimetabling.entity.ExamPlan;
import openerp.openerpresourceserver.examtimetabling.repository.ExamClassRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamPlanRepository;
import openerp.openerpresourceserver.examtimetabling.repository.SemesterRepository;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Semester;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamPlanService {
  private final ExamPlanRepository examPlanRepository;
  private final ExamClassRepository examClassRepository;
  private final SemesterRepository semesterRepository;
  private final EntityManager entityManager;
  private final ExamTimetableService examTimetableService;


  public List<ExamPlan> getAllExamPlans() {
    return examPlanRepository.findAll();
  }

  public ExamPlanWithSemesterDTO getExamPlanById(UUID id) {
    ExamPlan plan = examPlanRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Exam plan not found with id: " + id));

    if (plan.getSemesterId() == null) {
        throw new RuntimeException("Semester ID is null in ExamPlan");
    }

    Semester semester = semesterRepository.findById(plan.getSemesterId())
        .orElseThrow(() -> new RuntimeException("Semester not found with id: " + plan.getSemesterId()));

    return new ExamPlanWithSemesterDTO(plan, semester);
}

  public ExamPlan createExamPlan(ExamPlan examPlan) {
    examPlan.setId(UUID.randomUUID());

    return examPlanRepository.save(examPlan);
  }

  @Transactional
  public ExamPlan updateExamPlan(UUID id, ExamPlan examPlanDetails) {
    ExamPlan examPlan = examPlanRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Exam plan not found with id: " + id));

    examPlan.setName(examPlanDetails.getName());
    examPlan.setDescription(examPlanDetails.getDescription());
    examPlan.setStartTime(examPlanDetails.getStartTime());
    examPlan.setEndTime(examPlanDetails.getEndTime());

    return examPlanRepository.save(examPlan);
  }

  @Transactional
  public void deleteExamPlan(UUID id) {
      if (!examPlanRepository.existsById(id)) {
          throw new RuntimeException("Exam plan not found with id: " + id);
      }
      
      String timetablesSql = "SELECT id FROM exam_timetable WHERE exam_plan_id = :examPlanId";
      Query timetablesQuery = entityManager.createNativeQuery(timetablesSql);
      timetablesQuery.setParameter("examPlanId", id);
      
      @SuppressWarnings("unchecked")
      List<Object> timetableIds = timetablesQuery.getResultList();
      
      int deletedTimetables = 0;
      
      for (Object timetableIdObj : timetableIds) {
          if (timetableIdObj != null) {
              UUID timetableId = UUID.fromString(timetableIdObj.toString());
              try {
                  examTimetableService.deleteTimetable(timetableId);
                  deletedTimetables++;
              } catch (Exception e) {
                  System.err.println("Error deleting timetable " + timetableId + ": " + e.getMessage());
              }
          }
      }
      
      String deleteClassesSql = "DELETE FROM exam_timetabling_class WHERE exam_plan_id = :examPlanId";
      Query deleteClassesQuery = entityManager.createNativeQuery(deleteClassesSql);
      deleteClassesQuery.setParameter("examPlanId", id);
      int deletedClasses = deleteClassesQuery.executeUpdate();
      
      String deletePlanSql = "DELETE FROM exam_plan WHERE id = :examPlanId";
      Query deletePlanQuery = entityManager.createNativeQuery(deletePlanSql);
      deletePlanQuery.setParameter("examPlanId", id);
      int deletedPlans = deletePlanQuery.executeUpdate();
      
      if (deletedPlans == 0) {
          throw new RuntimeException("Failed to delete exam plan with id: " + id);
      }
  }

  public List<ExamPlan> findAllActivePlans() {
    return examPlanRepository.findByDeleteAtIsNull();
  }

  public ExamPlanStatisticsDTO getExamPlanStatistics(UUID examPlanId) {
    ExamPlan examPlan = examPlanRepository.findById(examPlanId)
        .orElseThrow(() -> new RuntimeException("Exam plan not found with id: " + examPlanId));
    
    long totalCount = examClassRepository.countByExamPlanId(examPlanId);
    
    List<Object[]> schoolCounts = examClassRepository.getSchoolDistributionByExamPlanId(examPlanId);
    
    ExamPlanStatisticsDTO stats = new ExamPlanStatisticsDTO();
    stats.setExamPlanId(examPlanId);
    stats.setExamPlanName(examPlan.getName());
    stats.setTotalExamClasses(totalCount);
    
    List<SchoolStatistic> allSchools = processSchoolCounts(schoolCounts, totalCount);
    
    List<SchoolStatistic> topSchools = new ArrayList<>();
    SchoolStatistic otherSchools = new SchoolStatistic();
    otherSchools.setSchoolName("Others");
    otherSchools.setCount(0);
    otherSchools.setPercentage(0.0);
    
    if (allSchools.size() <= 6) {
      stats.setTopSchools(allSchools);
    } else {
        topSchools = allSchools.subList(0, 6);
        
        long othersCount = 0;
        for (int i = 6; i < allSchools.size(); i++) {
            othersCount += allSchools.get(i).getCount();
        }
        otherSchools.setCount(othersCount);
        otherSchools.setPercentage((double) othersCount * 100 / totalCount);
        
        stats.setTopSchools(topSchools);
        stats.setOtherSchools(otherSchools);
    }
    
    return stats;
  }

  private List<SchoolStatistic> processSchoolCounts(List<Object[]> schoolCounts, long totalCount) {
      return schoolCounts.stream()
          .map(row -> {
              SchoolStatistic stat = new SchoolStatistic();
              stat.setSchoolName((String) row[0]);
              stat.setCount(((Number) row[1]).longValue());
              stat.setPercentage(((double) stat.getCount() * 100) / totalCount);
              return stat;
          })
          .sorted((s1, s2) -> Long.compare(s2.getCount(), s1.getCount())) 
          .collect(Collectors.toList());
  }
}

package openerp.openerpresourceserver.examtimetabling.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDetailDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDetailDTO.DateDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDetailDTO.SlotDTO;
import openerp.openerpresourceserver.examtimetabling.entity.ExamPlan;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetable;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableSession;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableSessionCollection;
import openerp.openerpresourceserver.examtimetabling.repository.ExamClassRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamPlanRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamTimetableAssignmentRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamTimetableRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamTimetableSessionCollectionRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamTimetableSessionRepository;

@Service
@RequiredArgsConstructor
public class ExamTimetableService {
    private final ExamTimetableRepository examTimetableRepository;
    private final ExamClassRepository examClassRepository;
    private final ExamTimetableAssignmentRepository examTimetableAssignmentRepository;
    private final EntityManager entityManager;
    private final ExamPlanRepository examPlanRepository;
    private final ExamTimetableAssignmentRepository assignmentRepository;
    private final ExamTimetableSessionCollectionRepository sessionCollectionRepository;
    private final ExamTimetableSessionRepository sessionRepository;
    
    @Transactional
    public ExamTimetable createExamTimetable(ExamTimetable examTimetable) {
        // Generate UUID if not provided
        if (examTimetable.getId() == null) {
            examTimetable.setId(UUID.randomUUID());
        }
        
        // Set timestamps and save timetable
        LocalDateTime now = LocalDateTime.now();
        examTimetable.setCreatedAt(now);
        examTimetable.setUpdatedAt(now);
        ExamTimetable savedTimetable = examTimetableRepository.save(examTimetable);
        
        // Use native SQL for bulk insert
        String insertSql = "INSERT INTO exam_timetable_assignment " +
                          "(id, exam_timetable_id, exam_timtabling_class_id, created_at, updated_at) " +
                          "SELECT uuid_generate_v4(), :timetableId, id, :createdAt, :updatedAt " +
                          "FROM exam_timetabling_class " +
                          "WHERE exam_plan_id = :examPlanId";
        
        Query query = entityManager.createNativeQuery(insertSql);
        query.setParameter("timetableId", savedTimetable.getId());
        query.setParameter("examPlanId", examTimetable.getExamPlanId());
        query.setParameter("createdAt", now);
        query.setParameter("updatedAt", now);
        
        query.executeUpdate();
        
        return savedTimetable;
    }
    
    public List<ExamTimetableDTO> getAllTimetablesByExamPlanId(UUID examPlanId) {
        // Get total count of exam classes for this plan
        long totalClasses = examClassRepository.countByExamPlanId(examPlanId);
        
        // Get all timetables for this plan
        List<ExamTimetable> timetables = examTimetableRepository.findByExamPlanIdAndDeletedAtIsNull(examPlanId);
        
        // Map to DTOs with progress calculation
        return timetables.stream().map(timetable -> {
            ExamTimetableDTO dto = new ExamTimetableDTO();
            dto.setId(timetable.getId());
            dto.setName(timetable.getName());
            dto.setExamPlanId(timetable.getExamPlanId());
            dto.setCreatedAt(timetable.getCreatedAt());
            dto.setUpdatedAt(timetable.getUpdatedAt());
            
            // Calculate progress
            long completedAssignments = examTimetableAssignmentRepository
                .countByExamTimetableIdAndRoomIdIsNotNullAndExamSessionIdIsNotNull(timetable.getId());
            
            double progressPercentage = totalClasses > 0 
                ? ((double) completedAssignments / totalClasses) * 100 
                : 0.0;
            
            dto.setProgressPercentage(Math.round(progressPercentage * 100.0) / 100.0); // Round to 2 decimal places
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void softDeleteTimetable(UUID timetableId) {
        // First verify the timetable exists
        ExamTimetable timetable = examTimetableRepository.findById(timetableId)
            .orElseThrow(() -> new RuntimeException("Timetable not found with id: " + timetableId));
        
        // Use native query to soft delete all assignments
        Query assignmentsQuery = entityManager.createNativeQuery(
            "UPDATE exam_timetable_assignment " +
            "SET deleted_at = NOW() " +
            "WHERE exam_timetable_id = :timetableId AND deleted_at IS NULL"
        );
        
        assignmentsQuery.setParameter("timetableId", timetableId);
        assignmentsQuery.executeUpdate();
        
        // Soft delete the timetable
        timetable.setDeletedAt(LocalDateTime.now());
        examTimetableRepository.save(timetable);
    }

    @Transactional
    public ExamTimetable updateTimetable(UUID timetableId, String name) {
        ExamTimetable timetable = examTimetableRepository.findById(timetableId)
            .orElseThrow(() -> new RuntimeException("Timetable not found with id: " + timetableId));
        
        // Update only the name
        timetable.setName(name);
        
        // Update timestamp
        timetable.setUpdatedAt(LocalDateTime.now());
        
        return examTimetableRepository.save(timetable);
    }

    public ExamTimetableDetailDTO getTimetableDetail(UUID timetableId) {
        // Get timetable
        ExamTimetable timetable = examTimetableRepository.findById(timetableId)
            .orElseThrow(() -> new RuntimeException("Timetable not found"));
            
        // Get exam plan
        ExamPlan plan = examPlanRepository.findById(timetable.getExamPlanId())
            .orElseThrow(() -> new RuntimeException("Exam plan not found"));
            
        // Get session collection and sessions
        ExamTimetableSessionCollection collection = sessionCollectionRepository
            .findById(timetable.getExamTimetableSessionCollectionId())
            .orElseThrow(() -> new RuntimeException("Session collection not found"));
            
        List<ExamTimetableSession> sessions = sessionRepository
            .findByExamTimetableSessionCollectionId(collection.getId());
            
        // Count completed and total assignments
        long completedAssignments = assignmentRepository
            .countByExamTimetableIdAndRoomIdIsNotNullAndExamSessionIdIsNotNull(timetable.getId());
        
        long totalAssignments = assignmentRepository
            .countByExamTimetableIdAndDeletedAtIsNull(timetable.getId());
            
        // Build response DTO
        ExamTimetableDetailDTO detailDTO = new ExamTimetableDetailDTO();
        detailDTO.setId(timetable.getId());
        detailDTO.setName(timetable.getName());
        detailDTO.setExamPlanId(plan.getId());
        detailDTO.setPlanStartWeek(plan.getStartWeek());
        detailDTO.setPlanStartTime(plan.getStartTime());
        detailDTO.setPlanEndTime(plan.getEndTime());
        detailDTO.setSessionCollectionId(collection.getId());
        detailDTO.setSessionCollectionName(collection.getName());
        detailDTO.setCreatedAt(timetable.getCreatedAt());
        detailDTO.setUpdatedAt(timetable.getUpdatedAt());
        detailDTO.setCompletedAssignments(completedAssignments);
        detailDTO.setTotalAssignments(totalAssignments);
        
        // Generate weeks - now as a list of integers
        List<Integer> weeks = generateWeekNumbers(plan.getStartWeek(), 
                                                plan.getStartTime().toLocalDate(), 
                                                plan.getEndTime().toLocalDate());
        detailDTO.setWeeks(weeks);
        
        // Generate dates - modified format
        List<DateDTO> dates = generateDatesNew(
            plan.getStartTime().toLocalDate(),
            plan.getEndTime().toLocalDate(),
            plan.getStartWeek()  // Pass startWeek to ensure consistent numbering
        );
        
        detailDTO.setDates(dates);
        
        // Generate slots from sessions - modified format
        List<SlotDTO> slots = sessions.stream().map(session -> {
            SlotDTO dto = new SlotDTO();
            dto.setId(session.getId());  // Now returning UUID directly
            
            // Format time for display
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
            String startTimeStr = session.getStartTime().format(timeFormatter);
            String endTimeStr = session.getEndTime().format(timeFormatter);
            dto.setName(session.getName() + " (" + startTimeStr + " - " + endTimeStr + ")");
            
            return dto;
        }).collect(Collectors.toList());
        
        detailDTO.setSlots(slots);
        
        return detailDTO;
    }

    // New methods for the updated DTO structure
    private List<Integer> generateWeekNumbers(Integer startWeek, LocalDate startDate, LocalDate endDate) {
        List<Integer> weeks = new ArrayList<>();
        
        // Calculate the start of the week containing the startDate
        LocalDate firstDayOfStartWeek = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        LocalDate currentWeekStart = firstDayOfStartWeek;
        int currentWeek = startWeek;
        
        while (!currentWeekStart.isAfter(endDate)) {
            weeks.add(currentWeek);
            
            // Move to next week
            currentWeekStart = currentWeekStart.plusWeeks(1);
            currentWeek++;
        }
        
        return weeks;
    }
    private List<DateDTO> generateDatesNew(LocalDate startDate, LocalDate endDate, Integer startWeek) {
        List<DateDTO> dates = new ArrayList<>();
        
        // Calculate the start of the week containing the startDate
        LocalDate firstDayOfStartWeek = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            DateDTO dateDTO = new DateDTO();
            
            // Calculate which week this date belongs to
            LocalDate mondayOfThisWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            int weeksDiff = (int) ChronoUnit.WEEKS.between(firstDayOfStartWeek, mondayOfThisWeek);
            int weekNumber = startWeek + weeksDiff;
            
            dateDTO.setWeekNumber(weekNumber);
            
            String dayOfWeek;
            switch (currentDate.getDayOfWeek()) {
                case MONDAY: dayOfWeek = "T2"; break;
                case TUESDAY: dayOfWeek = "T3"; break;
                case WEDNESDAY: dayOfWeek = "T4"; break;
                case THURSDAY: dayOfWeek = "T5"; break;
                case FRIDAY: dayOfWeek = "T6"; break;
                case SATURDAY: dayOfWeek = "T7"; break;
                case SUNDAY: dayOfWeek = "CN"; break;
                default: dayOfWeek = ""; break;
            }
            
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dateDTO.setName(dayOfWeek + " (" + dateStr + ")");
            dateDTO.setDate(dateStr);
            
            dates.add(dateDTO);
            
            // Move to next day
            currentDate = currentDate.plusDays(1);
        }
        
        return dates;
    }
}

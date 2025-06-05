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
import openerp.openerpresourceserver.examtimetabling.dtos.DistributionItemDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDetailDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.GroupAssignmentStatsDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDetailDTO.DateDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDetailDTO.SlotDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.TimetableStatisticsDTO;
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
        if (examTimetable.getId() == null) {
            examTimetable.setId(UUID.randomUUID());
        }
        
        LocalDateTime now = LocalDateTime.now();
        examTimetable.setCreatedAt(now);
        ExamTimetable savedTimetable = examTimetableRepository.save(examTimetable);
        
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
        long totalClasses = examClassRepository.countByExamPlanId(examPlanId);
        
        List<ExamTimetable> timetables = examTimetableRepository.findByExamPlanId(examPlanId);
        
        return timetables.stream().map(timetable -> {
            ExamTimetableDTO dto = new ExamTimetableDTO();
            dto.setId(timetable.getId());
            dto.setName(timetable.getName());
            dto.setExamPlanId(timetable.getExamPlanId());
            dto.setCreatedAt(timetable.getCreatedAt());
            
            long completedAssignments = examTimetableAssignmentRepository
                .countByExamTimetableIdAndRoomIdIsNotNullAndExamSessionIdIsNotNull(timetable.getId());
            
            double progressPercentage = totalClasses > 0 
                ? ((double) completedAssignments / totalClasses) * 100 
                : 0.0;
            
            dto.setProgressPercentage(Math.round(progressPercentage * 100.0) / 100.0);
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteTimetable(UUID timetableId) {
        if (!examTimetableRepository.existsById(timetableId)) {
            throw new RuntimeException("Timetable not found with id: " + timetableId);
        }
        
        String deleteAssignmentsSql = "DELETE FROM exam_timetable_assignment " +
                                    "WHERE exam_timetable_id = :timetableId";
        
        Query deleteAssignmentsQuery = entityManager.createNativeQuery(deleteAssignmentsSql);
        deleteAssignmentsQuery.setParameter("timetableId", timetableId);
        int deletedAssignments = deleteAssignmentsQuery.executeUpdate();
        
        String deleteTimetableSql = "DELETE FROM exam_timetable WHERE id = :timetableId";
        
        Query deleteTimetableQuery = entityManager.createNativeQuery(deleteTimetableSql);
        deleteTimetableQuery.setParameter("timetableId", timetableId);
        int deletedTimetables = deleteTimetableQuery.executeUpdate();
        
        if (deletedTimetables == 0) {
            throw new RuntimeException("Failed to delete timetable with id: " + timetableId);
        }
    }

    @Transactional
    public ExamTimetable updateTimetable(UUID timetableId, String name) {
        ExamTimetable timetable = examTimetableRepository.findById(timetableId)
            .orElseThrow(() -> new RuntimeException("Timetable not found with id: " + timetableId));
        
        // Update only the name
        timetable.setName(name);
        
        // Update timestamp
        
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
            .countByExamTimetableId(timetable.getId());
            
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
        detailDTO.setCompletedAssignments(completedAssignments);
        detailDTO.setTotalAssignments(totalAssignments);
        
        List<Integer> weeks = generateWeekNumbers(plan.getStartWeek(), 
                                                plan.getStartTime().toLocalDate(), 
                                                plan.getEndTime().toLocalDate());
        detailDTO.setWeeks(weeks);
        
        List<DateDTO> dates = generateDatesNew(
            plan.getStartTime().toLocalDate(),
            plan.getEndTime().toLocalDate(),
            plan.getStartWeek()  
        );
        
        detailDTO.setDates(dates);
        
        List<SlotDTO> slots = sessions.stream().map(session -> {
            SlotDTO dto = new SlotDTO();
            dto.setId(session.getId()); 
            
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
            String startTimeStr = session.getStartTime().format(timeFormatter);
            String endTimeStr = session.getEndTime().format(timeFormatter);
            dto.setName(session.getName() + " (" + startTimeStr + " - " + endTimeStr + ")");
            
            return dto;
        }).collect(Collectors.toList());
        
        detailDTO.setSlots(slots);
        
        return detailDTO;
    }

    private List<Integer> generateWeekNumbers(Integer startWeek, LocalDate startDate, LocalDate endDate) {
        List<Integer> weeks = new ArrayList<>();
        
        LocalDate firstDayOfStartWeek = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        LocalDate currentWeekStart = firstDayOfStartWeek;
        int currentWeek = startWeek;
        
        while (!currentWeekStart.isAfter(endDate)) {
            weeks.add(currentWeek);
            
            currentWeekStart = currentWeekStart.plusWeeks(1);
            currentWeek++;
        }
        
        return weeks;
    }
    private List<DateDTO> generateDatesNew(LocalDate startDate, LocalDate endDate, Integer startWeek) {
        List<DateDTO> dates = new ArrayList<>();
        
        LocalDate firstDayOfStartWeek = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            DateDTO dateDTO = new DateDTO();
            
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

    public TimetableStatisticsDTO generateStatistics(UUID timetableId) {
        ExamTimetable timetable = examTimetableRepository.findById(timetableId)
            .orElseThrow(() -> new RuntimeException("Timetable not found"));
        
        ExamPlan examPlan = examPlanRepository.findById(timetable.getExamPlanId())
            .orElseThrow(() -> new RuntimeException("Exam plan not found"));
            
        ExamTimetableSessionCollection sessionCollection = null;
        String sessionCollectionName = "Unknown";
        if (timetable.getExamTimetableSessionCollectionId() != null) {
            sessionCollection = sessionCollectionRepository
                .findById(timetable.getExamTimetableSessionCollectionId())
                .orElse(null);
            if (sessionCollection != null) {
                sessionCollectionName = sessionCollection.getName();
            }
        }
        
        long totalExamDays = 0;
        if (examPlan.getStartTime() != null && examPlan.getEndTime() != null) {
            LocalDate startDate = examPlan.getStartTime().toLocalDate();
            LocalDate endDate = examPlan.getEndTime().toLocalDate();
            
            totalExamDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            totalExamDays = totalExamDays - countWeekendDays(startDate, endDate);
        }
        
        TimetableStatisticsDTO statistics = new TimetableStatisticsDTO();
        statistics.setTimetableId(timetableId);
        statistics.setTimetableName(timetable.getName());
        statistics.setTotalExamDays(totalExamDays);
        statistics.setSessionCollectionName(sessionCollectionName);
        
        // Calculate total rooms used
        calculateTotalRoomsUsed(timetableId, statistics);
        
        // Calculate completion rate
        calculateCompletionRate(timetableId, statistics);
        
        // Calculate session distribution
        calculateSessionDistribution(timetableId, statistics);
        
        // Calculate room distribution
        calculateRoomDistribution(timetableId, statistics);

        // Calculate daily distribution
        calculateDailyDistribution(timetableId, statistics);
        
        // Calculate small room assignments
        calculateSmallRoomAssignments(timetableId, statistics);
        
        // Calculate building distribution
        calculateBuildingDistribution(timetableId, statistics);
        
        // Calculate group assignment statistics
        calculateGroupAssignmentStats(timetableId, statistics);
        
        return statistics;
    }

    private void calculateDailyDistribution(UUID timetableId, TimetableStatisticsDTO statistics) {
        String sql = "SELECT " +
                    "TO_CHAR(a.date, 'DD/MM/YYYY') as exam_date, " +
                    "COUNT(a.id) as assignment_count " +
                    "FROM exam_timetable_assignment a " +
                    "WHERE a.exam_timetable_id = :timetableId " +
                    "AND a.room_id IS NOT NULL " +
                    "AND a.date IS NOT NULL " +
                    "GROUP BY a.date " +
                    "ORDER BY a.date";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("timetableId", timetableId);
        
        List<Object[]> results = query.getResultList();
        
        List<DistributionItemDTO> dailyDistribution = new ArrayList<>();
        
        if (results.size() > 20) {
            results.sort((a, b) -> Long.compare(
                ((Number) b[1]).longValue(),
                ((Number) a[1]).longValue()
            ));
            
            long otherCount = 0;
            
            for (int i = 0; i < results.size(); i++) {
                Object[] row = results.get(i);
                if (i < 19) {
                    dailyDistribution.add(new DistributionItemDTO(
                        row[0].toString(),
                        ((Number) row[1]).longValue()
                    ));
                } else {
                    otherCount += ((Number) row[1]).longValue();
                }
            }
            
            if (otherCount > 0) {
                dailyDistribution.add(new DistributionItemDTO("Other", otherCount));
            }
            
            if (dailyDistribution.size() > 1) {
                dailyDistribution.subList(0, dailyDistribution.size() - 1).sort((a, b) -> {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate dateA = LocalDate.parse(a.getName(), formatter);
                        LocalDate dateB = LocalDate.parse(b.getName(), formatter);
                        return dateA.compareTo(dateB);
                    } catch (Exception e) {
                        return a.getName().compareTo(b.getName());
                    }
                });
            }
        } else {
            dailyDistribution = results.stream()
                .map(row -> new DistributionItemDTO(
                    row[0].toString(),
                    ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
        }
        
        statistics.setDailyDistribution(dailyDistribution);
    }

    private void calculateTotalRoomsUsed(UUID timetableId, TimetableStatisticsDTO statistics) {
        String totalRoomsSql = "SELECT COUNT(*) FROM timetabling_classroom";
        Query totalRoomsQuery = entityManager.createNativeQuery(totalRoomsSql);
        Number totalRoomsResult = (Number) totalRoomsQuery.getSingleResult();
        long totalRooms = totalRoomsResult.longValue();
        
        String usedRoomsSql = "SELECT COUNT(DISTINCT room_id) " +
                     "FROM exam_timetable_assignment " +
                     "WHERE exam_timetable_id = :timetableId " +
                     "AND room_id IS NOT NULL";
        
        Query usedRoomsQuery = entityManager.createNativeQuery(usedRoomsSql);
        usedRoomsQuery.setParameter("timetableId", timetableId);
        
        Number usedRoomsResult = (Number) usedRoomsQuery.getSingleResult();
        long usedRooms = usedRoomsResult.longValue();
        
        statistics.setTotalAvailableRooms(totalRooms);
        statistics.setUsedRoomsCount(usedRooms);
    }

    private int countWeekendDays(LocalDate startDate, LocalDate endDate) {
        int weekendDays = 0;
        LocalDate date = startDate;
        
        while (!date.isAfter(endDate)) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                weekendDays++;
            }
            date = date.plusDays(1);
        }
        
        return weekendDays;
    }
    
    private void calculateCompletionRate(UUID timetableId, TimetableStatisticsDTO statistics) {
        String sql = "SELECT " +
                    "COUNT(*) as total, " +
                    "COUNT(CASE WHEN room_id IS NOT NULL AND exam_session_id IS NOT NULL THEN 1 END) as assigned " +
                    "FROM exam_timetable_assignment " +
                    "WHERE exam_timetable_id = :timetableId";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("timetableId", timetableId);
        
        Object[] result = (Object[]) query.getSingleResult();
        long total = ((Number) result[0]).longValue();
        long assigned = ((Number) result[1]).longValue();
        
        double completionRate = total > 0 ? (double) assigned / total * 100 : 0;
        statistics.setTotalClasses(total);
        statistics.setAssignedClasses(assigned);
        statistics.setCompletionRate(Math.round(completionRate * 100) / 100.0); 
    }

    private void calculateGroupAssignmentStats(UUID timetableId, TimetableStatisticsDTO statistics) {
        // Get the basic statistics first
        String basicStatsSql = "SELECT " +
                    "c.description as group_name, " +
                    "COUNT(a.id) as total_classes, " +
                    "SUM(CASE WHEN a.room_id IS NOT NULL AND a.exam_session_id IS NOT NULL THEN 1 ELSE 0 END) as assigned_classes, " +
                    "SUM(CASE WHEN a.room_id IS NULL OR a.exam_session_id IS NULL THEN 1 ELSE 0 END) as unassigned_classes " +
                    "FROM exam_timetable_assignment a " +
                    "JOIN exam_timetabling_class c ON a.exam_timtabling_class_id = c.id " +
                    "WHERE a.exam_timetable_id = :timetableId " +
                    "GROUP BY c.description " +
                    "ORDER BY c.description";
        
        Query basicStatsQuery = entityManager.createNativeQuery(basicStatsSql);
        basicStatsQuery.setParameter("timetableId", timetableId);
        
        List<Object[]> basicStatsResults = basicStatsQuery.getResultList();
        
        List<GroupAssignmentStatsDTO> groupStats = new ArrayList<>();
        
        for (Object[] row : basicStatsResults) {
            String groupName = row[0] != null ? row[0].toString() : "Unknown";
            long totalClasses = ((Number) row[1]).longValue();
            long assignedClasses = ((Number) row[2]).longValue();
            long unassignedClasses = ((Number) row[3]).longValue();
            double completionRate = totalClasses > 0 ? (double) assignedClasses / totalClasses * 100 : 0;
            
            // Calculate average relax time between courses and days with multiple exams
            double averageRelaxTime = 0;
            int daysWithMultipleExams = 0;
            Map<String, Integer> examsPerDayDistribution = new HashMap<>(); 
            
            if (assignedClasses > 0) {
                // Query to get one exam class per course_id for this description (group)
                String courseDatesSql = "WITH RankedAssignments AS ( " +
                                    "SELECT " +
                                    "c.course_id, " +
                                    "a.date, " +
                                    "ROW_NUMBER() OVER (PARTITION BY c.course_id ORDER BY a.date) as rn " +
                                    "FROM exam_timetable_assignment a " +
                                    "JOIN exam_timetabling_class c ON a.exam_timtabling_class_id = c.id " +
                                    "WHERE a.exam_timetable_id = :timetableId " +
                                    "AND c.description = :description " +
                                    "AND a.date IS NOT NULL " +
                                    ") " +
                                    "SELECT course_id, date FROM RankedAssignments WHERE rn = 1 " +
                                    "ORDER BY date";
                                    
                Query courseDatesQuery = entityManager.createNativeQuery(courseDatesSql);
                courseDatesQuery.setParameter("timetableId", timetableId);
                courseDatesQuery.setParameter("description", groupName);
                
                List<Object[]> courseDatesResults = courseDatesQuery.getResultList();
                
                // Calculate average relax time between different course exams
                if (courseDatesResults.size() > 1) {
                    List<Long> daysBetweenExams = new ArrayList<>();
                    Map<LocalDate, Integer> examsPerDay = new HashMap<>();
                    
                    LocalDate previousDate = null;
                    for (Object[] dateRow : courseDatesResults) {
                        LocalDate examDate;
                        
                        if (dateRow[1] instanceof java.sql.Date) {
                            examDate = ((java.sql.Date) dateRow[1]).toLocalDate();
                        } else if (dateRow[1] instanceof LocalDate) {
                            examDate = (LocalDate) dateRow[1];
                        } else {
                            continue;
                        }
                        
                        // Count exams per day for the distribution map
                        examsPerDay.put(examDate, examsPerDay.getOrDefault(examDate, 0) + 1);
                        
                        if (previousDate != null) {
                            long daysBetween = ChronoUnit.DAYS.between(previousDate, examDate);
                            if (daysBetween > 0) { // Only count if on different days
                                daysBetweenExams.add(daysBetween);
                            }
                        }
                        previousDate = examDate;
                    }
                    
                    if (!daysBetweenExams.isEmpty()) {
                        averageRelaxTime = daysBetweenExams.stream().mapToLong(Long::longValue).average().orElse(0);
                    }
                    
                    // Count days with multiple exams
                    daysWithMultipleExams = (int) examsPerDay.values().stream().filter(count -> count > 1).count();
                    
                    // Convert the examsPerDay map to the examsPerDayDistribution with formatted dates
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    for (Map.Entry<LocalDate, Integer> entry : examsPerDay.entrySet()) {
                        String formattedDate = entry.getKey().format(dateFormatter);
                        examsPerDayDistribution.put(formattedDate, entry.getValue());
                    }
                }
            }
            
            groupStats.add(new GroupAssignmentStatsDTO(
                groupName,
                totalClasses,
                assignedClasses,
                unassignedClasses,
                completionRate,
                Math.round(averageRelaxTime * 100) / 100.0, 
                daysWithMultipleExams,
                examsPerDayDistribution 
            ));
        }
        
        statistics.setGroupAssignmentStats(groupStats);
    }
    
    private void calculateSessionDistribution(UUID timetableId, TimetableStatisticsDTO statistics) {
        String sql = "SELECT " +
                    "s.name as session_name, " +
                    "COUNT(a.id) as assignment_count " +
                    "FROM exam_timetable_assignment a " +
                    "JOIN exam_timetable_session s ON a.exam_session_id = s.id " +
                    "WHERE a.exam_timetable_id = :timetableId " +
                    "AND a.room_id IS NOT NULL " +
                    "GROUP BY s.name " +
                    "ORDER BY assignment_count DESC";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("timetableId", timetableId);
        
        List<Object[]> results = query.getResultList();
        
        List<DistributionItemDTO> sessionDistribution = results.stream()
            .map(row -> new DistributionItemDTO(
                row[0].toString(),
                ((Number) row[1]).longValue()
            ))
            .collect(Collectors.toList());
        
        statistics.setSessionDistribution(sessionDistribution);
    }
    
    private void calculateRoomDistribution(UUID timetableId, TimetableStatisticsDTO statistics) {
        String sql = "SELECT " +
                    "r.classroom as room_name, " +
                    "COUNT(a.id) as assignment_count " +
                    "FROM exam_timetable_assignment a " +
                    "JOIN timetabling_classroom r ON a.room_id = r.classroom_id " +
                    "WHERE a.exam_timetable_id = :timetableId " +
                    "GROUP BY r.classroom " +
                    "ORDER BY assignment_count DESC";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("timetableId", timetableId);
        
        List<Object[]> results = query.getResultList();
        
        List<DistributionItemDTO> roomDistribution = new ArrayList<>();
        
        if (results.size() > 40) {
            long otherCount = 0;
            
            for (int i = 0; i < results.size(); i++) {
                Object[] row = results.get(i);
                if (i < 39) {
                    roomDistribution.add(new DistributionItemDTO(
                        row[0].toString(),
                        ((Number) row[1]).longValue()
                    ));
                } else {
                    otherCount += ((Number) row[1]).longValue();
                }
            }
            
            if (otherCount > 0) {
                roomDistribution.add(new DistributionItemDTO("Other", otherCount));
            }
        } else {
            roomDistribution = results.stream()
                .map(row -> new DistributionItemDTO(
                    row[0].toString(),
                    ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
        }
        
        statistics.setRoomDistribution(roomDistribution);
    }
    
    
    private void calculateSmallRoomAssignments(UUID timetableId, TimetableStatisticsDTO statistics) {
        String sql = "SELECT " +
                    "COUNT(a.id) " +
                    "FROM exam_timetable_assignment a " +
                    "JOIN timetabling_classroom r ON a.room_id = r.classroom_id " +
                    "JOIN exam_timetabling_class c ON a.exam_timtabling_class_id = c.id " +
                    "WHERE a.exam_timetable_id = :timetableId " +
                    "AND r.quantity_max < c.number_students * 2";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("timetableId", timetableId);
        
        Number result = (Number) query.getSingleResult();
        long smallRoomAssignments = result.longValue();
        
        statistics.setSmallRoomAssignments(smallRoomAssignments);
    }
    
    private void calculateBuildingDistribution(UUID timetableId, TimetableStatisticsDTO statistics) {
        String sql = "SELECT " +
                    "r.building_id as building_name, " +
                    "COUNT(a.id) as assignment_count " +
                    "FROM exam_timetable_assignment a " +
                    "JOIN timetabling_classroom r ON a.room_id = r.classroom_id " +
                    "WHERE a.exam_timetable_id = :timetableId " +
                    "GROUP BY building_name " +
                    "ORDER BY assignment_count DESC";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("timetableId", timetableId);
        
        List<Object[]> results = query.getResultList();
        
        List<DistributionItemDTO> buildingDistribution = results.stream()
            .map(row -> new DistributionItemDTO(
                row[0] != null ? row[0].toString() : "Unknown",
                ((Number) row[1]).longValue()
            ))
            .collect(Collectors.toList());
        
        statistics.setBuildingDistribution(buildingDistribution);
    }
}

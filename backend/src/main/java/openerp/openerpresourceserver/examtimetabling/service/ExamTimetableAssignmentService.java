package openerp.openerpresourceserver.examtimetabling.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.AssignmentResultDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.AssignmentUpdateDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.BusyCombinationDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ConflictDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ConflictResponseDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamAssignmentDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamRoom;
import openerp.openerpresourceserver.examtimetabling.dtos.ScheduleSlotDTO;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetable;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableAssignment;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableSession;
import openerp.openerpresourceserver.examtimetabling.repository.ClassroomRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamTimetableAssignmentRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamTimetableRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamTimetableSessionRepository;

@Service
@RequiredArgsConstructor
public class ExamTimetableAssignmentService {
    private final ExamTimetableAssignmentRepository assignmentRepository;
    private final ExamTimetableRepository examTimetableRepository;
    private final ClassroomRepository roomRepository;
    private final ExamTimetableSessionRepository sessionRepository;
    private final EntityManager entityManager;
    
    public List<ConflictDTO> checkForConflicts(List<AssignmentUpdateDTO> assignmentChanges) {
        if (assignmentChanges == null || assignmentChanges.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<ConflictDTO> conflicts = new ArrayList<>();
        
        for (AssignmentUpdateDTO change : assignmentChanges) {
            if (change.getRoomId() == null || change.getDate() == null || 
                change.getSessionId() == null || change.getAssignmentId() == null ||
                change.getTimetableId() == null) { 
                    System.err.println("Skipping assignment change with missing data");
                    continue;
            }
            
            try {
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
                    continue;
                }
                
                String conflictSql = 
                    "SELECT a.id, c.exam_class_id, a.exam_timtabling_class_id " +
                    "FROM exam_timetable_assignment a " +
                    "JOIN exam_timetabling_class c ON a.exam_timtabling_class_id = c.id " +
                    "WHERE CAST(a.id AS VARCHAR) != :assignmentId " +
                    "AND a.deleted_at IS NULL " +
                    "AND CAST(a.room_id AS VARCHAR) = :roomId " +
                    "AND CAST(a.exam_session_id AS VARCHAR) = :sessionId " +
                    "AND a.date = :date " +
                    "AND CAST(a.exam_timetable_id AS VARCHAR) = :timetableId"; 
                
                Query conflictQuery = entityManager.createNativeQuery(conflictSql);
                conflictQuery.setParameter("assignmentId", change.getAssignmentId());
                conflictQuery.setParameter("roomId", change.getRoomId());
                conflictQuery.setParameter("sessionId", change.getSessionId());
                conflictQuery.setParameter("timetableId", change.getTimetableId().toString());
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate date = LocalDate.parse(change.getDate(), formatter);
                conflictQuery.setParameter("date", date);
                
                List<Object[]> conflictResults = conflictQuery.getResultList();
                
                if (!conflictResults.isEmpty()) {
                    ConflictDTO conflict = new ConflictDTO();
                    
                    conflict.setWeekName("W" + change.getWeekNumber());
                    
                    conflict.setDate(change.getDate());
                    
                    try {
                        String roomSql = "SELECT name FROM exam_room WHERE CAST(id AS VARCHAR) = :roomId";
                        Query roomQuery = entityManager.createNativeQuery(roomSql);
                        roomQuery.setParameter("roomId", change.getRoomId());
                        String roomName = (String) roomQuery.getSingleResult();
                        conflict.setRoomName(roomName);
                    } catch (Exception e) {
                        conflict.setRoomName("Unknown Room");
                    }
                    
                    try {
                        String sessionSql = 
                            "SELECT name, start_time, end_time FROM exam_timetable_session WHERE CAST(id AS VARCHAR) = :sessionId";
                        Query sessionQuery = entityManager.createNativeQuery(sessionSql);
                        sessionQuery.setParameter("sessionId", change.getSessionId());
                        Object[] sessionResult = (Object[]) sessionQuery.getSingleResult();
                        
                        String sessionName = (String) sessionResult[0];
                        Timestamp startTime = (Timestamp) sessionResult[1];
                        Timestamp endTime = (Timestamp) sessionResult[2];
                        
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
                    
                    List<String> examClassIds = new ArrayList<>();
                    
                    if (currentAssignment != null && currentAssignment[2] != null) {
                        examClassIds.add(currentAssignment[2].toString());
                    }
                    
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
                StringBuilder queryBuilder = new StringBuilder(
                    "UPDATE exam_timetable_assignment SET updated_at = NOW()");
                
                if (change.getRoomId() != null) {
                    queryBuilder.append(", room_id = CAST(:roomId AS TEXT)");
                }
                
                if (change.getSessionId() != null) {
                    queryBuilder.append(", exam_session_id = CAST(:sessionId AS UUID)");
                    queryBuilder.append(", session = (SELECT name FROM exam_timetable_session WHERE id = CAST(:sessionId AS UUID))");
                }
                
                if (change.getWeekNumber() != null) {
                    queryBuilder.append(", week_number = :weekNumber");
                }
                
                if (change.getDate() != null) {
                    queryBuilder.append(", date = :date");
                }
                
                queryBuilder.append(" WHERE CAST(id AS VARCHAR) = :assignmentId");
                
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
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate localDate = LocalDate.parse(change.getDate(), formatter);
                    query.setParameter("date", localDate);
                }
                
                query.executeUpdate();
            } catch (Exception e) {
                System.err.println("Error updating assignment: " + e.getMessage());
            }
        }
    }

    public List<ExamAssignmentDTO> getAssignmentsByTimetableId(UUID timetableId) {
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
            "  a.date, " +
            "  c.id " +
            "FROM exam_timetable_assignment a " +
            "JOIN exam_timetabling_class c ON a.exam_timtabling_class_id = c.id " +
            "WHERE a.exam_timetable_id = :timetableId " +
            "AND a.deleted_at IS NULL " +
            "ORDER BY c.description";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("timetableId", timetableId);
        
        List<Object[]> results = query.getResultList();
        
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
            dto.setExamTimetableClassId(row[15] != null ? row[15].toString() : null);
            
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

    @Transactional
    public int unassignAssignments(List<UUID> assignmentIds) {
        if (assignmentIds == null || assignmentIds.isEmpty()) {
            return 0;
        }
        
        String idList = assignmentIds.stream()
            .map(id -> "'" + id + "'")
            .collect(Collectors.joining(","));
        
        String sql = "UPDATE exam_timetable_assignment " +
                     "SET room_id = NULL, " +
                     "    exam_session_id = NULL, " +
                     "    session = NULL, " +
                     "    date = NULL, " +
                     "    week_number = NULL, " +
                     "    updated_at = NOW() " +
                     "WHERE id IN (" + idList + ") " +
                     "AND deleted_at IS NULL";
        
        Query query = entityManager.createNativeQuery(sql);
        int updatedCount = query.executeUpdate();
        
        return updatedCount;
    }

    public ByteArrayInputStream exportAssignmentsToExcel(List<String> assignmentIds) {
        if (assignmentIds == null || assignmentIds.isEmpty()) {
            return createEmptyExcel();
        }
        
        String sql = 
            "SELECT " +
            "c.class_id, c.class_id as class_lt, c.course_id, c.course_name, " +
            "c.description, c.group_id, '' as nhom, '' as sessionid, " +
            "c.number_students, c.period, '' as managerid, c.management_code, " +
            "'' as teachunitid, c.school, c.exam_class_id, " +
            "a.date, a.week_number, s.name as session_name, " +
            "a.room_id as room_name " +
            "FROM exam_timetable_assignment a " +
            "JOIN exam_timetabling_class c ON a.exam_timtabling_class_id = c.id " +
            "LEFT JOIN exam_timetable_session s ON CAST(a.exam_session_id AS VARCHAR) = CAST(s.id AS VARCHAR) " +
            "WHERE CAST(a.id AS VARCHAR) IN :assignmentIds " +
            "AND a.deleted_at IS NULL";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("assignmentIds", assignmentIds);
        
        List<Object[]> results = query.getResultList();
        
        if (results.isEmpty()) {
            return createEmptyExcel();
        }
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exam Assignments");
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern((short) 1); 
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Mã lớp QT", "Mã lớp LT", "Mã học phần", "Tên học phần", 
                "Ghi chú", "studyGroupID", "Nhóm", "sessionid", 
                "SL", "Đợt mở", "ManagerID", "Mã_QL", 
                "TeachUnitID", "Tên trường/khoa", "Mã lớp thi",
                "Ngày", "Tuần", "Kíp", "Phòng"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Populate data rows
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            int rowNum = 1;
            for (Object[] result : results) {
                Row row = sheet.createRow(rowNum++);
                
                for (int i = 0; i < 15; i++) {
                    Cell cell = row.createCell(i);
                    if (result[i] != null) {
                        if (i == 8) {
                            cell.setCellValue(getInteger(result[i]));
                        } else {
                            cell.setCellValue(getString(result[i]));
                        }
                    } else {
                        cell.setCellValue("");
                    }
                }
                
                // Date
                Object dateObj = result[15];
                String dateStr = "";
                if (dateObj != null) {
                    if (dateObj instanceof java.sql.Date) {
                        dateStr = ((java.sql.Date) dateObj).toLocalDate().format(dateFormatter);
                    } else if (dateObj instanceof java.sql.Timestamp) {
                        dateStr = ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate().format(dateFormatter);
                    } else if (dateObj instanceof LocalDate) {
                        dateStr = ((LocalDate) dateObj).format(dateFormatter);
                    }
                }
                row.createCell(15).setCellValue(dateStr);
                
                // Week
                Integer weekNumber = getInteger(result[16]);
                row.createCell(16).setCellValue(weekNumber != null ? "W" + weekNumber : "");
                
                // Session
                row.createCell(17).setCellValue(getString(result[17]) != null ? getString(result[17]) : "");
                
                // Room
                row.createCell(18).setCellValue(getString(result[18]) != null ? getString(result[18]) : "");
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }
    
    private ByteArrayInputStream createEmptyExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("No Data");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("No data to export based on selected assignments");
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate empty Excel file", e);
        }
    }
    
    private String getString(Object obj) {
        return obj != null ? obj.toString() : null;
    }
    
    private Integer getInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

        @Transactional
    public List<ExamTimetableAssignment> autoAssignSchedule(List<UUID> assignmentIds) {
        List<ExamTimetableAssignment> assignments = assignmentRepository.findAllById(assignmentIds);
        if (assignments.isEmpty()) {
            throw new RuntimeException("No assignments found");
        }
        Set<UUID> timetableIds = assignments.stream()
            .map(ExamTimetableAssignment::getExamTimetableId)
            .collect(Collectors.toSet());
            
        if (timetableIds.size() != 1) {
            throw new RuntimeException("All assignments must belong to the same timetable");
        }
        
        // Get the timetable details
        UUID timetableId = timetableIds.iterator().next();
        ExamTimetable timetable = examTimetableRepository.findById(timetableId)
            .orElseThrow(() -> new RuntimeException("Timetable not found"));
        
        // Get available rooms
        List<ExamRoom> availableRooms = roomRepository.findAllAsExamRoomDTO();
        if (availableRooms.isEmpty()) {
            throw new RuntimeException("No rooms available for assignment");
        }
        
        // Get available sessions from the timetable's session collection
        List<ExamTimetableSession> availableSessions = sessionRepository
            .findByExamTimetableSessionCollectionId(timetable.getExamTimetableSessionCollectionId());
            
        if (availableSessions.isEmpty()) {
            throw new RuntimeException("No sessions available for this timetable's session collection");
        }
        
        // Get timetable date and week constraints
        LocalDate startDate = timetable.getExamPlan().getStartTime().toLocalDate();
        LocalDate endDate = timetable.getExamPlan().getEndTime().toLocalDate();
        int startWeek = timetable.getExamPlan().getStartWeek();
        
        // Generate available slots (session + date combinations)
        List<ScheduleSlotDTO> availableSlots = generateAvailableSlots(
            availableSessions, startDate, endDate, startWeek);
        
        // Get busy combinations (room + slot that are already used)
        List<BusyCombinationDTO> busyCombinations = getBusyCombinations();
        
        List<AssignmentResultDTO> assignmentResults = runAssignmentAlgorithm(
            availableRooms, 
            availableSlots, 
            assignments,
            busyCombinations
        );
        
        for (AssignmentResultDTO result : assignmentResults) {
            ExamTimetableAssignment assignment = assignments.stream()
                .filter(a -> a.getId().equals(result.getAssignmentId()))
                .findFirst()
                .orElseThrow();
            
            assignment.setRoomId(result.getRoomId());
            assignment.setExamSessionId(result.getSessionId());
            assignment.setDate(result.getDate());
            assignment.setWeekNumber(result.getWeekNumber());
        }
        
        return assignmentRepository.saveAll(assignments);
    }

    /**
     * Generate available slots (session + date combinations)
     */
    private List<ScheduleSlotDTO> generateAvailableSlots(
            List<ExamTimetableSession> sessions,
            LocalDate startDate,
            LocalDate endDate,
            int startWeek) {
        List<ScheduleSlotDTO> slots = new ArrayList<>();
        
        LocalDate weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Skip weekends
            if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY && 
                currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                
                int weeksDifference = (int)ChronoUnit.WEEKS.between(weekStart, currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
                int weekNumber = startWeek + weeksDifference;
                
                for (ExamTimetableSession session : sessions) {
                    slots.add(new ScheduleSlotDTO(session.getId(), currentDate, weekNumber));
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return slots;
    }
    
    /**
     * Get busy combinations (room + slot that are already used)
     */
    private List<BusyCombinationDTO> getBusyCombinations() {
        String sql = "SELECT room_id, exam_session_id, date " +
                     "FROM exam_timetable_assignment " +
                     "WHERE room_id IS NOT NULL " +
                     "AND exam_session_id IS NOT NULL " +
                     "AND date IS NOT NULL " +
                     "AND deleted_at IS NULL";
        
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        
        return results.stream()
            .map(row -> {
                UUID roomId = toUUID(row[0]);
                UUID sessionId = toUUID(row[1]);
                LocalDate date = toLocalDate(row[2]);
                return new BusyCombinationDTO(roomId, sessionId, date);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * The algorithm function that assigns rooms and slots to assignments
     */
    private List<AssignmentResultDTO> runAssignmentAlgorithm(
            List<ExamRoom> availableRooms,
            List<ScheduleSlotDTO> availableSlots,
            List<ExamTimetableAssignment> assignments,
            List<BusyCombinationDTO> busyCombinations) {
        
        List<AssignmentResultDTO> results = new ArrayList<>();
        Random random = new Random();
        
        Set<String> busySet = busyCombinations.stream()
            .map(combo -> combo.getRoomId() + ":" + combo.getSessionId() + ":" + combo.getDate())
            .collect(Collectors.toSet());
        
        for (ExamTimetableAssignment assignment : assignments) {
            boolean assigned = false;
            int attempts = 0;
            
            while (!assigned && attempts < 50) { // Limit attempts to avoid infinite loop
                ExamRoom room = availableRooms.get(random.nextInt(availableRooms.size()));
                ScheduleSlotDTO slot = availableSlots.get(random.nextInt(availableSlots.size()));
                
                String combination = room.getId() + ":" + slot.getSessionId() + ":" + slot.getDate();
                
                results.add(new AssignmentResultDTO(
                    assignment.getId(),
                    room.getId(),
                    slot.getSessionId(),
                    slot.getDate(),
                    slot.getWeekNumber()
                ));
                
                assigned = true;
                attempts++;
            }
            
            if (!assigned) {
                ExamRoom room = availableRooms.get(random.nextInt(availableRooms.size()));
                ScheduleSlotDTO slot = availableSlots.get(random.nextInt(availableSlots.size()));
                
                results.add(new AssignmentResultDTO(
                    assignment.getId(),
                    room.getId(),
                    slot.getSessionId(),
                    slot.getDate(),
                    slot.getWeekNumber()
                ));
            }
        }
        
        return results;
    }
    
    // Helper methods for type conversion
    private UUID toUUID(Object obj) {
        if (obj == null) return null;
        return UUID.fromString(obj.toString());
    }
    
    private LocalDate toLocalDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.sql.Date) return ((java.sql.Date) obj).toLocalDate();
        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime().toLocalDate();
        if (obj instanceof LocalDate) return (LocalDate) obj;
        return LocalDate.parse(obj.toString());
    }

   /**
     * Check for conflicts in assignments of a timetable
     * @param timetableId The timetable ID to check
     * @return List of room-time combinations with conflicts
     */
    public List<ConflictResponseDTO> checkTimetableConflicts(UUID timetableId) {
        List<ConflictResponseDTO> conflicts = new ArrayList<>();
        
        // Step 1: Find room-time slots with multiple assignments (conflict type 1)
        conflicts.addAll(findRoomTimeConflicts(timetableId));
        
        // Step 2: Find conflicts between classes in the conflict_exam_timetabling_class table (conflict type 2)
        conflicts.addAll(findConflictPairTimeOverlaps(timetableId));
        
        return conflicts;
    }

    /**
     * Find room-time slots with multiple assignments (conflict type 1)
     */
    private List<ConflictResponseDTO> findRoomTimeConflicts(UUID timetableId) {
        // This query efficiently identifies all room-time slots with multiple assignments
        String sql = 
            "SELECT " +
            "  a.room_id, " +
            "  a.date, " +
            "  a.session, " +
            "  COUNT(*) as assignment_count " +
            "FROM exam_timetable_assignment a " +
            "WHERE a.exam_timetable_id = :timetableId " +
            "  AND a.deleted_at IS NULL " +
            "  AND a.room_id IS NOT NULL " +
            "  AND a.date IS NOT NULL " +
            "  AND a.session IS NOT NULL " +
            "GROUP BY a.room_id, a.date, a.session " +
            "HAVING COUNT(*) > 1"; // Only slots with multiple assignments
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("timetableId", timetableId);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        List<ConflictResponseDTO> conflicts = new ArrayList<>();
        
        for (Object[] row : results) {
            String roomId = row[0] != null ? row[0].toString() : "";
            String date = row[1] != null ? row[1].toString() : null;
            String session = row[2] != null ? row[2].toString() : "";
            
            ConflictResponseDTO conflict = ConflictResponseDTO.builder()
                .roomId(roomId)
                .date(date)
                .session(session)
                .conflictType("ROOM")
                .examClassId1(null) 
                .examClassId2(null)
                .build();
            
            conflicts.add(conflict);
        }
        
        return conflicts;
    }

    /**
     * Find time overlaps between classes that have conflict records (conflict type 2)
     */
    private List<ConflictResponseDTO> findConflictPairTimeOverlaps(UUID timetableId) {
        // Query to find time slots where conflict pairs are scheduled together, including exam class IDs
        String sql = 
            "SELECT DISTINCT " +
            "  'Multiple Rooms' as room_name, " +
            "  a1.date, " +
            "  a1.session, " +
            "  LEAST(ec1.exam_class_id, ec2.exam_class_id) as exam_class_id_1, " +
            "  GREATEST(ec1.exam_class_id, ec2.exam_class_id) as exam_class_id_2 " +
            "FROM exam_timetable_assignment a1 " +
            "JOIN exam_timetable_assignment a2 ON " +
            "  a1.date = a2.date AND " +
            "  a1.session = a2.session AND " +
            "  a1.id < a2.id " + // Ensure each pair check only once
            "JOIN exam_timetabling_class ec1 ON a1.exam_timtabling_class_id = ec1.id " +
            "JOIN exam_timetabling_class ec2 ON a2.exam_timtabling_class_id = ec2.id " +
            "JOIN conflict_exam_timetabling_class c ON " +
            "  (c.exam_timetabling_class_id_1 = a1.exam_timtabling_class_id AND " +
            "   c.exam_timetabling_class_id_2 = a2.exam_timtabling_class_id) OR " +
            "  (c.exam_timetabling_class_id_1 = a2.exam_timtabling_class_id AND " +
            "   c.exam_timetabling_class_id_2 = a1.exam_timtabling_class_id) " +
            "WHERE a1.exam_timetable_id = :timetableId " +
            "  AND a2.exam_timetable_id = :timetableId " +
            "  AND a1.deleted_at IS NULL " +
            "  AND a2.deleted_at IS NULL " +
            "  AND a1.date IS NOT NULL " +
            "  AND a1.session IS NOT NULL";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("timetableId", timetableId);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        List<ConflictResponseDTO> conflicts = new ArrayList<>();
        
        for (Object[] row : results) {
            String roomName = row[0] != null ? row[0].toString() : "Multiple Rooms";
            String date = row[1] != null ? row[1].toString(): null;
            String session = row[2] != null ? row[2].toString() : "";
            String examClassId1 = row[3] != null ? row[3].toString() : "Unknown";
            String examClassId2 = row[4] != null ? row[4].toString() : "Unknown";
            
            ConflictResponseDTO conflict = ConflictResponseDTO.builder()
                .roomId(roomName) 
                .date(date)
                .session(session)
                .conflictType("CLASS")
                .examClassId1(examClassId1)
                .examClassId2(examClassId2)
                .build();
            
            conflicts.add(conflict);
        }
        
        return conflicts;
    }
}

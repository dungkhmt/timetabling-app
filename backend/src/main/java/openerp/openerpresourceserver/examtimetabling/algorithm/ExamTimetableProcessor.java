package openerp.openerpresourceserver.examtimetabling.algorithm;

import openerp.openerpresourceserver.examtimetabling.algorithm.model.*;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamRoom;
import openerp.openerpresourceserver.examtimetabling.entity.*;
import openerp.openerpresourceserver.examtimetabling.repository.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Processor for preparing timetabling data
 */
public class ExamTimetableProcessor {

    private final ExamClassRepository examClassRepository;
    private final ClassroomRepository examRoomRepository;
    private final ExamTimetableSessionRepository sessionRepository;
    private final ExamTimetableAssignmentRepository assignmentRepository;
    private final ConflictExamTimetablingClassRepository conflictRepository;
    private final ExamTimetableRepository examTimetableRepository;
    
    public ExamTimetableProcessor(
        ExamClassRepository examClassRepository,
        ClassroomRepository examRoomRepository,
        ExamTimetableSessionRepository sessionRepository,
        ExamTimetableAssignmentRepository assignmentRepository,
        ConflictExamTimetablingClassRepository conflictRepository,
        ExamTimetableRepository examTimetableRepository 
    ) {
        this.examClassRepository = examClassRepository;
        this.examRoomRepository = examRoomRepository;
        this.sessionRepository = sessionRepository;
        this.assignmentRepository = assignmentRepository;
        this.conflictRepository = conflictRepository;
        this.examTimetableRepository = examTimetableRepository;
    }
    
    /**
     * Process all data needed for the timetabling algorithm
     * 
     * @param examTimetableId ID of the exam timetable
     * @param classIds List of class IDs to be assigned
     * @param examDates List of dates in dd-MM-yyyy format
     * @return TimetablingData containing all processed data
     */
    public TimetablingData processData(UUID examTimetableId, List<UUID> classIds, List<String> examDates) {
        TimetablingData data = new TimetablingData();
        
        List<LocalDate> dates = processExamDates(examDates);
        data.setExamDates(dates);
        
        List<ExamClass> examClasses = examClassRepository.findAllByIdIn(classIds);
        data.setExamClasses(examClasses);
        
        // Original course grouping
        Map<String, List<ExamClass>> originalCourseGroups = examClasses.stream()
            .collect(Collectors.groupingBy(ExamClass::getCourseId));
        
        // Split large courses into smaller sub-courses
        Map<String, List<ExamClass>> classesByCourseId = splitLargeCourses(originalCourseGroups);
        data.setClassesByCourseId(classesByCourseId);
        
        // Rest of the method remains the same...
        Map<String, List<ExamClass>> classesByGroupId = examClasses.stream()
            .filter(ec -> ec.getGroupId() != null && !ec.getGroupId().isEmpty())
            .collect(Collectors.groupingBy(ExamClass::getGroupId));
        data.setClassesByGroupId(classesByGroupId);
        
        List<ExamRoom> rooms = examRoomRepository.findAllAsExamRoomDTO();
        data.setAvailableRooms(rooms);
        
        List<TimeSlot> timeSlots = generateTimeSlots(examTimetableId, dates);
        data.setAvailableTimeSlots(timeSlots);
        
        List<ExamTimetableAssignment> assignedAssignments = 
            assignmentRepository.findAssignedClassByExamTimetableId(examTimetableId);
        data.setExistingAssignments(assignedAssignments);

        Set<TimeSlotRoomPair> prohibitedSlots = getProhibitedSlots(assignedAssignments);
        data.setProhibitedSlots(prohibitedSlots);
        
        // Build conflict graph
        Map<UUID, Set<UUID>> conflictGraph = buildConflictGraph(classIds);
        data.setConflictGraph(conflictGraph);
        
        // Identify early time slots (7am-9am)
        Set<UUID> earlyTimeSlots = identifyEarlyTimeSlots(timeSlots);
        data.setEarlyTimeSlots(earlyTimeSlots);
        
        return data;
    }
    
    /**
     * Convert string dates to LocalDate objects
     */
    private List<LocalDate> processExamDates(List<String> examDates) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return examDates.stream()
            .map(dateStr -> LocalDate.parse(dateStr, formatter))
            .collect(Collectors.toList());
    }

    /**
     * Split large courses into smaller sub-courses
     * Courses with >10 classes will be split based on group and size constraints
     */
    private Map<String, List<ExamClass>> splitLargeCourses(Map<String, List<ExamClass>> originalCourseGroups) {
        Map<String, List<ExamClass>> newCourseGroups = new HashMap<>();
        
        for (Map.Entry<String, List<ExamClass>> entry : originalCourseGroups.entrySet()) {
            String originalCourseId = entry.getKey();
            List<ExamClass> classes = entry.getValue();
            
            // If course has <= 10 classes, keep it as is
            if (classes.size() <= 10) {
                newCourseGroups.put(originalCourseId, classes);
                continue;
            }
            
            // Step 1: Group classes by description group (groupId)
            Map<String, List<ExamClass>> classesByGroup = classes.stream()
                .collect(Collectors.groupingBy(ec -> 
                    ec.getExamClassGroupId() != null ? ec.getExamClassGroupId().toString() : "NO_GROUP"));
            
            // Step 2: Separate large groups (>5) and small groups (<=5)
            List<List<ExamClass>> largeGroups = new ArrayList<>();
            List<List<ExamClass>> smallGroups = new ArrayList<>();
            
            for (List<ExamClass> group : classesByGroup.values()) {
                if (group.size() > 5) {
                    largeGroups.add(group);
                } else {
                    smallGroups.add(group);
                }
            }
            
            // Step 3: Create new courses from large groups
            int subCourseCounter = 1;
            for (List<ExamClass> largeGroup : largeGroups) {
                String newCourseId = originalCourseId + "_SUB_" + subCourseCounter++;
                newCourseGroups.put(newCourseId, largeGroup);
            }
            
            // Step 4: Combine small groups to create new courses
            List<ExamClass> currentCombinedGroup = new ArrayList<>();
            
            for (List<ExamClass> smallGroup : smallGroups) {
                currentCombinedGroup.addAll(smallGroup);
                
                // If combined group reaches 5 or more classes, create a new course
                if (currentCombinedGroup.size() >= 5) {
                    String newCourseId = originalCourseId + "_SUB_" + subCourseCounter++;
                    newCourseGroups.put(newCourseId, new ArrayList<>(currentCombinedGroup));
                    currentCombinedGroup.clear();
                }
            }
            
            // Step 5: Handle remaining small groups that couldn't be combined
            if (!currentCombinedGroup.isEmpty()) {
                String newCourseId = originalCourseId + "_SUB_" + subCourseCounter++;
                newCourseGroups.put(newCourseId, currentCombinedGroup);
            }
        }
        
        return newCourseGroups;
    }
    
    /**
     * Generate all available time slots from session templates and dates
     * Only includes sessions that belong to the timetable's session collection
     */
    private List<TimeSlot> generateTimeSlots(UUID examTimetableId, List<LocalDate> dates) {
        ExamTimetable examTimetable = examTimetableRepository.findById(examTimetableId)
            .orElseThrow(() -> new RuntimeException("Exam timetable not found: " + examTimetableId));
        
        List<ExamTimetableSession> sessionTemplates = 
            getSessionsForTimetable(examTimetable.getExamTimetableSessionCollectionId());
        
        List<TimeSlot> timeSlots = new ArrayList<>();
        
        for (LocalDate date : dates) {
            for (ExamTimetableSession sessionTemplate : sessionTemplates) {
                TimeSlot slot = new TimeSlot();
                slot.setId(UUID.randomUUID()); 
                slot.setSessionId(sessionTemplate.getId());
                slot.setDate(date);
                slot.setStartTime(sessionTemplate.getStartTime().toLocalTime());
                slot.setEndTime(sessionTemplate.getEndTime().toLocalTime());
                slot.setName(sessionTemplate.getName() + " - " + date);
                timeSlots.add(slot);
            }
        }
        
        return timeSlots;
    }

    /**
     * Get all sessions that belong to a specific session collection
     */
    private List<ExamTimetableSession> getSessionsForTimetable(UUID sessionCollectionId) {
        List<ExamTimetableSession> sessions = sessionRepository.findByExamTimetableSessionCollectionId(sessionCollectionId);
        
        return sessions;
    }
    
    /**
     * Find time slots and rooms that are already in use
     */
    private Set<TimeSlotRoomPair> getProhibitedSlots(List<ExamTimetableAssignment> existingAssignments) {
        Set<TimeSlotRoomPair> prohibitedSlots = new HashSet<>();
        
        for (ExamTimetableAssignment assignment : existingAssignments) {
            TimeSlotRoomPair pair = new TimeSlotRoomPair();
            pair.setRoomId(assignment.getRoomId());
            pair.setSessionId(assignment.getExamSessionId());
            pair.setDate(assignment.getDate());
            prohibitedSlots.add(pair);
        }
        
        return prohibitedSlots;
    }
    
    /**
     * Build a graph representing conflicts between exam classes
     */
    private Map<UUID, Set<UUID>> buildConflictGraph(List<UUID> classIds) {
        Map<UUID, Set<UUID>> conflictGraph = new HashMap<>();
        
        for (UUID classId : classIds) {
            conflictGraph.put(classId, new HashSet<>());
        }
        
        List<ConflictExamTimetablingClass> conflicts = conflictRepository.findByExamTimetablingClassId1InOrExamTimetablingClassId2In(
            classIds, classIds);
        
        for (ConflictExamTimetablingClass conflict : conflicts) {
            UUID class1 = conflict.getExamTimetablingClassId1();
            UUID class2 = conflict.getExamTimetablingClassId2();
            
            // Only add if both classes are in our set to be scheduled
            if (classIds.contains(class1) && classIds.contains(class2)) {
                conflictGraph.computeIfAbsent(class1, k -> new HashSet<>()).add(class2);
                conflictGraph.computeIfAbsent(class2, k -> new HashSet<>()).add(class1);
            }
        }
        
        return conflictGraph;
    }
    
    /**
     * Identify time slots that are in the early morning (7am-9am)
     */
    private Set<UUID> identifyEarlyTimeSlots(List<TimeSlot> timeSlots) {
        return timeSlots.stream()
            .filter(slot -> {
                int hour = slot.getStartTime().getHour();
                return hour >= 7 && hour < 9;
            })
            .map(TimeSlot::getId)
            .collect(Collectors.toSet());
    }
}

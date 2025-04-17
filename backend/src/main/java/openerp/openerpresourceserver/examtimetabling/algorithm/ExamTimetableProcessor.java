package openerp.openerpresourceserver.examtimetabling.algorithm;

import openerp.openerpresourceserver.examtimetabling.algorithm.model.*;
import openerp.openerpresourceserver.examtimetabling.entity.*;
import openerp.openerpresourceserver.examtimetabling.repository.*;
import openerp.openerpresourceserver.examtimetabling.entity.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Processor for preparing timetabling data
 */
public class ExamTimetableProcessor {

    private final ExamClassRepository examClassRepository;
    private final ExamRoomRepository examRoomRepository;
    private final ExamTimetableSessionRepository sessionRepository;
    private final ExamTimetableAssignmentRepository assignmentRepository;
    private final ConflictExamTimetablingClassRepository conflictRepository;
    private final ExamTimetableRepository examTimetableRepository;
    
    public ExamTimetableProcessor(
        ExamClassRepository examClassRepository,
        ExamRoomRepository examRoomRepository,
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
        
        // Process dates
        List<LocalDate> dates = processExamDates(examDates);
        data.setExamDates(dates);
        
        // Get exam classes
        List<ExamClass> examClasses = examClassRepository.findAllByIdIn(classIds);
        data.setExamClasses(examClasses);
        
        // Group classes by course ID
        Map<String, List<ExamClass>> classesByCourseId = examClasses.stream()
            .collect(Collectors.groupingBy(ExamClass::getCourseId));
        data.setClassesByCourseId(classesByCourseId);
        
        // Group classes by group ID
        Map<String, List<ExamClass>> classesByGroupId = examClasses.stream()
            .filter(ec -> ec.getGroupId() != null && !ec.getGroupId().isEmpty())
            .collect(Collectors.groupingBy(ExamClass::getGroupId));
        data.setClassesByGroupId(classesByGroupId);
        
        // Get available rooms
        List<ExamRoom> rooms = examRoomRepository.findAll();
        data.setAvailableRooms(rooms);
        
        // Generate available time slots
        List<TimeSlot> timeSlots = generateTimeSlots(examTimetableId, dates);
        data.setAvailableTimeSlots(timeSlots);
        
        // Get assigned assignments for this timetable
        List<ExamTimetableAssignment> assignedAssignments = 
            assignmentRepository.findAssignedClassByExamTimetableId(examTimetableId);
        data.setExistingAssignments(assignedAssignments);

        
        
        // Get prohibited time slots
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
     * Generate all available time slots from session templates and dates
     * Only includes sessions that belong to the timetable's session collection
     */
    private List<TimeSlot> generateTimeSlots(UUID examTimetableId, List<LocalDate> dates) {
        // Get the timetable to find its session collection
        ExamTimetable examTimetable = examTimetableRepository.findById(examTimetableId)
            .orElseThrow(() -> new RuntimeException("Exam timetable not found: " + examTimetableId));
        
        // Get sessions belonging to this timetable's session collection
        List<ExamTimetableSession> sessionTemplates = 
            getSessionsForTimetable(examTimetable.getExamTimetableSessionCollectionId());
        
        List<TimeSlot> timeSlots = new ArrayList<>();
        
        for (LocalDate date : dates) {
            for (ExamTimetableSession sessionTemplate : sessionTemplates) {
                TimeSlot slot = new TimeSlot();
                slot.setId(UUID.randomUUID()); // Generate a unique ID for the time slot
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
        // Find all sessions that belong to this collection
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
        
        // Initialize empty sets for all classes
        for (UUID classId : classIds) {
            conflictGraph.put(classId, new HashSet<>());
        }
        
        // Add conflicts from the repository
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
        
        // Add implicit conflicts - classes with same course ID are not conflicts
        // They need to be scheduled at the same time
        // But we'll handle this as a special case in the algorithm
        
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

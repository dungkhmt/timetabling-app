package openerp.openerpresourceserver.examtimetabling.algorithm;

import openerp.openerpresourceserver.examtimetabling.entity.*;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility methods for exam timetabling
 */
@Slf4j
public class ExamTimetablingUtils {

    /**
     * Check if a time slot is valid for a course based on conflicts
     * 
     * @param courseId Course ID to check
     * @param timeSlotId Time slot ID to check
     * @param courseTimeSlots Map of course IDs to assigned time slots
     * @param courseConflicts Map of course IDs to sets of conflicting course IDs
     * @param availableTimeSlots List of all available time slots
     * @return true if the time slot is valid for the course
     */
    public static boolean isValidTimeSlot(
            String courseId, 
            UUID timeSlotId, 
            Map<String, UUID> courseTimeSlots,
            Map<String, Set<String>> courseConflicts,
            List<TimeSlot> availableTimeSlots) {
        
        // Get conflicts for this course
        Set<String> conflicts = courseConflicts.getOrDefault(courseId, Collections.emptySet());
        
        // Check if any conflicting course is already assigned to this time slot
        for (String conflictCourseId : conflicts) {
            UUID conflictTimeSlotId = courseTimeSlots.get(conflictCourseId);
            
            if (conflictTimeSlotId != null) {
                if (conflictTimeSlotId.equals(timeSlotId)) {
                    return false;
                }
                
                // Check if they're consecutive slots on the same day
                TimeSlot timeSlot = findTimeSlot(timeSlotId, availableTimeSlots);
                TimeSlot conflictTimeSlot = findTimeSlot(conflictTimeSlotId, availableTimeSlots);
                
                if (timeSlot != null && conflictTimeSlot != null) {
                    if (timeSlot.isSameDayAs(conflictTimeSlot) && 
                        timeSlot.isConsecutiveWith(conflictTimeSlot)) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Find a TimeSlot by its ID
     */
    public static TimeSlot findTimeSlot(UUID timeSlotId, List<TimeSlot> availableTimeSlots) {
        for (TimeSlot timeSlot : availableTimeSlots) {
            if (timeSlot.getId().equals(timeSlotId)) {
                return timeSlot;
            }
        }
        return null;
    }
    
    /**
     * Check if a room can be assigned to a class
     */
    public static boolean isRoomSuitable(ExamClass examClass, ExamRoom room) {
        // Room must have at least 2n seats for n students
        int requiredCapacity = examClass.getNumberOfStudents() * 2;
        return room.getNumberSeat() >= requiredCapacity;
    }
    
    /**
     * Check if a time slot is prohibited
     */
    public static boolean isTimeSlotProhibited(
            TimeSlot timeSlot, 
            Set<TimeSlotRoomPair> prohibitedSlots) {
        
        for (TimeSlotRoomPair prohibited : prohibitedSlots) {
            if (prohibited.getSessionId().equals(timeSlot.getSessionId()) &&
                prohibited.getDate().equals(timeSlot.getDate())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if a room is available at a particular time slot
     */
    public static boolean isRoomAvailable(
            UUID roomId, 
            TimeSlot timeSlot, 
            Set<TimeSlotRoomPair> prohibitedSlots,
            Map<TimeSlotRoomPair, Boolean> assignedRooms) {
        
        TimeSlotRoomPair pair = new TimeSlotRoomPair();
        pair.setRoomId(roomId);
        pair.setSessionId(timeSlot.getSessionId());
        pair.setDate(timeSlot.getDate());
        
        if (prohibitedSlots.contains(pair)) {
            return false;
        }
        
        // Check if this pair is already assigned
        if (assignedRooms.getOrDefault(pair, false)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate days between two dates
     */
    public static long daysBetween(LocalDate date1, LocalDate date2) {
        return Math.abs(ChronoUnit.DAYS.between(date1, date2));
    }
    
    /**
     * Check if a time slot is early (7am-9am)
     */
    public static boolean isEarlyTimeSlot(TimeSlot timeSlot) {
        int hour = timeSlot.getStartTime().getHour();
        return hour >= 7 && hour < 9;
    }
    
    /**
     * Parse a date string in dd-MM-yyyy format
     */
    public static LocalDate parseDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(dateStr, formatter);
    }
    
    /**
     * Format a date to dd-MM-yyyy
     */
    public static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(formatter);
    }
    
    /**
     * Check if a solution satisfies all hard constraints
     */
    public static boolean validateSolution(TimetablingSolution solution, TimetablingData data) {
        log.info("Validating solution...");
        
        // 1. Check that all classes are assigned exactly one room and one time slot
        for (ExamClass examClass : data.getExamClasses()) {
            AssignmentDetails assignment = solution.getAssignedClasses().get(examClass.getId());
            if (assignment == null) {
                log.error("Validation failed: Class {} is not assigned", examClass.getId());
                return false;
            }
        }
        
        // 2. Check that no room has multiple classes at the same time slot
        Map<String, List<UUID>> roomSessionAssignments = new HashMap<>();
        
        for (Map.Entry<UUID, AssignmentDetails> entry : solution.getAssignedClasses().entrySet()) {
            UUID classId = entry.getKey();
            AssignmentDetails assignment = entry.getValue();
            
            String key = assignment.getRoomId() + "_" + assignment.getSessionId() + "_" + assignment.getDate();
            
            List<UUID> assignedClasses = roomSessionAssignments.computeIfAbsent(key, k -> new ArrayList<>());
            assignedClasses.add(classId);
            
            if (assignedClasses.size() > 1) {
                log.error("Validation failed: Room {} has multiple classes at session {} on {}", 
                    assignment.getRoomId(), assignment.getSessionId(), assignment.getDate());
                return false;
            }
        }
        
        // 3. Check capacity constraints
        for (ExamClass examClass : data.getExamClasses()) {
            AssignmentDetails assignment = solution.getAssignedClasses().get(examClass.getId());
            if (assignment == null) continue;
            
            ExamRoom room = null;
            for (ExamRoom r : data.getAvailableRooms()) {
                if (r.getId().equals(assignment.getRoomId())) {
                    room = r;
                    break;
                }
            }
            
            if (room == null) {
                log.error("Validation failed: Room {} not found", assignment.getRoomId());
                return false;
            }
            
            int requiredCapacity = examClass.getNumberOfStudents() * 2;
            if (room.getNumberSeat() < requiredCapacity) {
                log.error("Validation failed: Room {} capacity ({}) is less than required ({})", 
                    room.getId(), room.getNumberSeat(), requiredCapacity);
                return false;
            }
        }
        
        // 4. Check conflict constraints
        for (Map.Entry<UUID, Set<UUID>> conflictEntry : data.getConflictGraph().entrySet()) {
            UUID classId1 = conflictEntry.getKey();
            Set<UUID> conflictingClasses = conflictEntry.getValue();
            
            AssignmentDetails assignment1 = solution.getAssignedClasses().get(classId1);
            if (assignment1 == null) continue;
            
            for (UUID classId2 : conflictingClasses) {
                AssignmentDetails assignment2 = solution.getAssignedClasses().get(classId2);
                if (assignment2 == null) continue;
                
                // Check if they're in the same time slot
                if (assignment1.getSessionId().equals(assignment2.getSessionId()) && 
                    assignment1.getDate().equals(assignment2.getDate())) {
                    log.error("Validation failed: Conflicting classes {} and {} are in the same time slot", 
                        classId1, classId2);
                    return false;
                }
                
                // Check if they're in consecutive time slots on the same day
                if (assignment1.getDate().equals(assignment2.getDate())) {
                    // Find time slots to check if they're consecutive
                    TimeSlot timeSlot1 = null;
                    TimeSlot timeSlot2 = null;
                    
                    for (TimeSlot ts : data.getAvailableTimeSlots()) {
                        if (ts.getSessionId().equals(assignment1.getSessionId()) && 
                            ts.getDate().equals(assignment1.getDate())) {
                            timeSlot1 = ts;
                        }
                        if (ts.getSessionId().equals(assignment2.getSessionId()) && 
                            ts.getDate().equals(assignment2.getDate())) {
                            timeSlot2 = ts;
                        }
                    }
                    
                    if (timeSlot1 != null && timeSlot2 != null && 
                        timeSlot1.isConsecutiveWith(timeSlot2)) {
                        log.error("Validation failed: Conflicting classes {} and {} are in consecutive time slots", 
                            classId1, classId2);
                        return false;
                    }
                }
            }
        }
        
        // 5. Check that classes with same course ID are in same time slot
        Map<String, UUID> courseSessionMap = new HashMap<>();
        
        for (ExamClass examClass : data.getExamClasses()) {
            AssignmentDetails assignment = solution.getAssignedClasses().get(examClass.getId());
            if (assignment == null) continue;
            
            String courseId = examClass.getCourseId();
            UUID sessionId = assignment.getSessionId();
            LocalDate date = assignment.getDate();
            
            String courseTimeSlotKey = courseId + "_" + sessionId + "_" + date;
            
            if (courseSessionMap.containsKey(courseId)) {
                UUID existingSessionId = solution.getCourseTimeSlotAssignments().get(courseId);
                
                if (!existingSessionId.equals(assignment.getTimeSlotId())) {
                    log.error("Validation failed: Classes with course ID {} are in different time slots", courseId);
                    return false;
                }
            } else {
                courseSessionMap.put(courseId, sessionId);
            }
        }
        
        // 6. Check that no class is in a prohibited time slot
        for (Map.Entry<UUID, AssignmentDetails> entry : solution.getAssignedClasses().entrySet()) {
            AssignmentDetails assignment = entry.getValue();
            
            TimeSlotRoomPair pair = new TimeSlotRoomPair();
            pair.setSessionId(assignment.getSessionId());
            pair.setDate(assignment.getDate());
            pair.setRoomId(assignment.getRoomId());
            
            if (data.getProhibitedSlots().contains(pair)) {
                log.error("Validation failed: Class {} is in a prohibited time slot", entry.getKey());
                return false;
            }
        }
        
        log.info("Solution validation successful");
        return true;
    }
}

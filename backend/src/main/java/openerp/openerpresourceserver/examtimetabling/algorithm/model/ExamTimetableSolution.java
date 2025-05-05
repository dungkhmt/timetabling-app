package openerp.openerpresourceserver.examtimetabling.algorithm.model;

import openerp.openerpresourceserver.examtimetabling.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that implements the calculation of solution metrics and quality score
 * Extends the basic TimetablingSolution with methods specific to the exam timetabling problem
 */
@Slf4j
public class ExamTimetableSolution extends TimetablingSolution {
    
    // Constants for optimization weights
    private static final double WEIGHT_GROUP_SPACING = 10.0;  // 1st priority
    private static final double WEIGHT_ROOM_BALANCE = 5.0;    // 2nd priority
    private static final double WEIGHT_TIMESLOT_BALANCE = 5.0; // 2nd priority
    private static final double WEIGHT_EARLY_SLOTS = 2.0;     // 3rd priority
    
    /**
     * Calculate metrics for the current solution based on the preprocessed data
     */
    @Override
    public void calculateMetrics(TimetablingData data) {
        // Calculate group spacing violations (1st priority)
        calculateGroupSpacingViolations(data);
        
        // Calculate room usage balance (2nd priority)
        calculateRoomBalanceMetric();
        
        // Calculate time slot usage balance (2nd priority)
        calculateTimeSlotBalanceMetric();
        
        // Calculate early slot assignments (3rd priority)
        calculateEarlySlotAssignments(data);
        
        // Calculate overall quality score (weighted sum)
        calculateQualityScore();
    }
    
    /**
     * Calculate violations of the group spacing constraint
     * (classes in same group with different course IDs should have at least a day between them)
     */
    private void calculateGroupSpacingViolations(TimetablingData data) {
        int violations = 0;
        
        // Get classes grouped by group ID
        Map<String, List<ExamClass>> classesByGroupId = data.getClassesByGroupId();
        
        // For each group
        for (Map.Entry<String, List<ExamClass>> entry : classesByGroupId.entrySet()) {
            String groupId = entry.getKey();
            List<ExamClass> classesInGroup = entry.getValue();
            
            // Skip groups with only one course
            Map<String, List<ExamClass>> courseGroups = classesInGroup.stream()
                .collect(Collectors.groupingBy(ExamClass::getCourseId));
            
            if (courseGroups.size() <= 1) continue;
            
            // Get all unique course IDs in this group
            List<String> courseIds = new ArrayList<>(courseGroups.keySet());
            
            // For each pair of courses in the group
            for (int i = 0; i < courseIds.size(); i++) {
                for (int j = i + 1; j < courseIds.size(); j++) {
                    String courseId1 = courseIds.get(i);
                    String courseId2 = courseIds.get(j);
                    
                    // Get their assigned time slots
                    UUID timeSlotId1 = getCourseTimeSlotAssignments().get(courseId1);
                    UUID timeSlotId2 = getCourseTimeSlotAssignments().get(courseId2);
                    
                    if (timeSlotId1 == null || timeSlotId2 == null) continue;
                    
                    // Find the corresponding time slot objects
                    TimeSlot timeSlot1 = findTimeSlot(timeSlotId1, data.getAvailableTimeSlots());
                    TimeSlot timeSlot2 = findTimeSlot(timeSlotId2, data.getAvailableTimeSlots());
                    
                    if (timeSlot1 == null || timeSlot2 == null) continue;
                    
                    // Check if they are scheduled on the same day or consecutive days
                    if (timeSlot1.isSameDayAs(timeSlot2) || 
                        timeSlot1.isDayAfter(timeSlot2) || 
                        timeSlot1.isDayBefore(timeSlot2)) {
                        violations++;
                    }
                }
            }
        }
        
        setGroupSpacingViolations(violations);
    }
    
    /**
     * Calculate the balance of room usage
     * (Should be as equal as possible)
     */
    private void calculateRoomBalanceMetric() {
        Map<String, Integer> roomUsage = getRoomUsageCounts();
        
        if (roomUsage.isEmpty()) {
            setRoomBalanceMetric(1.0); // Perfect balance if no rooms used
            return;
        }
        
        // Calculate standard deviation of room usage
        double mean = roomUsage.values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
        
        double variance = roomUsage.values().stream()
            .mapToInt(Integer::intValue)
            .mapToDouble(count -> Math.pow(count - mean, 2))
            .average()
            .orElse(0.0);
        
        double stdDev = Math.sqrt(variance);
        
        // Normalize to a 0-1 metric where 1 is perfect balance
        // We use an exponential function to scale: e^(-stdDev)
        double metric = Math.exp(-stdDev);
        
        setRoomBalanceMetric(metric);
    }
    
    /**
     * Calculate the balance of time slot usage
     * (Should be as equal as possible)
     */
    private void calculateTimeSlotBalanceMetric() {
        Map<UUID, Integer> timeSlotUsage = getTimeSlotUsageCounts();
        
        if (timeSlotUsage.isEmpty()) {
            setTimeSlotBalanceMetric(1.0); // Perfect balance if no time slots used
            return;
        }
        
        // Calculate standard deviation of time slot usage
        double mean = timeSlotUsage.values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
        
        double variance = timeSlotUsage.values().stream()
            .mapToInt(Integer::intValue)
            .mapToDouble(count -> Math.pow(count - mean, 2))
            .average()
            .orElse(0.0);
        
        double stdDev = Math.sqrt(variance);
        
        // Normalize to a 0-1 metric where 1 is perfect balance
        double metric = Math.exp(-stdDev);
        
        setTimeSlotBalanceMetric(metric);
    }
    
    /**
     * Calculate the number of early slot assignments
     * (Classes scheduled in 7am-9am slots)
     */
    private void calculateEarlySlotAssignments(TimetablingData data) {
        Set<UUID> earlySlots = data.getEarlyTimeSlots();
        int earlyAssignments = 0;
        
        // Count classes assigned to early slots
        for (UUID classId : getAssignedClasses().keySet()) {
            AssignmentDetails details = getAssignedClasses().get(classId);
            if (earlySlots.contains(details.getTimeSlotId())) {
                earlyAssignments++;
            }
        }
        
        setEarlySlotAssignments(earlyAssignments);
    }
    
    /**
     * Calculate the overall quality score
     * (Weighted sum of metrics)
     */
    private void calculateQualityScore() {
        // Group spacing violations (negative impact, higher priority)
        double groupSpacingScore = 1.0 / (1.0 + getGroupSpacingViolations());
        
        // Room balance (positive impact)
        double roomBalanceScore = getRoomBalanceMetric();
        
        // Time slot balance (positive impact)
        double timeSlotBalanceScore = getTimeSlotBalanceMetric();
        
        // Early slot assignments (negative impact, lower priority)
        double earlySlotScore = 1.0 / (1.0 + getEarlySlotAssignments() * 0.1);

        System.err.println("Group Spacing Score: " + groupSpacingScore);
        System.err.println("Room Balance Score: " + roomBalanceScore);
        System.err.println("Time Slot Balance Score: " + timeSlotBalanceScore);
        System.err.println("Early Slot Score: " + earlySlotScore);
        
        // Weighted sum (higher is better)
        double score = (WEIGHT_GROUP_SPACING * groupSpacingScore) +
                      (WEIGHT_ROOM_BALANCE * roomBalanceScore) +
                      (WEIGHT_TIMESLOT_BALANCE * timeSlotBalanceScore) +
                      (WEIGHT_EARLY_SLOTS * earlySlotScore);
        
        setQualityScore(score);
    }
    
    /**
     * Check if the solution is complete (all classes assigned)
     */
    @Override
    public boolean isComplete() {
        // In a complete solution, all course IDs should have an assigned time slot
        for (String courseId : getCourseTimeSlotAssignments().keySet()) {
            if (getCourseTimeSlotAssignments().get(courseId) == null) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Helper method to find a TimeSlot by its ID
     */
    private TimeSlot findTimeSlot(UUID timeSlotId, List<TimeSlot> availableTimeSlots) {
        for (TimeSlot timeSlot : availableTimeSlots) {
            if (timeSlot.getId().equals(timeSlotId)) {
                return timeSlot;
            }
        }
        return null;
    }
    
    /**
     * Override the add assignment method to update usage counts
     */
    @Override
    public void addAssignment(UUID classId, UUID timeSlotId, String roomId, 
                              String courseId, LocalDate date) {
        super.addAssignment(classId, timeSlotId, roomId, courseId, date);
        
        // Update course time slot assignments
        getCourseTimeSlotAssignments().put(courseId, timeSlotId);
        
        // Update time slot class assignments
        getTimeSlotClassAssignments()
            .computeIfAbsent(timeSlotId, k -> new ArrayList<>())
            .add(classId);
        
        // Update usage counts
        getRoomUsageCounts().put(roomId, getRoomUsageCounts().getOrDefault(roomId, 0) + 1);
        getTimeSlotUsageCounts().put(timeSlotId, getTimeSlotUsageCounts().getOrDefault(timeSlotId, 0) + 1);
    }
}

package openerp.openerpresourceserver.examtimetabling.algorithm.model;

import openerp.openerpresourceserver.examtimetabling.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.CriteriaBuilder.In;

/**
 * Class that implements the calculation of solution metrics and quality score
 * Extends the basic TimetablingSolution with methods specific to the exam timetabling problem
 */
@Slf4j
@Data
@AllArgsConstructor
public class ExamTimetableSolution {
     // Maps class ID to its assignment details
    private Map<UUID, AssignmentDetails> assignedClasses;
    
    // Maps course ID to time slot ID
    private Map<String, UUID> courseTimeSlotAssignments;
    
    // Maps time slot ID to list of class IDs assigned to it
    private Map<UUID, List<UUID>> timeSlotClassAssignments;
    
    // Maps room ID to number of times it's used
    private Map<String, Integer> roomUsageCounts;
    
    // Maps time slot ID to number of times it's used
    private Map<UUID, Integer> timeSlotUsageCounts;
    
    // Optimization metrics
    private int groupSpacingViolations; // 1st priority
    private double roomBalanceMetric;   // 2nd priority
    private double timeSlotBalanceMetric; // 2nd priority
    private int earlySlotAssignments;   // 3rd priority
    
    // Overall quality score (weighted sum of metrics)
    private double qualityScore;
    
    public ExamTimetableSolution() {
        this.assignedClasses = new HashMap<>();
        this.courseTimeSlotAssignments = new HashMap<>();
        this.timeSlotClassAssignments = new HashMap<>();
        this.roomUsageCounts = new HashMap<>();
        this.timeSlotUsageCounts = new HashMap<>();
    }
    
    // Constants for optimization weights
    private static final double WEIGHT_GROUP_SPACING = 10.0;  // 1st priority
    private static final double WEIGHT_TIMESLOT_BALANCE = 7.0; // 2nd priority
    private static final double WEIGHT_ROOM_BALANCE = 3.0;    // 3nd priority
    private static final double WEIGHT_EARLY_SLOTS = 2.0;     // 4rd priority
    
    /**
     * Calculate metrics for the current solution based on the preprocessed data
     */
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
     * Calculate group spacing violations based on:
     * 1. Number of days with multiple courses
     * 2. Rest days between courses in the same group
     */
    private void calculateGroupSpacingViolations(TimetablingData data) {
        int totalViolations = 0;
        
        // Get classes grouped by group ID
        Map<Integer, List<ExamClass>> classesByGroupId = data.getClassesByGroupId();
        
        // For each group
        for (Map.Entry<Integer, List<ExamClass>> entry : classesByGroupId.entrySet()) {
            Integer groupId = entry.getKey();
            List<ExamClass> classesInGroup = entry.getValue();
            
            // Get unique course IDs in this group
            Set<String> courseIds = classesInGroup.stream()
                .map(ExamClass::getCourseId)
                .collect(Collectors.toSet());
            
            if (courseIds.size() <= 1) continue; // Skip groups with only one course
            
            // Get exam dates for all courses in this group
            List<LocalDate> examDates = new ArrayList<>();
            for (String courseId : courseIds) {
                UUID timeSlotId = getCourseTimeSlotAssignments().get(courseId);
                if (timeSlotId != null) {
                    TimeSlot timeSlot = findTimeSlot(timeSlotId, data.getAvailableTimeSlots());
                    if (timeSlot != null) {
                        examDates.add(timeSlot.getDate());
                    }
                }
            }
            
            if (examDates.size() <= 1) continue;
            
            // Sort exam dates
            examDates.sort(LocalDate::compareTo);
            
            // 1. Count days with multiple courses (violation)
            Map<LocalDate, Long> coursesPerDay = examDates.stream()
                .collect(Collectors.groupingBy(date -> date, Collectors.counting()));
            
            int multipleCoursesDays = (int) coursesPerDay.values().stream()
                .mapToLong(count -> Math.max(0, count - 1)) // Each extra course on same day is a violation
                .sum();
            
            // 2. Calculate rest day violations
            int restDayViolations = 0;
            
            // Remove duplicates and sort unique dates
            List<LocalDate> uniqueDates = examDates.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            
            for (int i = 0; i < uniqueDates.size() - 1; i++) {
                LocalDate currentDate = uniqueDates.get(i);
                LocalDate nextDate = uniqueDates.get(i + 1);
                
                long daysBetween = ChronoUnit.DAYS.between(currentDate, nextDate);
                
                // Violations based on days between exams:
                // 0 days (same day): already counted in multipleCoursesDays
                // 1 day (consecutive days): high violation
                // 2 days (1 rest day): medium violation
                // 3+ days: no violation
                
                if (daysBetween == 1) {
                    restDayViolations += 3; // High penalty for consecutive days
                } else if (daysBetween == 2) {
                    restDayViolations += 1; // Medium penalty for only 1 rest day
                }
                // daysBetween >= 3: no violation
            }
            
            // Calculate group-specific violations
            int groupViolations = multipleCoursesDays + restDayViolations;
            
            // Weight violations by group size (larger groups should have higher standards)
            double groupWeight = Math.sqrt(courseIds.size()); // Square root to avoid over-penalizing large groups
            totalViolations += (int) (groupViolations * groupWeight);
            
            // // Debug logging
            // System.out.printf("Group %d: %d courses, %d unique exam dates, %d same-day violations, %d rest-day violations, total: %d%n",
            //     groupId, courseIds.size(), uniqueDates.size(), multipleCoursesDays, restDayViolations, groupViolations);
        }
        
        setGroupSpacingViolations(totalViolations);
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
        double metric = Math.exp(-stdDev/(mean + 1));
        
        setRoomBalanceMetric(metric);
    }
    
    /**
     * Calculate the balance of time slot usage
     * Evaluates balance between exam days and balance between sessions within each day
     */
    private void calculateTimeSlotBalanceMetric() {
        if (getAssignedClasses().isEmpty()) {
            setTimeSlotBalanceMetric(1.0);
            return;
        }
        
        // Group assignments by date and session
        Map<LocalDate, Map<UUID, Integer>> dateSessionCounts = new HashMap<>();
        
        for (AssignmentDetails assignment : getAssignedClasses().values()) {
            LocalDate date = assignment.getDate();
            UUID sessionId = assignment.getSessionId();
            
            dateSessionCounts.computeIfAbsent(date, k -> new HashMap<>())
                .put(sessionId, dateSessionCounts.get(date).getOrDefault(sessionId, 0) + 1);
        }
        
        // 1. Calculate balance between exam days
        List<Integer> classesPerDay = dateSessionCounts.values().stream()
            .map(sessionMap -> sessionMap.values().stream().mapToInt(Integer::intValue).sum())
            .collect(Collectors.toList());
        
        double dayBalanceMetric = calculateBalanceScore(classesPerDay);
        
        // 2. Calculate average balance between sessions within each day
        List<Double> sessionBalanceScores = new ArrayList<>();
        
        for (Map<UUID, Integer> sessionCounts : dateSessionCounts.values()) {
            if (sessionCounts.size() > 1) { // Only calculate if there are multiple sessions
                List<Integer> classesPerSession = new ArrayList<>(sessionCounts.values());
                double sessionBalance = calculateBalanceScore(classesPerSession);
                sessionBalanceScores.add(sessionBalance);
            }
        }
        
        double avgSessionBalanceMetric = sessionBalanceScores.isEmpty() ? 1.0 : 
            sessionBalanceScores.stream().mapToDouble(Double::doubleValue).average().orElse(1.0);
        
        // Combine day balance (weight 0.6) and session balance (weight 0.4)
        double combinedMetric = (0.6 * dayBalanceMetric) + (0.4 * avgSessionBalanceMetric);
        
        setTimeSlotBalanceMetric(combinedMetric);
    }

    /**
     * Helper method to calculate balance score from a list of counts
     */
    private double calculateBalanceScore(List<Integer> counts) {
        if (counts.isEmpty()) return 1.0;
        
        double mean = counts.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        
        if (mean == 0) return 1.0;
        
        double variance = counts.stream()
            .mapToDouble(count -> Math.pow(count - mean, 2))
            .average()
            .orElse(0.0);
        
        double stdDev = Math.sqrt(variance);
        
        // Normalize to 0-1 scale where 1 is perfect balance
        return Math.exp(-stdDev / (mean + 1)); // Add 1 to prevent division by zero
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

        // System.err.println("Group Spacing Score: " + groupSpacingScore);
        // System.err.println("Room Balance Score: " + roomBalanceScore);
        // System.err.println("Time Slot Balance Score: " + timeSlotBalanceScore);
        // System.err.println("Early Slot Score: " + earlySlotScore);
        
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
    public void addAssignment(UUID classId, UUID timeSlotId, String roomId, 
                              String courseId, LocalDate date) {
        AssignmentDetails details = new AssignmentDetails();
        details.setTimeSlotId(timeSlotId);
        details.setRoomId(roomId);
        details.setDate(date);
        
        assignedClasses.put(classId, details);
        courseTimeSlotAssignments.put(courseId, timeSlotId);
        
        // Update time slot assignments
        timeSlotClassAssignments
            .computeIfAbsent(timeSlotId, k -> new ArrayList<>())
            .add(classId);
        
        // Update usage counts
        roomUsageCounts.put(roomId, roomUsageCounts.getOrDefault(roomId, 0) + 1);
        timeSlotUsageCounts.put(timeSlotId, timeSlotUsageCounts.getOrDefault(timeSlotId, 0) + 1);
        
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

package openerp.openerpresourceserver.examtimetabling.algorithm.model;

import java.time.LocalDate;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a solution to the timetabling problem
 */
@Data
@AllArgsConstructor
public class TimetablingSolution {
    // Maps class ID to its assignment details
    private Map<UUID, AssignmentDetails> assignedClasses;
    
    // Maps course ID to time slot ID
    private Map<String, UUID> courseTimeSlotAssignments;
    
    // Maps time slot ID to list of class IDs assigned to it
    private Map<UUID, List<UUID>> timeSlotClassAssignments;
    
    // Maps room ID to number of times it's used
    private Map<UUID, Integer> roomUsageCounts;
    
    // Maps time slot ID to number of times it's used
    private Map<UUID, Integer> timeSlotUsageCounts;
    
    // Optimization metrics
    private int groupSpacingViolations; // 1st priority
    private double roomBalanceMetric;   // 2nd priority
    private double timeSlotBalanceMetric; // 2nd priority
    private int earlySlotAssignments;   // 3rd priority
    
    // Overall quality score (weighted sum of metrics)
    private double qualityScore;
    
    public TimetablingSolution() {
        this.assignedClasses = new HashMap<>();
        this.courseTimeSlotAssignments = new HashMap<>();
        this.timeSlotClassAssignments = new HashMap<>();
        this.roomUsageCounts = new HashMap<>();
        this.timeSlotUsageCounts = new HashMap<>();
    }
    
    /**
     * Check if all classes are assigned
     */
    public boolean isComplete() {
        return true; // This will need to be implemented based on your criteria
    }
    
    /**
     * Add an assignment to the solution
     */
    public void addAssignment(UUID classId, UUID timeSlotId, UUID roomId, 
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
    }
    
    /**
     * Calculate metrics for optimization
     */
    public void calculateMetrics(TimetablingData data) {
        // This would calculate metrics based on the solution and constraints
        // Implementation would depend on your specific requirements
    }
}

package openerp.openerpresourceserver.examtimetabling.algorithm.model;

import java.time.LocalDate;
import java.util.*;

import openerp.openerpresourceserver.examtimetabling.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains all preprocessed data needed for the timetabling algorithm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetablingData {
    private List<ExamClass> examClasses;
    private List<ExamRoom> availableRooms;
    private List<TimeSlot> availableTimeSlots;
    private List<LocalDate> examDates;
    
    private Map<String, List<ExamClass>> classesByCourseId;
    private Map<String, List<ExamClass>> classesByGroupId;
    
    // Constraints
    private Map<UUID, Set<UUID>> conflictGraph;
    private Set<TimeSlotRoomPair> prohibitedSlots;
    private Set<UUID> earlyTimeSlots;
    
    // Existing assignments
    private List<ExamTimetableAssignment> existingAssignments;
}

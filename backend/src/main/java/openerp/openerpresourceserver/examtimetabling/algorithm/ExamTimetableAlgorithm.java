package openerp.openerpresourceserver.examtimetabling.algorithm;

import openerp.openerpresourceserver.examtimetabling.entity.*;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.*;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamRoom;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.CriteriaBuilder.In;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the exam timetabling algorithm using graph coloring and local search
 */
@Slf4j
public class ExamTimetableAlgorithm {

    // Constants for optimization weights
    private static final double WEIGHT_GROUP_SPACING = 10.0;  // 1st priority
    private static final double WEIGHT_ROOM_BALANCE = 5.0;    // 2nd priority
    private static final double WEIGHT_TIMESLOT_BALANCE = 5.0; // 2nd priority
    private static final double WEIGHT_EARLY_SLOTS = 2.0;     // 3rd priority
    
    // Maximum iterations for local search
    private static final int MAX_ITERATIONS = 1000;
    
    /**
     * Main method to assign classes to rooms and time slots
     * 
     * @param data The preprocessed timetabling data
     * @return A solution with assignments
     */
    public ExamTimetableSolution assign(TimetablingData data) {
        log.info("Starting assignment algorithm for {} classes", data.getExamClasses().size());
        
        // Create initial solution with multiple attempts for unassigned classes
        ExamTimetableSolution solution = createInitialAssignmentWithRetries(data);
        
        System.out.println("Total assigned classes: " + solution.getAssignedClasses().size() + "/" + data.getExamClasses().size());

        // return solution;
        
        // Phase 2: Improve solution using local search (Temporary discarded for now)
        ExamTimetableSolution optimizedSolution = optimizeSolution(solution, data);
        
        System.out.println("Optimization complete. Final solution quality: " + optimizedSolution.getQualityScore());
        System.out.println("Total assigned classes: " + solution.getAssignedClasses().size() + "/" + data.getExamClasses().size());
        

        return optimizedSolution;
    }

    /**
     * Create initial assignment with multiple retries for unassigned classes
     */
    private ExamTimetableSolution createInitialAssignmentWithRetries(TimetablingData data) {
        ExamTimetableSolution solution = new ExamTimetableSolution();
        
        
        // Track assigned class IDs
        Set<UUID> assignedClassIds = new HashSet<>();
        int totalClasses = data.getExamClasses().size();
        int newlyAssignedClasses;
        int maxRetries = 10;
        int retryCount = 0;
        
        // Track prohibited slots (will be updated after each iteration)
        Set<TimeSlotRoomPair> prohibitedSlots = new HashSet<>(data.getProhibitedSlots());
        
        do {
            retryCount++;
            System.out.println("Retrying assignment iteration #" + retryCount);
            
            // Create filtered data with updated prohibited slots
            TimetablingData filteredData = filterUnassignedClasses(data, assignedClassIds, prohibitedSlots);
            System.out.println("Filtered Course groups found: " + filteredData.getClassesByCourseId().size() + " course groups found");
            
            // Create initial solution for remaining classes
            ExamTimetableSolution iterationSolution = createInitialAssignment(filteredData);
            
            // Merge with existing solution
            newlyAssignedClasses = mergeSolutions(solution, iterationSolution);
            
            // Update assigned class IDs
            for (UUID classId : iterationSolution.getAssignedClasses().keySet()) {
                assignedClassIds.add(classId);
            }
            
            // Update prohibited slots from this iteration's assignments
            data.setProhibitedSlots(updateProhibitedSlots(prohibitedSlots, iterationSolution, filteredData.getAvailableTimeSlots()));

            System.out.println("Newly assigned classes in this iteration: " + newlyAssignedClasses);
            
        } while (newlyAssignedClasses > 0 && assignedClassIds.size() < totalClasses && retryCount < maxRetries);
        
        // Calculate metrics for the final solution
        solution.calculateMetrics(data);
        
        return solution;
    }

    /**
     * Update prohibited slots based on new assignments
     */
    private Set<TimeSlotRoomPair> updateProhibitedSlots(Set<TimeSlotRoomPair> prohibitedSlots, 
                                    ExamTimetableSolution solution,
                                    List<TimeSlot> availableTimeSlots) {
        // For each assigned class, mark its room-timeslot pair as prohibited
        for (Map.Entry<UUID, AssignmentDetails> entry : solution.getAssignedClasses().entrySet()) {
            AssignmentDetails details = entry.getValue();
            
            // Find the corresponding time slot to get session ID
            TimeSlot timeSlot = null;
            for (TimeSlot ts : availableTimeSlots) {
                if (ts.getId().equals(details.getTimeSlotId())) {
                    timeSlot = ts;
                    break;
                }
            }
            
            if (timeSlot != null) {
                // Create a new prohibited pair
                TimeSlotRoomPair pair = new TimeSlotRoomPair();
                pair.setRoomId(details.getRoomId());
                pair.setSessionId(timeSlot.getSessionId());
                pair.setDate(details.getDate());
                
                // Add to prohibited slots
                prohibitedSlots.add(pair);
            }
        }
        return prohibitedSlots;
    }

    /**
     * Filter data to only include unassigned classes and update prohibited slots
     */
    private TimetablingData filterUnassignedClasses(TimetablingData originalData, 
                                                Set<UUID> assignedClassIds,
                                                Set<TimeSlotRoomPair> updatedProhibitedSlots) {
        TimetablingData filteredData = new TimetablingData();
        
        // Copy basic data
        filteredData.setAvailableRooms(originalData.getAvailableRooms());
        filteredData.setAvailableTimeSlots(originalData.getAvailableTimeSlots());
        filteredData.setExamDates(originalData.getExamDates());
        filteredData.setEarlyTimeSlots(originalData.getEarlyTimeSlots());
        filteredData.setExistingAssignments(originalData.getExistingAssignments());
        
        // Use the updated prohibited slots instead of the original ones
        filteredData.setProhibitedSlots(updatedProhibitedSlots);
        
        // Filter classes to only include unassigned ones
        List<ExamClass> unassignedClasses = originalData.getExamClasses().stream()
                .filter(ec -> !assignedClassIds.contains(ec.getId()))
                .collect(Collectors.toList());
        filteredData.setExamClasses(unassignedClasses);
        
        Map<String, List<ExamClass>> filteredClassesByCourseId = new HashMap<>();
        
        // Iterate through the original sub-course groups
        for (Map.Entry<String, List<ExamClass>> entry : originalData.getClassesByCourseId().entrySet()) {
            String subCourseId = entry.getKey();
            List<ExamClass> classesInSubCourse = entry.getValue();
            
            // Filter to only include unassigned classes from this sub-course
            List<ExamClass> unassignedClassesInSubCourse = classesInSubCourse.stream()
                .filter(ec -> !assignedClassIds.contains(ec.getId()))
                .collect(Collectors.toList());
            
            // Only keep the sub-course if it has unassigned classes
            if (!unassignedClassesInSubCourse.isEmpty()) {
                filteredClassesByCourseId.put(subCourseId, unassignedClassesInSubCourse);
            }
        }
        
        filteredData.setClassesByCourseId(filteredClassesByCourseId);
        
        // Group by description group remains the same
        Map<Integer, List<ExamClass>> classesByGroupId = unassignedClasses.stream()
                .filter(ec -> ec.getExamClassGroupId() != null)
                .collect(Collectors.groupingBy(ExamClass::getExamClassGroupId));
        filteredData.setClassesByGroupId(classesByGroupId);
        
        // Filter conflict graph to only include unassigned classes
        Map<UUID, Set<UUID>> filteredConflictGraph = new HashMap<>();
        for (ExamClass examClass : unassignedClasses) {
            UUID classId = examClass.getId();
            Set<UUID> conflicts = originalData.getConflictGraph().getOrDefault(classId, new HashSet<>());
            
            // Keep only conflicts with other unassigned classes
            Set<UUID> filteredConflicts = conflicts.stream()
                    .filter(conflictId -> !assignedClassIds.contains(conflictId))
                    .collect(Collectors.toSet());
            
            filteredConflictGraph.put(classId, filteredConflicts);
        }
        filteredData.setConflictGraph(filteredConflictGraph);
        
        return filteredData;
    }

    /**
     * Merge a new solution into the existing solution
     * @return Number of newly assigned classes
     */
    private int mergeSolutions(ExamTimetableSolution existingSolution, ExamTimetableSolution newSolution) {
        int newlyAssignedCount = 0;
        
        for (Map.Entry<UUID, AssignmentDetails> entry : newSolution.getAssignedClasses().entrySet()) {
            UUID classId = entry.getKey();
            AssignmentDetails details = entry.getValue();
            
            if (!existingSolution.getAssignedClasses().containsKey(classId)) {
                existingSolution.getAssignedClasses().put(classId, details);
                newlyAssignedCount++;
            }
        }
        
        for (Map.Entry<String, UUID> entry : newSolution.getCourseTimeSlotAssignments().entrySet()) {
            String courseId = entry.getKey();
            UUID timeSlotId = entry.getValue();
            
            existingSolution.getCourseTimeSlotAssignments().put(courseId, timeSlotId);
        }
        
        for (Map.Entry<UUID, List<UUID>> entry : newSolution.getTimeSlotClassAssignments().entrySet()) {
            UUID timeSlotId = entry.getKey();
            List<UUID> classes = entry.getValue();
            
            List<UUID> existingClasses = existingSolution.getTimeSlotClassAssignments()
                    .computeIfAbsent(timeSlotId, k -> new ArrayList<>());
            
            for (UUID classId : classes) {
                if (!existingClasses.contains(classId)) {
                    existingClasses.add(classId);
                }
            }
        }
        
        for (Map.Entry<String, Integer> entry : newSolution.getRoomUsageCounts().entrySet()) {
            String roomId = entry.getKey();
            Integer count = entry.getValue();
            
            existingSolution.getRoomUsageCounts().put(roomId, 
                    existingSolution.getRoomUsageCounts().getOrDefault(roomId, 0) + count);
        }
        
        for (Map.Entry<UUID, Integer> entry : newSolution.getTimeSlotUsageCounts().entrySet()) {
            UUID timeSlotId = entry.getKey();
            Integer count = entry.getValue();
            
            existingSolution.getTimeSlotUsageCounts().put(timeSlotId, 
                    existingSolution.getTimeSlotUsageCounts().getOrDefault(timeSlotId, 0) + count);
        }
        
        return newlyAssignedCount;
    }
    
    /**
     * Create initial assignment using graph coloring approach
     */
    private ExamTimetableSolution createInitialAssignment(TimetablingData data) {
        ExamTimetableSolution solution = new ExamTimetableSolution();
        
        // Step 1: Group classes by course ID (they must be scheduled together)
        Map<String, List<ExamClass>> courseGroups = data.getClassesByCourseId();

        // Step 2: Sort course groups by most constrained first
        List<Map.Entry<String, List<ExamClass>>> sortedCourseGroups = sortCourseGroupsByConstraints(courseGroups, data);
        
        // Step 3: Assign time slots to courses using graph coloring
        Map<String, UUID> courseToTimeSlot = assignTimeSlotsToCourses(sortedCourseGroups, data);
        solution.setCourseTimeSlotAssignments(courseToTimeSlot);
        
        // Step 4: Assign rooms to classes
        boolean success = assignRoomsToClasses(solution, courseGroups, courseToTimeSlot, data);
        
        // Calculate metrics for the solution
        if (success) {
            solution.calculateMetrics(data);
        }
        
        return solution;
    }
    
    /**
     * Sort course groups by constraint level (most constrained first)
     */
    private List<Map.Entry<String, List<ExamClass>>> sortCourseGroupsByConstraints(
        Map<String, List<ExamClass>> courseGroups, TimetablingData data) {
        
        System.out.println("Sorting course groups by constraints..." + courseGroups.size() + " groups found");
        // Build course conflict graph based on group membership
        Map<String, Set<String>> courseConflictGraph = buildCourseConflictGraph(data);

        // Calculate how constrained each course is
        Map<String, Integer> courseConstraints = new HashMap<>();

        for (Map.Entry<String, List<ExamClass>> entry : courseGroups.entrySet()) {
            String courseId = entry.getKey();
            List<ExamClass> classes = entry.getValue();
            
            // 1. Number of conflicting courses (highest weight)
            int conflictingCoursesCount = courseConflictGraph.getOrDefault(courseId, Collections.emptySet()).size();
            
            // 2. Total number of students (medium weight)
            int totalStudents = classes.stream()
                .mapToInt(ExamClass::getNumberOfStudents)
                .sum();
            
            // 3. Total number of classes in the course (lowest weight)
            int classCount = classes.size();
            
            // Combined constraint score with appropriate weights
            int constraintScore = (conflictingCoursesCount * 1000) + (totalStudents * 10) + classCount;
            
            courseConstraints.put(courseId, constraintScore);
        }

        // Sort by constraint score (descending)
        List<Map.Entry<String, List<ExamClass>>> sorted = new ArrayList<>(courseGroups.entrySet());
        sorted.sort((e1, e2) -> {
            int c1 = courseConstraints.getOrDefault(e1.getKey(), 0);
            int c2 = courseConstraints.getOrDefault(e2.getKey(), 0);
            return Integer.compare(c2, c1); // Descending order
        });

        return sorted;
    }
    
    /**
    * Assign time slots to courses using graph coloring approach,
    * trying to assign as many courses as possible and maximize days between exams in the same group
    */
    private Map<String, UUID> assignTimeSlotsToCourses(
        List<Map.Entry<String, List<ExamClass>>> sortedCourseGroups, TimetablingData data) {

        Map<String, UUID> courseToTimeSlot = new HashMap<>();
        Map<UUID, Set<String>> timeSlotToCourses = new HashMap<>();

        // Build course conflict graph (from both group conflicts and explicit conflicts)
        Map<String, Set<String>> courseConflictGraph = buildCourseConflictGraph(data);

        // Track group information
        Map<Integer, List<LocalDate>> groupUsedDates = new HashMap<>(); // Dates used by each group
        Map<Integer, Integer> groupCourseCount = new HashMap<>(); // Number of courses assigned per group
        
        int totalCourses = sortedCourseGroups.size();
        System.out.printf("Total courses to assign: %d%n", totalCourses);
        int assignedCourses = 0;

        // Get all available dates sorted
        List<LocalDate> allAvailableDates = data.getAvailableTimeSlots().stream()
            .map(TimeSlot::getDate)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        Random random = new Random();

        // For each course, find a valid time slot
        for (Map.Entry<String, List<ExamClass>> courseEntry : sortedCourseGroups) {
            String courseId = courseEntry.getKey();
            List<ExamClass> classes = courseEntry.getValue();
            
            // Find the group this course belongs to
            Integer courseGroup = null;
            for (ExamClass examClass : classes) {
                if (examClass.getExamClassGroupId() != null) {
                    courseGroup = examClass.getExamClassGroupId();
                    break;
                }
            }
            
            // Get set of time slots that can't be used (hard constraints)
            Set<UUID> invalidTimeSlots = new HashSet<>();
            
            // Get set of time slots to avoid if possible (soft constraints)
            Set<UUID> preferredToAvoidTimeSlots = new HashSet<>();
            
            // Hard constraint: Check conflicting courses (cannot be in same time slot)
            Set<String> conflictingCourses = courseConflictGraph.getOrDefault(courseId, Collections.emptySet());
            for (String conflictingCourse : conflictingCourses) {
                // If the conflicting course is already assigned, add its time slot to invalid set
                if (courseToTimeSlot.containsKey(conflictingCourse)) {
                    invalidTimeSlots.add(courseToTimeSlot.get(conflictingCourse));
                    
                    // SOFT CONSTRAINT: Avoid same day and consecutive days if possible
                    UUID conflictTimeSlotId = courseToTimeSlot.get(conflictingCourse);
                    TimeSlot conflictTimeSlot = findTimeSlot(conflictTimeSlotId, data.getAvailableTimeSlots());
                    
                    if (conflictTimeSlot != null) {
                        for (TimeSlot otherSlot : data.getAvailableTimeSlots()) {
                            // Same day
                            if (otherSlot.isSameDayAs(conflictTimeSlot)) {
                                preferredToAvoidTimeSlots.add(otherSlot.getId());
                            }
                        }
                    }
                }
            }
            
            UUID assignedTimeSlot = null;
            LocalDate selectedDate = null;
            
            // Filter to only valid time slots (must satisfy hard constraints)
            List<TimeSlot> validTimeSlots = data.getAvailableTimeSlots().stream()
                .filter(ts -> !invalidTimeSlots.contains(ts.getId()))
                .collect(Collectors.toList());
            
            if (validTimeSlots.isEmpty()) {
                System.out.printf("No valid time slots available for course %s. Skipping.%n", courseId);
                continue;
            }
            
            if (courseGroup != null) {
                // Initialize group data if first time
                groupUsedDates.putIfAbsent(courseGroup, new ArrayList<>());
                groupCourseCount.putIfAbsent(courseGroup, 0);
                
                int coursePositionInGroup = groupCourseCount.get(courseGroup) + 1;
                
                if (coursePositionInGroup == 1) {
                    // First course: choose from first 3 dates
                    List<LocalDate> firstThreeDates = allAvailableDates.stream()
                        .limit(3)
                        .collect(Collectors.toList());
                    
                    selectedDate = firstThreeDates.get(random.nextInt(firstThreeDates.size()));
                    // System.out.printf("Course %s (1st in group %d): Selected early date %s%n", 
                    //     courseId, courseGroup, selectedDate);
                    
                } else if (coursePositionInGroup == 2) {
                    // Second course: choose from last 3 dates
                    List<LocalDate> lastThreeDates = allAvailableDates.stream()
                        .skip(Math.max(0, allAvailableDates.size() - 3))
                        .collect(Collectors.toList());
                    
                    selectedDate = lastThreeDates.get(random.nextInt(lastThreeDates.size()));
                    // System.out.printf("Course %s (2nd in group %d): Selected late date %s%n", 
                    //     courseId, courseGroup, selectedDate);
                    
                } else {
                    // Third course and beyond: find optimal gap
                    List<LocalDate> usedDates = new ArrayList<>(groupUsedDates.get(courseGroup));
                    usedDates.sort(LocalDate::compareTo);
                    
                    // Check if all available dates are used by this group
                    Set<LocalDate> usedDateSet = new HashSet<>(usedDates);
                    boolean allDatesUsed = usedDateSet.containsAll(allAvailableDates);
                    
                    if (allDatesUsed) {
                        // All dates used, choose date with minimum courses from this group
                        Map<LocalDate, Long> dateUsageCount = usedDates.stream()
                            .collect(Collectors.groupingBy(date -> date, Collectors.counting()));
                        
                        selectedDate = dateUsageCount.entrySet().stream()
                            .min(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(allAvailableDates.get(0));
                        
                        // System.out.printf("Course %s (group %d): All dates used, selected least used date %s%n", 
                        //     courseId, courseGroup, selectedDate);
                            
                    } else {
                        // Find the largest gap between consecutive used dates
                        LocalDate finalGapStartDate = null;
                        LocalDate finalGapEndDate = null;
                        long maxGapDays = 0;
                        
                        for (int i = 0; i < usedDates.size() - 1; i++) {
                            LocalDate dateA = usedDates.get(i);
                            LocalDate dateB = usedDates.get(i + 1);
                            
                            // Check if there's no other used date between A and B
                            boolean hasDateBetween = false;
                            for (LocalDate usedDate : usedDates) {
                                if (usedDate.isAfter(dateA) && usedDate.isBefore(dateB)) {
                                    hasDateBetween = true;
                                    break;
                                }
                            }
                            
                            if (!hasDateBetween) {
                                boolean hasAvailableDateBetween = false;
                                for (LocalDate availableDate : allAvailableDates) {
                                    if (availableDate.isAfter(dateA) && availableDate.isBefore(dateB)) {
                                        hasAvailableDateBetween = true;
                                        break;
                                    }
                                }
                                
                                if (hasAvailableDateBetween) {
                                    long gapDays = ChronoUnit.DAYS.between(dateA, dateB);
                                    if (gapDays > maxGapDays) {
                                        maxGapDays = gapDays;
                                        finalGapStartDate = dateA;
                                        finalGapEndDate = dateB;
                                    }
                                }
                            }
                        }
                        
                        if (finalGapStartDate != null && finalGapEndDate != null && maxGapDays > 1) {
                            // Calculate middle date
                            long daysBetween = ChronoUnit.DAYS.between(finalGapStartDate, finalGapEndDate);
                            LocalDate middleDate = finalGapStartDate.plusDays(daysBetween / 2);
                            
                            // Get 3 dates around middle date (middle-1, middle, middle+1)
                            List<LocalDate> candidateDates = new ArrayList<>();
                            LocalDate dayBefore = middleDate.minusDays(1);
                            LocalDate dayAfter = middleDate.plusDays(1);
                            
                            // Add dates that are within the gap and available
                            final LocalDate finalGapStart = finalGapStartDate;
                            final LocalDate finalGapEnd = finalGapEndDate;
                            
                            if (allAvailableDates.contains(dayBefore) && dayBefore.isAfter(finalGapStart) && dayBefore.isBefore(finalGapEnd)) {
                                candidateDates.add(dayBefore);
                            }
                            if (allAvailableDates.contains(middleDate) && middleDate.isAfter(finalGapStart) && middleDate.isBefore(finalGapEnd)) {
                                candidateDates.add(middleDate);
                            }
                            if (allAvailableDates.contains(dayAfter) && dayAfter.isAfter(finalGapStart) && dayAfter.isBefore(finalGapEnd)) {
                                candidateDates.add(dayAfter);
                            }
                            
                            // If no candidate dates in the gap, fall back to any available dates in the gap
                            if (candidateDates.isEmpty()) {
                                candidateDates = allAvailableDates.stream()
                                    .filter(date -> date.isAfter(finalGapStart) && date.isBefore(finalGapEnd))
                                    .collect(Collectors.toList());
                            }
                            
                            
                            // Count existing classes for each candidate date across ALL time slots
                            Map<LocalDate, Long> dateClassCount = new HashMap<>();
                            
                            for (LocalDate candidateDate : candidateDates) {
                                // Count classes already assigned to this date across all courses
                                long classCount = courseToTimeSlot.values().stream()
                                    .mapToLong(timeSlotId -> {
                                        TimeSlot timeSlot = findTimeSlot(timeSlotId, data.getAvailableTimeSlots());
                                        return (timeSlot != null && timeSlot.getDate().equals(candidateDate)) ? 1L : 0L;
                                    })
                                    .sum();
                                
                                dateClassCount.put(candidateDate, classCount);
                            }
                            
                            // Select the date with minimum class count
                            selectedDate = dateClassCount.entrySet().stream()
                                .min(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse(candidateDates.get(0));
                            
                            // System.out.printf("Course %s (group %d): Found gap between %s and %s, candidate dates %s, selected date %s with %d existing classes%n", 
                            //     courseId, courseGroup, finalGapStartDate, finalGapEndDate, 
                            //     candidateDates, selectedDate, dateClassCount.get(selectedDate));
                           
                        } else {
                            // No significant gap found, choose any unused date
                            selectedDate = allAvailableDates.stream()
                                .filter(date -> !usedDateSet.contains(date))
                                .findFirst()
                                .orElse(allAvailableDates.get(0));
                            
                            // System.out.printf("Course %s (group %d): No significant gap, selected unused date %s%n", 
                            //     courseId, courseGroup, selectedDate);
                        }
                    }
                }
                
                // Update group tracking
                groupUsedDates.get(courseGroup).add(selectedDate);
                groupCourseCount.put(courseGroup, coursePositionInGroup);
                
            } else {
                System.out.printf("Course %s is not part of a group. Assigning time slot without group constraints.%n", courseId);
                // This course is not part of a group, use standard selection
                selectedDate = validTimeSlots.get(0).getDate();
            }
            
            // Find time slots on the selected date that satisfy constraints
            final LocalDate finalSelectedDate = selectedDate;
            List<TimeSlot> slotsOnSelectedDate = validTimeSlots.stream()
                .filter(ts -> ts.getDate().equals(finalSelectedDate))
                .collect(Collectors.toList());
            
            // Apply soft constraints
            List<TimeSlot> bestTimeSlots = slotsOnSelectedDate.stream()
                .filter(ts -> !preferredToAvoidTimeSlots.contains(ts.getId()))
                .collect(Collectors.toList());
            
            if (!bestTimeSlots.isEmpty()) {
                slotsOnSelectedDate = bestTimeSlots;
            }
            
            if (!slotsOnSelectedDate.isEmpty()) {
                // Sort by time slot preferences and select the best one
                slotsOnSelectedDate.sort(createTimeSlotComparator(data, timeSlotToCourses));
                assignedTimeSlot = slotsOnSelectedDate.get(0).getId();
                
                courseToTimeSlot.put(courseId, assignedTimeSlot);
                timeSlotToCourses.computeIfAbsent(assignedTimeSlot, k -> new HashSet<>()).add(courseId);
                assignedCourses++;
                
                // System.out.printf("Course %s assigned to date %s%n", courseId, finalSelectedDate);
            } else {
                System.out.printf("Could not find suitable time slot for course %s on selected date %s%n", courseId, finalSelectedDate);
            }
        }

        System.out.printf("Time slot assignment completed: %d/%d courses assigned successfully%n", 
            assignedCourses, totalCourses);

        return courseToTimeSlot;
    }

    /**
     * Helper method to find a time slot by ID
     */
    private TimeSlot findTimeSlot(UUID timeSlotId, List<TimeSlot> availableTimeSlots) {
        for (TimeSlot ts : availableTimeSlots) {
            if (ts.getId().equals(timeSlotId)) {
                return ts;
            }
        }
        return null;
    }

    /**
    * Create a comparator for sorting time slots by preference
    */
    private Comparator<TimeSlot> createTimeSlotComparator(
        TimetablingData data, Map<UUID, Set<String>> timeSlotToCourses) {

        return (ts1, ts2) -> {
            boolean ts1Early = data.getEarlyTimeSlots().contains(ts1.getId());
            boolean ts2Early = data.getEarlyTimeSlots().contains(ts2.getId());

            // Sort by usage count (least used first)
            int ts1Usage = timeSlotToCourses.getOrDefault(ts1.getId(), Collections.emptySet()).size();
            int ts2Usage = timeSlotToCourses.getOrDefault(ts2.getId(), Collections.emptySet()).size();
            int result = Integer.compare(ts1Usage, ts2Usage);

            if (result != 0) {
                return result; // Prefer less used time slots
            }

            // Sort by preference (early slots last)
            
            if (ts1Early && !ts2Early) return 1;
            if (!ts1Early && ts2Early) return -1;

            return 1;
        };
    }

    /**
    * Build a map of courses grouped by their group IDs
    */
    private Map<String, Set<String>> buildCourseGroupMap(TimetablingData data) {
        Map<String, Set<String>> coursesByGroup = new HashMap<>();

        for (ExamClass examClass : data.getExamClasses()) {
            String courseId = examClass.getCourseId();
            Integer groupId = examClass.getExamClassGroupId();
            
            if (groupId != null) {
                for (ExamClass otherClass : data.getExamClasses()) {
                    if (groupId.equals(otherClass.getExamClassGroupId())) {
                        coursesByGroup.computeIfAbsent(courseId, k -> new HashSet<>())
                            .add(otherClass.getCourseId());
                    }
                }
            }
        }

        return coursesByGroup;
    }
    
    /**
     * Build a graph of course conflicts based on courses in the same group
     * and explicit conflicts from ConflictExamTimetablingClass
     */
    private Map<String, Set<String>> buildCourseConflictGraph(TimetablingData data) {
        Map<String, Set<String>> courseConflictGraph = new HashMap<>();
        
        // Initialize empty sets for all courses (including sub-courses)
        for (String courseId : data.getClassesByCourseId().keySet()) {
            courseConflictGraph.put(courseId, new HashSet<>());
        }
        
        // 1. Add conflicts from same group (different course IDs)
        // Group courses by group ID using the sub-course structure
        Map<Integer, Set<String>> coursesByGroup = new HashMap<>();
        
        // Build mapping of which sub-courses are in which groups
        for (Map.Entry<String, List<ExamClass>> courseEntry : data.getClassesByCourseId().entrySet()) {
            String subCourseId = courseEntry.getKey(); // This could be "MATH101" or "MATH101_SUB_1"
            List<ExamClass> classesInSubCourse = courseEntry.getValue();
            
            // Get the group IDs that this sub-course belongs to
            Set<Integer> groupIds = classesInSubCourse.stream()
                .map(ExamClass::getExamClassGroupId)
                .filter(groupId -> groupId != null)
                .collect(Collectors.toSet());
            
            // Add this sub-course to each group it belongs to
            for (Integer groupId : groupIds) {
                coursesByGroup.computeIfAbsent(groupId, k -> new HashSet<>())
                    .add(subCourseId);
            }
        }
        
        // For each group, add conflicts between all courses in that group
        for (Set<String> coursesInGroup : coursesByGroup.values()) {
            // If there's only one course in the group, no conflicts to add
            if (coursesInGroup.size() <= 1) continue;
            
            // For each pair of different courses in the group, add conflict
            for (String courseId1 : coursesInGroup) {
                for (String courseId2 : coursesInGroup) {
                    // Don't add conflict with self
                    if (!courseId1.equals(courseId2)) {
                        courseConflictGraph.get(courseId1).add(courseId2);
                    }
                }
            }
        }
        
        // 2. Add conflicts from ConflictExamTimetablingClass table
        // Build a map of class ID to sub-course ID for quick lookup
        Map<UUID, String> classIdToSubCourseId = new HashMap<>();
        for (Map.Entry<String, List<ExamClass>> courseEntry : data.getClassesByCourseId().entrySet()) {
            String subCourseId = courseEntry.getKey();
            for (ExamClass examClass : courseEntry.getValue()) {
                classIdToSubCourseId.put(examClass.getId(), subCourseId);
            }
        }
        
        // Process each conflict pair from the conflict graph
        for (Map.Entry<UUID, Set<UUID>> entry : data.getConflictGraph().entrySet()) {
            UUID classId1 = entry.getKey();
            Set<UUID> conflictingClassIds = entry.getValue();
            
            String subCourseId1 = classIdToSubCourseId.get(classId1);
            if (subCourseId1 == null) continue; // Skip if we can't find sub-course ID
            
            for (UUID classId2 : conflictingClassIds) {
                String subCourseId2 = classIdToSubCourseId.get(classId2);
                if (subCourseId2 == null || subCourseId1.equals(subCourseId2)) continue; // Skip same sub-course conflicts
                
                // Add conflict between these sub-courses
                courseConflictGraph.computeIfAbsent(subCourseId1, k -> new HashSet<>()).add(subCourseId2);
                courseConflictGraph.computeIfAbsent(subCourseId2, k -> new HashSet<>()).add(subCourseId1);
            }
        }
        
        return courseConflictGraph;
    }
    
    /**
     * Assign rooms to classes, trying to assign as many as possible
     * With additional soft constraint to keep classes with same course and group in the same building
     */
    private boolean assignRoomsToClasses(
        ExamTimetableSolution solution,
        Map<String, List<ExamClass>> courseGroups,
        Map<String, UUID> courseToTimeSlot,
        TimetablingData data) {

        boolean allSuccessful = true;
        int totalClasses = 0;
        int assignedClasses = 0;

        // Map to track room usage at each time slot
        Map<TimeSlotRoomPair, Boolean> roomTimeSlotUsage = new HashMap<>();

        // Initialize with prohibited slots
        for (TimeSlotRoomPair prohibited : data.getProhibitedSlots()) {
            roomTimeSlotUsage.put(prohibited, true);
        }

        // Map to track which building is used for each course+group combination
        Map<String, String> courseGroupBuildingMap = new HashMap<>();

        // For each course, assign rooms to its classes
        for (Map.Entry<String, UUID> entry : courseToTimeSlot.entrySet()) {
            String courseId = entry.getKey();
            UUID timeSlotId = entry.getValue();
            
            List<ExamClass> classesInCourse = courseGroups.get(courseId);
            if (classesInCourse == null) continue;
            
            totalClasses += classesInCourse.size();
            
            // Find the time slot object
            TimeSlot timeSlot = null;
            for (TimeSlot ts : data.getAvailableTimeSlots()) {
                if (ts.getId().equals(timeSlotId)) {
                    timeSlot = ts;
                    break;
                }
            }
            
            if (timeSlot == null) {
                log.error("Could not find time slot with ID {}", timeSlotId);
                allSuccessful = false;
                continue; 
            }
            
            // Sort classes by number of students (largest first)
            List<ExamClass> sortedClasses = new ArrayList<>(classesInCourse);
            sortedClasses.sort((c1, c2) -> 
                Integer.compare(c2.getNumberOfStudents(), c1.getNumberOfStudents()));
            
            // Assign rooms to classes
            for (ExamClass examClass : sortedClasses) {
                int requiredCapacity = examClass.getNumberOfStudents() * 15 / 10; // Room needs 2n seats
                
                String courseGroupKey = courseId + "_" + (examClass.getExamClassGroupId() != null ? examClass.getExamClassGroupId() : "");
                
                // Check if we already assigned a building for this course+group
                String preferredBuilding = courseGroupBuildingMap.get(courseGroupKey);
                
                // Find suitable rooms
                List<ExamRoom> suitableRooms = data.getAvailableRooms().stream()
                    .filter(room -> room.getNumberSeat() >= requiredCapacity)
                    .sorted((r1, r2) -> {
                        // If we have a preferred building, prioritize rooms in that building
                        if (preferredBuilding != null) {
                            String building1 = extractBuildingFromRoomName(r1.getName());
                            String building2 = extractBuildingFromRoomName(r2.getName());
                            
                            boolean r1InPreferredBuilding = preferredBuilding.equals(building1);
                            boolean r2InPreferredBuilding = preferredBuilding.equals(building2);
                            
                            if (r1InPreferredBuilding && !r2InPreferredBuilding) return -1;
                            if (!r1InPreferredBuilding && r2InPreferredBuilding) return 1;
                        }
                        
                        // Sort by size (smallest suitable room first)
                        int compare = Integer.compare(r1.getNumberSeat(), r2.getNumberSeat());
                        if (compare != 0) return compare;
                        
                        // If same size, sort by usage count (least used first)
                        int r1Usage = solution.getRoomUsageCounts().getOrDefault(r1.getId(), 0);
                        int r2Usage = solution.getRoomUsageCounts().getOrDefault(r2.getId(), 0);
                        return Integer.compare(r1Usage, r2Usage);
                    })
                    .collect(Collectors.toList());
                
                // Find an available room
                ExamRoom assignedRoom = null;
                for (ExamRoom room : suitableRooms) {
                    // Create key for room-time slot pair
                    TimeSlotRoomPair pair = new TimeSlotRoomPair();
                    pair.setRoomId(room.getId());
                    pair.setSessionId(timeSlot.getSessionId());
                    pair.setDate(timeSlot.getDate());
                    
                    // Check if room is available at this time slot
                    if (!roomTimeSlotUsage.getOrDefault(pair, false)) {
                        assignedRoom = room;
                        roomTimeSlotUsage.put(pair, true); // Mark as used
                        break;
                    }
                }
                
                // If no suitable room is available, skip this class but continue with others
                if (assignedRoom == null) {
                    log.warn("Could not find suitable room for class {} at time slot {}", 
                        examClass.getId(), timeSlotId);
                    allSuccessful = false;
                    continue;
                }
                
                // If this is the first class for this course+group, record the building
                if (preferredBuilding == null && assignedRoom != null) {
                    String building = extractBuildingFromRoomName(assignedRoom.getName());
                    courseGroupBuildingMap.put(courseGroupKey, building);
                }
                
                solution.addAssignment(
                    examClass.getId(), 
                    timeSlotId, 
                    assignedRoom.getId(), 
                    courseId,
                    timeSlot.getDate());
                
                // Set session ID in assignment details
                AssignmentDetails details = solution.getAssignedClasses().get(examClass.getId());
                details.setSessionId(timeSlot.getSessionId());
                
                assignedClasses++;
            }
        }

        System.out.println("Room assignment completed:" + assignedClasses);

        return allSuccessful;
    }

    /**
    * Extract building name from room name
    * Room name format: "building_name-room_number"
    */
    private String extractBuildingFromRoomName(String roomName) {
        if (roomName == null || roomName.isEmpty() || !roomName.contains("-")) {
            return null;
        }

        return roomName.substring(0, roomName.indexOf("-"));
    }
    
    /**
     * Optimize solution using Simulated Annealing with advanced move strategies
     */
    private ExamTimetableSolution optimizeSolution(ExamTimetableSolution initialSolution, TimetablingData data) {
        ExamTimetableSolution currentSolution = deepCopy(initialSolution);
        currentSolution.calculateMetrics(data);

        double currentScore = currentSolution.getQualityScore();
        ExamTimetableSolution bestSolution = deepCopy(currentSolution);
        double bestScore = currentScore;

        Random random = new Random();
        int iterations = 0;

        double temperature = 1.0;
        double coolingRate = 0.995;
        double minTemperature = 1e-4;

        // Track move success rates for adaptive selection
        Map<String, Integer> moveSuccessCount = new HashMap<>();
        Map<String, Integer> moveTotalCount = new HashMap<>();
        String[] moveTypes = {"sameDayFix", "consecutiveDayFix", "gapOptimization", 
                            "dayBalancing", "sessionBalancing", "roomReallocation"};
        
        for (String moveType : moveTypes) {
            moveSuccessCount.put(moveType, 0);
            moveTotalCount.put(moveType, 0);
        }

        while (iterations < MAX_ITERATIONS && temperature > minTemperature) {
            iterations++;
            // Print progress every 100 iterations
            if (iterations % 100 == 0) {
                System.out.println("Iteration: " + iterations + ", Current Score: " + currentScore + ", Best Score: " + bestScore);
            }

            
            // Apply batch moves occasionally (10% chance)
            ExamTimetableSolution neighborSolution;
            if (random.nextDouble() < 0.1) {
                neighborSolution = applyBatchMoves(currentSolution, data, random, temperature);
            } else {
                neighborSolution = applySmartRandomMove(currentSolution, data, random, temperature, 
                                                    moveSuccessCount, moveTotalCount);
            }
            
            neighborSolution.calculateMetrics(data);

            double neighborScore = neighborSolution.getQualityScore();
            // System.out.println(String.format("Neighbor score: %.4f", neighborScore));
            double delta = neighborScore - currentScore;
            
            boolean accepted = false;
            if (delta > 0 || Math.exp(delta / temperature) > random.nextDouble()) {
                currentSolution = neighborSolution;
                currentScore = neighborScore;
                accepted = true;
                
                if (neighborScore > bestScore) {
                    bestSolution = deepCopy(neighborSolution);
                    bestScore = neighborScore;
                    System.out.println(String.format("Iteration %d: New best score %.4f (Group: %d, TimeSlot: %.3f, Room: %.3f)", 
                        iterations, bestScore, currentSolution.getGroupSpacingViolations(),
                        currentSolution.getTimeSlotBalanceMetric(), currentSolution.getRoomBalanceMetric()));
                }
            }

            temperature *= coolingRate;
        }

        System.out.println(String.format("Optimization completed after %d iterations. Final score: %.4f", iterations, bestScore));
        return bestSolution;
    }

    /**
     * Apply smart random move with priority-based and weighted selection
     */
    private ExamTimetableSolution applySmartRandomMove(ExamTimetableSolution solution, TimetablingData data, 
                                                    Random random, double temperature,
                                                    Map<String, Integer> successCount, 
                                                    Map<String, Integer> totalCount) {
        ExamTimetableSolution neighbor = deepCopy(solution);
        
        // Priority-based move selection with adaptive weights
        double[] baseProbabilities = {0.3, 0.25, 0.15, 0.15, 0.1, 0.05}; // Base probabilities for each move type
        String[] moveTypes = {"sameDayFix", "consecutiveDayFix", "gapOptimization", 
                            "dayBalancing", "sessionBalancing", "roomReallocation"};
        
        // Adjust probabilities based on success rates and current solution quality
        double[] adjustedProbabilities = new double[baseProbabilities.length];
        for (int i = 0; i < moveTypes.length; i++) {
            String moveType = moveTypes[i];
            double successRate = totalCount.get(moveType) > 0 ? 
                (double) successCount.get(moveType) / totalCount.get(moveType) : 0.5;
            
            // Boost probability for successful moves, especially early in optimization
            double temperatureFactor = Math.min(1.0, temperature * 2); // Higher temperature = more exploration
            adjustedProbabilities[i] = baseProbabilities[i] * (0.5 + successRate * 0.5) * (0.7 + temperatureFactor * 0.3);
        }
        
        // Normalize probabilities
        double sum = Arrays.stream(adjustedProbabilities).sum();
        for (int i = 0; i < adjustedProbabilities.length; i++) {
            adjustedProbabilities[i] /= sum;
        }
        
        // Select move type
        double rand = random.nextDouble();
        double cumulative = 0;
        int selectedMove = 0;
        for (int i = 0; i < adjustedProbabilities.length; i++) {
            cumulative += adjustedProbabilities[i];
            if (rand < cumulative) {
                selectedMove = i;
                break;
            }
        }
        
        String selectedMoveType = moveTypes[selectedMove];
        totalCount.put(selectedMoveType, totalCount.get(selectedMoveType) + 1);
        
        boolean moveSuccessful = false;
        switch (selectedMove) {
            case 0:
                moveSuccessful = sameDayViolationFix(neighbor, data, random);
                break;
            case 1:
                moveSuccessful = consecutiveDayViolationFix(neighbor, data, random);
                break;
            case 2:
                moveSuccessful = gapOptimization(neighbor, data, random);
                break;
            case 3:
                moveSuccessful = dayLevelBalancing(neighbor, data, random);
                break;
            case 4:
                moveSuccessful = sessionLevelBalancing(neighbor, data, random);
                break;
            case 5:
                moveSuccessful = roomReallocation(neighbor, data, random);
                break;
        }
        
        if (moveSuccessful) {
            successCount.put(selectedMoveType, successCount.get(selectedMoveType) + 1);
        }
        
        return neighbor;
    }

    /**
     * Apply batch moves - multiple coordinated moves
     */
    private ExamTimetableSolution applyBatchMoves(ExamTimetableSolution solution, TimetablingData data, 
                                                Random random, double temperature) {
        ExamTimetableSolution neighbor = deepCopy(solution);
        
        // Apply 2-3 moves in sequence, focusing on the most problematic aspects
        int numMoves = 2 + random.nextInt(2); // 2-3 moves
        
        for (int i = 0; i < numMoves; i++) {
            // First move: always try to fix group spacing if violations exist
            if (i == 0 && neighbor.getGroupSpacingViolations() > 0) {
                sameDayViolationFix(neighbor, data, random);
            }
            // Second move: balance or optimize
            else if (i == 1) {
                if (random.nextBoolean()) {
                    dayLevelBalancing(neighbor, data, random);
                } else {
                    gapOptimization(neighbor, data, random);
                }
            }
            // Third move: fine-tuning
            else {
                if (random.nextBoolean()) {
                    sessionLevelBalancing(neighbor, data, random);
                } else {
                    roomReallocation(neighbor, data, random);
                }
            }
        }
        
        return neighbor;
    }

    /**
     * Move 1: Fix same-day violations in groups
     */
    private boolean sameDayViolationFix(ExamTimetableSolution solution, TimetablingData data, Random random) {
        System.out.println("Starting same-day violation fix...");
        // Find group with most same-day violations
        Map<Integer, List<LocalDate>> groupExamDates = analyzeGroupExamDates(solution, data);
        
        Integer worstGroup = null;
        int maxViolations = 0;
        
        for (Map.Entry<Integer, List<LocalDate>> entry : groupExamDates.entrySet()) {
            Map<LocalDate, Long> dateCount = entry.getValue().stream()
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));
            
            int violations = dateCount.values().stream()
                .mapToInt(count -> (int) Math.max(0, count - 1))
                .sum();
            
            if (violations > maxViolations) {
                maxViolations = violations;
                worstGroup = entry.getKey();
            }
        }
        
        if (worstGroup == null || maxViolations == 0) return false;
        
        // Find date with most courses for this group
        List<LocalDate> groupDates = groupExamDates.get(worstGroup);
        Map<LocalDate, List<String>> coursesPerDate = new HashMap<>();
        
        for (Map.Entry<String, UUID> courseEntry : solution.getCourseTimeSlotAssignments().entrySet()) {
            String courseId = courseEntry.getKey();
            
            // Check if this course belongs to the worst group
            if (belongsToGroup(courseId, worstGroup, data)) {
                TimeSlot timeSlot = findTimeSlot(courseEntry.getValue(), data.getAvailableTimeSlots());
                if (timeSlot != null) {
                    coursesPerDate.computeIfAbsent(timeSlot.getDate(), k -> new ArrayList<>()).add(courseId);
                }
            }
        }
        
        // Find date with most courses
        LocalDate worstDate = coursesPerDate.entrySet().stream()
            .max(Map.Entry.<LocalDate, List<String>>comparingByValue((l1, l2) -> Integer.compare(l1.size(), l2.size())))
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (worstDate == null || coursesPerDate.get(worstDate).size() <= 1) return false;
        
        // Select a course to move
        List<String> coursesToMove = coursesPerDate.get(worstDate);
        String courseToMove = coursesToMove.get(random.nextInt(coursesToMove.size()));
        
        // Find best target date (no courses from this group, minimum total classes)
        Set<LocalDate> groupUsedDates = new HashSet<>(groupDates);
        
        List<TimeSlot> candidateSlots = data.getAvailableTimeSlots().stream()
            .filter(ts -> !groupUsedDates.contains(ts.getDate()))
            .filter(ts -> isValidMoveTarget(courseToMove, ts, solution, data))
            .collect(Collectors.toList());
        
        if (candidateSlots.isEmpty()) return false;
        
        // Select slot with minimum classes
        TimeSlot bestSlot = candidateSlots.stream()
            .min(Comparator.comparing(ts -> countClassesOnDate(ts.getDate(), solution, data)))
            .orElse(null);
        
        if (bestSlot != null) {
            return moveCourseToTimeSlot(courseToMove, bestSlot, solution, data);
        }
        
        return false;
    }

    /**
     * Move 2: Fix consecutive day violations
     */
    private boolean consecutiveDayViolationFix(ExamTimetableSolution solution, TimetablingData data, Random random) {
        System.out.println("Starting consecutive day violation fix...");
        Map<Integer, List<LocalDate>> groupExamDates = analyzeGroupExamDates(solution, data);
        
        for (Map.Entry<Integer, List<LocalDate>> entry : groupExamDates.entrySet()) {
            Integer groupId = entry.getKey();
            List<LocalDate> dates = entry.getValue().stream().distinct().sorted().collect(Collectors.toList());
            
            // Find consecutive dates
            for (int i = 0; i < dates.size() - 1; i++) {
                LocalDate date1 = dates.get(i);
                LocalDate date2 = dates.get(i + 1);
                
                if (ChronoUnit.DAYS.between(date1, date2) == 1) {
                    // Found consecutive dates, try to move one course
                    List<String> coursesOnDate2 = getCoursesOnDate(date2, groupId, solution, data);
                    
                    if (!coursesOnDate2.isEmpty()) {
                        String courseToMove = coursesOnDate2.get(random.nextInt(coursesOnDate2.size()));
                        
                        // Find a slot at least 2 days away
                        List<TimeSlot> validSlots = data.getAvailableTimeSlots().stream()
                            .filter(ts -> Math.abs(ChronoUnit.DAYS.between(ts.getDate(), date1)) >= 2)
                            .filter(ts -> !dates.contains(ts.getDate())) // Ensure not already used
                            .filter(ts -> isValidMoveTarget(courseToMove, ts, solution, data))
                            .collect(Collectors.toList());
                        
                        if (!validSlots.isEmpty()) {
                            TimeSlot targetSlot = validSlots.get(random.nextInt(validSlots.size()));
                            return moveCourseToTimeSlot(courseToMove, targetSlot, solution, data);
                        }
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Move 3: Gap optimization within groups
     */
    private boolean gapOptimization(ExamTimetableSolution solution, TimetablingData data, Random random) {
        System.out.println("Starting gap optimization...");
        Map<Integer, List<LocalDate>> groupExamDates = analyzeGroupExamDates(solution, data);
        
        for (Map.Entry<Integer, List<LocalDate>> entry : groupExamDates.entrySet()) {
            Integer groupId = entry.getKey();
            List<LocalDate> dates = entry.getValue().stream().distinct().sorted().collect(Collectors.toList());
            
            if (dates.size() < 3) continue; // Need at least 3 dates for gap optimization
            
            // Calculate current distribution score
            double currentScore = calculateDistributionScore(dates);
            
            // Try moving each course to improve distribution
            for (LocalDate date : dates) {
                List<String> coursesOnDate = getCoursesOnDate(date, groupId, solution, data);
                
                for (String course : coursesOnDate) {
                     List<TimeSlot> validSlots = data.getAvailableTimeSlots().stream()
                        .filter(ts -> !dates.contains(ts.getDate())) // Ensure not already used
                        .filter(ts -> isValidMoveTarget(course, ts, solution, data))
                        .collect(Collectors.toList());
                    // Try different target dates
                    for (TimeSlot targetSlot : validSlots) {
                        // Calculate new distribution score
                        List<LocalDate> newDates = new ArrayList<>(dates);
                        newDates.remove(date);
                        newDates.add(targetSlot.getDate());
                        newDates = newDates.stream().distinct().sorted().collect(Collectors.toList());
                        
                        double newScore = calculateDistributionScore(newDates);
                        
                        if (newScore > currentScore) {
                            return moveCourseToTimeSlot(course, targetSlot, solution, data);
                        }
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Move 4: Day-level balancing
     */
    private boolean dayLevelBalancing(ExamTimetableSolution solution, TimetablingData data, Random random) {
        System.out.println("Starting day-level balancing...");
        // Find day with most classes and day with least classes
        Map<LocalDate, Integer> classesPerDay = new HashMap<>();
        
        for (AssignmentDetails assignment : solution.getAssignedClasses().values()) {
            classesPerDay.put(assignment.getDate(), 
                classesPerDay.getOrDefault(assignment.getDate(), 0) + 1);
        }
        
        if (classesPerDay.size() < 2) return false;
        
        LocalDate maxDay = classesPerDay.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        LocalDate minDay = classesPerDay.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (maxDay == null || minDay == null || 
            classesPerDay.get(maxDay) - classesPerDay.get(minDay) <= 1) {
            return false;
        }
        
        // Find courses on max day that can be moved to min day
        List<String> movableCourses = solution.getCourseTimeSlotAssignments().entrySet().stream()
            .filter(entry -> {
                TimeSlot ts = findTimeSlot(entry.getValue(), data.getAvailableTimeSlots());
                return ts != null && ts.getDate().equals(maxDay);
            })
            .map(Map.Entry::getKey)
            .filter(courseId -> canMoveToDate(courseId, minDay, solution, data))
            .collect(Collectors.toList());
        
        if (movableCourses.isEmpty()) return false;
        
        String courseToMove = movableCourses.get(random.nextInt(movableCourses.size()));
        
        // Find best time slot on min day
        List<TimeSlot> slotsOnMinDay = data.getAvailableTimeSlots().stream()
            .filter(ts -> ts.getDate().equals(minDay))
            .filter(ts -> isValidMoveTarget(courseToMove, ts, solution, data))
            .collect(Collectors.toList());
        
        if (!slotsOnMinDay.isEmpty()) {
            TimeSlot targetSlot = slotsOnMinDay.get(random.nextInt(slotsOnMinDay.size()));
            return moveCourseToTimeSlot(courseToMove, targetSlot, solution, data);
        }
        
        return false;
    }

    /**
     * Move 5: Session-level balancing within days
     */
    private boolean sessionLevelBalancing(ExamTimetableSolution solution, TimetablingData data, Random random) {
        System.out.println("Starting session-level balancing...");
        // Group by date and session
        Map<LocalDate, Map<UUID, List<String>>> dateSessionCourses = new HashMap<>();
        
        for (Map.Entry<String, UUID> entry : solution.getCourseTimeSlotAssignments().entrySet()) {
            String courseId = entry.getKey();
            TimeSlot timeSlot = findTimeSlot(entry.getValue(), data.getAvailableTimeSlots());
            
            if (timeSlot != null) {
                dateSessionCourses.computeIfAbsent(timeSlot.getDate(), k -> new HashMap<>())
                    .computeIfAbsent(timeSlot.getSessionId(), k -> new ArrayList<>())
                    .add(courseId);
            }
        }
        
        // Find day with unbalanced sessions
        for (Map.Entry<LocalDate, Map<UUID, List<String>>> dayEntry : dateSessionCourses.entrySet()) {
            Map<UUID, List<String>> sessionCourses = dayEntry.getValue();
            
            if (sessionCourses.size() < 2) continue;
            
            // Find max and min sessions
            UUID maxSession = sessionCourses.entrySet().stream()
                .max(Map.Entry.<UUID, List<String>>comparingByValue((l1, l2) -> Integer.compare(l1.size(), l2.size())))
                .map(Map.Entry::getKey)
                .orElse(null);
            
            UUID minSession = sessionCourses.entrySet().stream()
                .min(Map.Entry.<UUID, List<String>>comparingByValue((l1, l2) -> Integer.compare(l1.size(), l2.size())))
                .map(Map.Entry::getKey)
                .orElse(null);
            
            if (maxSession != null && minSession != null && 
                sessionCourses.get(maxSession).size() - sessionCourses.get(minSession).size() > 1) {
                
                // Move a course from max session to min session
                List<String> coursesToMove = sessionCourses.get(maxSession);
                String courseToMove = coursesToMove.get(random.nextInt(coursesToMove.size()));
                
                TimeSlot targetSlot = data.getAvailableTimeSlots().stream()
                    .filter(ts -> ts.getDate().equals(dayEntry.getKey()) && ts.getSessionId().equals(minSession))
                    .filter(ts -> isValidMoveTarget(courseToMove, ts, solution, data))
                    .findFirst()
                    .orElse(null);
                
                if (targetSlot != null) {
                    return moveCourseToTimeSlot(courseToMove, targetSlot, solution, data);
                }
            }
        }
        
        return false;
    }

    /**
     * Move 6: Room reallocation for better balance and building consistency
     */
    private boolean roomReallocation(ExamTimetableSolution solution, TimetablingData data, Random random) {
        System.out.println("Starting room reallocation...");
        // Find time slot with multiple classes
        List<UUID> timeSlots = solution.getTimeSlotClassAssignments().entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        if (timeSlots.isEmpty()) return false;
        
        UUID selectedTimeSlot = timeSlots.get(random.nextInt(timeSlots.size()));
        List<UUID> classesInSlot = solution.getTimeSlotClassAssignments().get(selectedTimeSlot);
        
        if (classesInSlot.size() < 2) return false;
        
        // Try swapping rooms between two classes
        UUID class1 = classesInSlot.get(random.nextInt(classesInSlot.size()));
        UUID class2 = classesInSlot.get(random.nextInt(classesInSlot.size()));
        
        if (class1.equals(class2)) return false;
        
        AssignmentDetails details1 = solution.getAssignedClasses().get(class1);
        AssignmentDetails details2 = solution.getAssignedClasses().get(class2);
        
        // Perform swap
        String tempRoom = details1.getRoomId();
        details1.setRoomId(details2.getRoomId());
        details2.setRoomId(tempRoom);
        return true;
    }

    // Helper methods
    private Map<Integer, List<LocalDate>> analyzeGroupExamDates(ExamTimetableSolution solution, TimetablingData data) {
        Map<Integer, List<LocalDate>> groupExamDates = new HashMap<>();
        
        for (Map.Entry<String, UUID> entry : solution.getCourseTimeSlotAssignments().entrySet()) {
            String courseId = entry.getKey();
            TimeSlot timeSlot = findTimeSlot(entry.getValue(), data.getAvailableTimeSlots());
            
            if (timeSlot != null) {
                Integer groupId = findGroupForCourse(courseId, data);
                if (groupId != null) {
                    groupExamDates.computeIfAbsent(groupId, k -> new ArrayList<>()).add(timeSlot.getDate());
                }
            }
        }
        
        return groupExamDates;
    }

    private boolean belongsToGroup(String courseId, Integer groupId, TimetablingData data) {
        return data.getClassesByCourseId().getOrDefault(courseId, Collections.emptyList()).stream()
            .anyMatch(examClass -> Objects.equals(examClass.getExamClassGroupId(), groupId));
    }

    private Integer findGroupForCourse(String courseId, TimetablingData data) {
        return data.getClassesByCourseId().getOrDefault(courseId, Collections.emptyList()).stream()
            .map(ExamClass::getExamClassGroupId)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private List<String> getCoursesOnDate(LocalDate date, Integer groupId, ExamTimetableSolution solution, TimetablingData data) {
        return solution.getCourseTimeSlotAssignments().entrySet().stream()
            .filter(entry -> {
                TimeSlot ts = findTimeSlot(entry.getValue(), data.getAvailableTimeSlots());
                return ts != null && ts.getDate().equals(date) && belongsToGroup(entry.getKey(), groupId, data);
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private int countClassesOnDate(LocalDate date, ExamTimetableSolution solution, TimetablingData data) {
        return (int) solution.getAssignedClasses().values().stream()
            .filter(assignment -> assignment.getDate().equals(date))
            .count();
    }

    private boolean isValidMoveTarget(String courseId, TimeSlot targetSlot, ExamTimetableSolution solution, TimetablingData data) {
        // Check hard constraints: conflicts, prohibited slots, etc.
        Map<String, Set<String>> courseConflicts = buildCourseConflictGraph(data);
        Set<String> conflicts = courseConflicts.getOrDefault(courseId, Collections.emptySet());
        
        // Check if any conflicting course is already at this time slot
        for (String conflictCourse : conflicts) {
            UUID conflictTimeSlotId = solution.getCourseTimeSlotAssignments().get(conflictCourse);
            if (conflictTimeSlotId != null && conflictTimeSlotId.equals(targetSlot.getId())) {
                return false;
            }
        }
        
        // Check prohibited slots
        for (TimeSlotRoomPair prohibited : data.getProhibitedSlots()) {
            if (prohibited.getSessionId().equals(targetSlot.getSessionId()) && 
                prohibited.getDate().equals(targetSlot.getDate())) {
                return false;
            }
        }
        
        return true;
    }

    private boolean canMoveToDate(String courseId, LocalDate targetDate, ExamTimetableSolution solution, TimetablingData data) {
        Integer courseGroup = findGroupForCourse(courseId, data);
        if (courseGroup == null) return true;
        
        // Check if any other course from the same group is on the target date
        return solution.getCourseTimeSlotAssignments().entrySet().stream()
            .filter(entry -> belongsToGroup(entry.getKey(), courseGroup, data))
            .noneMatch(entry -> {
                TimeSlot ts = findTimeSlot(entry.getValue(), data.getAvailableTimeSlots());
                return ts != null && ts.getDate().equals(targetDate);
            });
    }

    private boolean moveCourseToTimeSlot(String courseId, TimeSlot targetSlot, ExamTimetableSolution solution, TimetablingData data) {
        UUID oldTimeSlotId = solution.getCourseTimeSlotAssignments().get(courseId);
        
        // Update course assignment
        solution.getCourseTimeSlotAssignments().put(courseId, targetSlot.getId());
        
        // Update all classes of this course
        for (Map.Entry<UUID, AssignmentDetails> entry : solution.getAssignedClasses().entrySet()) {
            UUID classId = entry.getKey();
            
            // Check if this class belongs to the course
            boolean belongsToCourse = data.getClassesByCourseId().getOrDefault(courseId, Collections.emptyList())
                .stream().anyMatch(examClass -> examClass.getId().equals(classId));
            
            if (belongsToCourse) {
                AssignmentDetails details = entry.getValue();
                details.setTimeSlotId(targetSlot.getId());
                details.setSessionId(targetSlot.getSessionId());
                details.setDate(targetSlot.getDate());
            }
        }
        
        // Update time slot class assignments
        if (oldTimeSlotId != null) {
            List<UUID> oldSlotClasses = solution.getTimeSlotClassAssignments().getOrDefault(oldTimeSlotId, new ArrayList<>());
            oldSlotClasses.removeIf(classId -> {
                return data.getClassesByCourseId().getOrDefault(courseId, Collections.emptyList())
                    .stream().anyMatch(examClass -> examClass.getId().equals(classId));
            });
            solution.getTimeSlotClassAssignments().put(oldTimeSlotId, oldSlotClasses);
        }
        
        List<UUID> newSlotClasses = solution.getTimeSlotClassAssignments().getOrDefault(targetSlot.getId(), new ArrayList<>());
        data.getClassesByCourseId().getOrDefault(courseId, Collections.emptyList()).forEach(examClass -> {
            if (!newSlotClasses.contains(examClass.getId())) {
                newSlotClasses.add(examClass.getId());
            }
        });
        solution.getTimeSlotClassAssignments().put(targetSlot.getId(), newSlotClasses);
        
        return true;
    }

    private double calculateDistributionScore(List<LocalDate> dates) {
        if (dates.size() <= 1) return 1.0;
        
        // Calculate variance of gaps between consecutive dates
        List<Long> gaps = new ArrayList<>();
        for (int i = 0; i < dates.size() - 1; i++) {
            gaps.add(ChronoUnit.DAYS.between(dates.get(i), dates.get(i + 1)));
        }
        
        double mean = gaps.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double variance = gaps.stream()
            .mapToDouble(gap -> Math.pow(gap - mean, 2))
            .average()
            .orElse(0.0);
        
        // Lower variance = better distribution
        return Math.exp(-variance / (mean + 1));
    }

    /**
     * Check if swapping time slots between two courses creates conflicts
     */
    private boolean isValidSwap(String course1, String course2, UUID timeSlot1, UUID timeSlot2, 
                                ExamTimetableSolution solution, TimetablingData data) {
        // Build course conflict graph
        Map<String, Set<String>> courseConflicts = buildCourseConflictGraph(data);
        
        // Get conflicts for each course
        Set<String> course1Conflicts = courseConflicts.getOrDefault(course1, Collections.emptySet());
        Set<String> course2Conflicts = courseConflicts.getOrDefault(course2, Collections.emptySet());
        
        // Check if course1 would conflict with any course at timeSlot2
        for (Map.Entry<String, UUID> entry : solution.getCourseTimeSlotAssignments().entrySet()) {
            String otherCourse = entry.getKey();
            UUID otherTimeSlot = entry.getValue();
            
            if (otherCourse.equals(course1) || otherCourse.equals(course2)) continue;
            
            if (otherTimeSlot.equals(timeSlot2) && course1Conflicts.contains(otherCourse)) {
                return false; // Conflict found
            }
            
            if (otherTimeSlot.equals(timeSlot1) && course2Conflicts.contains(otherCourse)) {
                return false; // Conflict found
            }
        }
        
        return true;
    }
    
    /**
     * Create a deep copy of a solution
     */
    private ExamTimetableSolution deepCopy(ExamTimetableSolution solution) {
        ExamTimetableSolution copy = new ExamTimetableSolution();
        
        // Copy assigned classes
        Map<UUID, AssignmentDetails> assignedClasses = new HashMap<>();
        for (Map.Entry<UUID, AssignmentDetails> entry : solution.getAssignedClasses().entrySet()) {
            AssignmentDetails details = entry.getValue();
            AssignmentDetails detailsCopy = new AssignmentDetails();
            
            detailsCopy.setTimeSlotId(details.getTimeSlotId());
            detailsCopy.setRoomId(details.getRoomId());
            detailsCopy.setSessionId(details.getSessionId());
            detailsCopy.setDate(details.getDate());
            
            assignedClasses.put(entry.getKey(), detailsCopy);
        }
        copy.setAssignedClasses(assignedClasses);
        
        copy.setCourseTimeSlotAssignments(new HashMap<>(solution.getCourseTimeSlotAssignments()));
        
        Map<UUID, List<UUID>> timeSlotClassAssignments = new HashMap<>();
        for (Map.Entry<UUID, List<UUID>> entry : solution.getTimeSlotClassAssignments().entrySet()) {
            timeSlotClassAssignments.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        copy.setTimeSlotClassAssignments(timeSlotClassAssignments);
        
        copy.setRoomUsageCounts(new HashMap<>(solution.getRoomUsageCounts()));
        
        copy.setTimeSlotUsageCounts(new HashMap<>(solution.getTimeSlotUsageCounts()));
        
        copy.setGroupSpacingViolations(solution.getGroupSpacingViolations());
        copy.setRoomBalanceMetric(solution.getRoomBalanceMetric());
        copy.setTimeSlotBalanceMetric(solution.getTimeSlotBalanceMetric());
        copy.setEarlySlotAssignments(solution.getEarlySlotAssignments());
        copy.setQualityScore(solution.getQualityScore());
        
        return copy;
    }
}

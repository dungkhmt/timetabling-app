package openerp.openerpresourceserver.examtimetabling.algorithm;

import openerp.openerpresourceserver.examtimetabling.entity.*;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.*;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamRoom;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
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

        return solution;
        
        // Phase 2: Improve solution using local search (Temporary discarded for now)
        // ExamTimetableSolution optimizedSolution = optimizeSolution(solution, data);
        
        // System.out.println("Optimization complete. Final solution quality: " + optimizedSolution.getQualityScore());
        // System.out.println("Total assigned classes: " + solution.getAssignedClasses().size() + "/" + data.getExamClasses().size());
        

        // return optimizedSolution;
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
            updateProhibitedSlots(prohibitedSlots, iterationSolution, filteredData.getAvailableTimeSlots());
            
            System.out.println("Newly assigned classes in this iteration: " + newlyAssignedClasses);
            
        } while (newlyAssignedClasses > 0 && assignedClassIds.size() < totalClasses && retryCount < maxRetries);
        
        // Calculate metrics for the final solution
        solution.calculateMetrics(data);
        
        return solution;
    }

    /**
     * Update prohibited slots based on new assignments
     */
    private void updateProhibitedSlots(Set<TimeSlotRoomPair> prohibitedSlots, 
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

        // Map to track courses in the same group
        Map<String, Set<String>> coursesByGroup = buildCourseGroupMap(data);
        
        // Track which groups have already had courses assigned
        Map<Integer, Boolean> groupHasAssignedCourses = new HashMap<>();
        
        // Track earliest and latest assigned dates for each group
        Map<Integer, LocalDate> groupEarliestDate = new HashMap<>();
        Map<Integer, LocalDate> groupLatestDate = new HashMap<>();

        int totalCourses = sortedCourseGroups.size();
        System.out.printf("Total courses to assign: %d%n", totalCourses);
        int assignedCourses = 0;

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
                            // // Day before or day after
                            // else if (otherSlot.isDayBefore(conflictTimeSlot) || otherSlot.isDayAfter(conflictTimeSlot)) {
                            //     preferredToAvoidTimeSlots.add(otherSlot.getId());
                            // }
                        }
                    }
                }
            }
            
            // Add prohibited time slots to invalid set (these are hard constraints)
            // for (TimeSlotRoomPair prohibited : data.getProhibitedSlots()) {
            //     for (TimeSlot timeSlot : data.getAvailableTimeSlots()) {
            //         if (timeSlot.getSessionId().equals(prohibited.getSessionId()) &&
            //             timeSlot.getDate().equals(prohibited.getDate())) {
            //             invalidTimeSlots.add(timeSlot.getId());
            //         }
            //     }
            // }
            
            UUID assignedTimeSlot = null;
            
            // Filter to only valid time slots (must satisfy hard constraints)
            List<TimeSlot> validTimeSlots = data.getAvailableTimeSlots().stream()
                .filter(ts -> !invalidTimeSlots.contains(ts.getId()))
                .collect(Collectors.toList());
            
            if (validTimeSlots.isEmpty()) {
                System.out.printf("No valid time slots available for course %s. Skipping.", courseId);
                continue;
            }
            
            if (courseGroup != null) {
                System.out.printf("Course %s belongs to group %s%n", courseId, courseGroup);
                boolean isFirstCourseInGroup = !groupHasAssignedCourses.getOrDefault(courseGroup, false);
                
                if (isFirstCourseInGroup) {
                    // This is the first course in this group to be assigned
                    
                    // First, try to find slots that also satisfy soft constraints

                    System.out.printf("Number of preferred to avoid time slots: %d\n" ,preferredToAvoidTimeSlots.size());
                    List<TimeSlot> bestTimeSlots = validTimeSlots.stream()
                        .filter(ts -> !preferredToAvoidTimeSlots.contains(ts.getId()))
                        .collect(Collectors.toList());
                    
                    if (!bestTimeSlots.isEmpty()) {
                        // We have slots that satisfy both hard and soft constraints
                        validTimeSlots = bestTimeSlots;
                    } else {
                        System.out.printf("Could not find time slot for course %s that satisfies soft constraints. Using one that violates them.", courseId);
                    }
                    
                    // Choose either the earliest or latest date
                    // Sort by date (ascending)
                    validTimeSlots.sort(Comparator.comparing(TimeSlot::getDate));
                    
                    // Decide whether to use earliest or latest date
                    Random random = new Random();
                    boolean useEarliestDate = random.nextBoolean();
                    
                    if (useEarliestDate && !validTimeSlots.isEmpty()) {
                        // Group time slots by date
                        Map<LocalDate, List<TimeSlot>> slotsByDate = validTimeSlots.stream()
                            .collect(Collectors.groupingBy(TimeSlot::getDate));
                        
                        // Get earliest date
                        LocalDate earliestDate = validTimeSlots.get(0).getDate();
                        
                        // Choose the best time slot from this date
                        List<TimeSlot> slotsOnEarliestDate = slotsByDate.get(earliestDate);
                        slotsOnEarliestDate.sort(createTimeSlotComparator(data, timeSlotToCourses));
                        
                        assignedTimeSlot = slotsOnEarliestDate.get(0).getId();
                        
                        // Record this as the earliest date for this group
                        groupEarliestDate.put(courseGroup, earliestDate);
                        groupLatestDate.put(courseGroup, earliestDate);
                    } else if (!validTimeSlots.isEmpty()) {
                        // Use latest date
                        Map<LocalDate, List<TimeSlot>> slotsByDate = validTimeSlots.stream()
                            .collect(Collectors.groupingBy(TimeSlot::getDate));
                        
                        // Get latest date
                        LocalDate latestDate = validTimeSlots.get(validTimeSlots.size() - 1).getDate();
                        
                        // Choose the best time slot from this date
                        List<TimeSlot> slotsOnLatestDate = slotsByDate.get(latestDate);
                        slotsOnLatestDate.sort(createTimeSlotComparator(data, timeSlotToCourses));
                        
                        assignedTimeSlot = slotsOnLatestDate.get(0).getId();
                        
                        // Record this as the earliest and latest date for this group
                        groupEarliestDate.put(courseGroup, latestDate);
                        groupLatestDate.put(courseGroup, latestDate);
                    }
                    
                    // Mark that this group now has an assigned course
                    groupHasAssignedCourses.put(courseGroup, true);
                } else {
                    // This is NOT the first course in this group
                    
                    // First, try to find slots that also satisfy soft constraints
                    List<TimeSlot> bestTimeSlots = validTimeSlots.stream()
                        .filter(ts -> !preferredToAvoidTimeSlots.contains(ts.getId()))
                        .collect(Collectors.toList());
                    
                    if (!bestTimeSlots.isEmpty()) {
                        // We have slots that satisfy both hard and soft constraints
                        validTimeSlots = bestTimeSlots;
                    } else {
                        log.info("Could not find time slot for course {} that satisfies soft constraints. Using one that violates them.", courseId);
                    }
                    
                    // Try to schedule it as far away as possible from existing courses
                    LocalDate currentEarliestDate = groupEarliestDate.get(courseGroup);
                    LocalDate currentLatestDate = groupLatestDate.get(courseGroup);
                    
                    // First, try scheduling before the earliest date
                    List<TimeSlot> slotBeforeEarliest = validTimeSlots.stream()
                        .filter(ts -> ts.getDate().isBefore(currentEarliestDate))
                        .sorted(Comparator.comparing(TimeSlot::getDate).reversed()) // Sort by date descending
                        .collect(Collectors.toList());
                    
                    // Second, try scheduling after the latest date
                    List<TimeSlot> slotsAfterLatest = validTimeSlots.stream()
                        .filter(ts -> ts.getDate().isAfter(currentLatestDate))
                        .sorted(Comparator.comparing(TimeSlot::getDate)) // Sort by date ascending
                        .collect(Collectors.toList());
                    
                    // Decide which option to use (before earliest or after latest)
                    TimeSlot selectedSlot = null;
                    
                    if (!slotBeforeEarliest.isEmpty() && !slotsAfterLatest.isEmpty()) {
                        // Both options are available, choose the one with more days in between
                        long daysBeforeEarliest = ChronoUnit.DAYS.between(
                            slotBeforeEarliest.get(0).getDate(), currentEarliestDate);
                        long daysAfterLatest = ChronoUnit.DAYS.between(
                            currentLatestDate, slotsAfterLatest.get(0).getDate());
                        
                        if (daysBeforeEarliest >= daysAfterLatest) {
                            selectedSlot = slotBeforeEarliest.get(0);
                            groupEarliestDate.put(courseGroup, selectedSlot.getDate());
                        } else {
                            selectedSlot = slotsAfterLatest.get(0);
                            groupLatestDate.put(courseGroup, selectedSlot.getDate());
                        }
                    } else if (!slotBeforeEarliest.isEmpty()) {
                        // Only before-earliest is available
                        selectedSlot = slotBeforeEarliest.get(0);
                        groupEarliestDate.put(courseGroup, selectedSlot.getDate());
                    } else if (!slotsAfterLatest.isEmpty()) {
                        // Only after-latest is available
                        selectedSlot = slotsAfterLatest.get(0);
                        groupLatestDate.put(courseGroup, selectedSlot.getDate());
                    } else {
                        // Neither option is available, just use the best time slot by standard criteria
                        validTimeSlots.sort(createTimeSlotComparator(data, timeSlotToCourses));
                        if (!validTimeSlots.isEmpty()) {
                            selectedSlot = validTimeSlots.get(0);
                            
                            // Update group date tracking
                            if (selectedSlot.getDate().isBefore(currentEarliestDate)) {
                                groupEarliestDate.put(courseGroup, selectedSlot.getDate());
                            } else if (selectedSlot.getDate().isAfter(currentLatestDate)) {
                                groupLatestDate.put(courseGroup, selectedSlot.getDate());
                            }
                        }
                    }
                    
                    if (selectedSlot != null) {
                        assignedTimeSlot = selectedSlot.getId();
                    }
                }
            } else {
                System.out.printf("Course %s is not part of a group. Assigning time slot without group constraints.%n", courseId);
                // This course is not part of a group, still apply soft constraints first
                // First, try to find slots that also satisfy soft constraints
                List<TimeSlot> bestTimeSlots = validTimeSlots.stream()
                    .filter(ts -> !preferredToAvoidTimeSlots.contains(ts.getId()))
                    .collect(Collectors.toList());
                
                if (!bestTimeSlots.isEmpty()) {
                    // We have slots that satisfy both hard and soft constraints
                    validTimeSlots = bestTimeSlots;
                }
                
                // Just use standard time slot selection
                validTimeSlots.sort(createTimeSlotComparator(data, timeSlotToCourses));
                if (!validTimeSlots.isEmpty()) {
                    assignedTimeSlot = validTimeSlots.get(0).getId();
                }
            }
            
            if (assignedTimeSlot != null) {
                courseToTimeSlot.put(courseId, assignedTimeSlot);
                
                timeSlotToCourses.computeIfAbsent(assignedTimeSlot, k -> new HashSet<>())
                    .add(courseId);
                
                assignedCourses++;
            } else {
                log.warn("Could not find suitable time slot for course {}. Skipping.", courseId);
            }
        }

        System.out.printf("Time slot assignment completed: {}/{} courses assigned successfully", 
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
            // Sort by preference (early slots last)
            boolean ts1Early = data.getEarlyTimeSlots().contains(ts1.getId());
            boolean ts2Early = data.getEarlyTimeSlots().contains(ts2.getId());
            
            if (ts1Early && !ts2Early) return 1;
            if (!ts1Early && ts2Early) return -1;
            
            // Otherwise sort by usage count (least used first)
            int ts1Usage = timeSlotToCourses.getOrDefault(ts1.getId(), Collections.emptySet()).size();
            int ts2Usage = timeSlotToCourses.getOrDefault(ts2.getId(), Collections.emptySet()).size();
            return Integer.compare(ts1Usage, ts2Usage);
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
     * Optimize solution using Simulated Annealing
     */
    private ExamTimetableSolution optimizeSolution(ExamTimetableSolution initialSolution, TimetablingData data) {
        ExamTimetableSolution currentSolution = deepCopy(initialSolution);
        currentSolution.calculateMetrics(data);

        double currentScore = currentSolution.getQualityScore();
        ExamTimetableSolution bestSolution = deepCopy(currentSolution);
        double bestScore = currentScore;

        Random random = new Random();
        int iterations = 0;

        double temperature = 1.0;         // Initial temperature
        double coolingRate = 0.995;       // Cooling factor (between 0 and 1)
        double minTemperature = 1e-4;     // Stop when temperature is low

        while (iterations < MAX_ITERATIONS && temperature > minTemperature) {
            iterations++;
            
            // Generate a neighbor solution
            ExamTimetableSolution neighborSolution = applyRandomMove(currentSolution, data, random);
            neighborSolution.calculateMetrics(data);
            
            double neighborScore = neighborSolution.getQualityScore();
            double delta = neighborScore - currentScore;
            
            if (delta > 0 || Math.exp(delta / temperature) > random.nextDouble()) {
                // Accept neighbor (better or with acceptance probability)
                currentSolution = neighborSolution;
                currentScore = neighborScore;
                
                if (neighborScore > bestScore) {
                    bestSolution = deepCopy(neighborSolution);
                    bestScore = neighborScore;
                    System.err.println("---------------------------------");
                    System.err.println("Iteration: " + iterations + ", Temperature: " + temperature + ", Best Score: " + bestScore);
                    System.err.println("New best solution found. Score: " + bestScore);
                }
            }

            // Cool down
            temperature *= coolingRate;
        }

        System.err.println("Simulated Annealing completed after " + iterations + " iterations. Final best score: " + bestScore);
        return bestSolution;
    }

    
    /**
     * Apply a random move to generate a neighbor solution
     */
    private ExamTimetableSolution applyRandomMove(ExamTimetableSolution solution, TimetablingData data, Random random) {
        ExamTimetableSolution neighbor = deepCopy(solution);
        
        // Choose a random move type
        int moveType = random.nextInt(3);
        
        switch (moveType) {
            case 0:
                swapCourseTimeSlots(neighbor, data, random);
                break;
            case 1:
                reassignRooms(neighbor, data, random);
                break;
            case 2:
                moveCourseToNewTimeSlot(neighbor, data, random);
                break;
        }
        
        return neighbor;
    }
    
    /**
     * Swap time slots between two courses
     */
    private void swapCourseTimeSlots(ExamTimetableSolution solution, TimetablingData data, Random random) {
        List<String> courseIds = new ArrayList<>(solution.getCourseTimeSlotAssignments().keySet());
        
        if (courseIds.size() < 2) return;
        
        // Choose two random courses
        String course1 = courseIds.get(random.nextInt(courseIds.size()));
        String course2;
        do {
            course2 = courseIds.get(random.nextInt(courseIds.size()));
        } while (course1.equals(course2));
        
        // Get their time slots
        UUID timeSlot1 = solution.getCourseTimeSlotAssignments().get(course1);
        UUID timeSlot2 = solution.getCourseTimeSlotAssignments().get(course2);
        
        // Check if swap is valid (no new conflicts)
        if (!isValidSwap(course1, course2, timeSlot1, timeSlot2, solution, data)) {
            return;
        }
        
        // Find all classes for each course
        List<UUID> classesForCourse1 = new ArrayList<>();
        List<UUID> classesForCourse2 = new ArrayList<>();
        
        for (Map.Entry<UUID, AssignmentDetails> entry : solution.getAssignedClasses().entrySet()) {
            UUID classId = entry.getKey();
            
            // Find course ID for this class
            String courseId = null;
            for (ExamClass ec : data.getExamClasses()) {
                if (ec.getId().equals(classId)) {
                    courseId = ec.getCourseId();
                    break;
                }
            }
            
            if (course1.equals(courseId)) {
                classesForCourse1.add(classId);
            } else if (course2.equals(courseId)) {
                classesForCourse2.add(classId);
            }
        }
        
        // Update time slot assignments
        solution.getCourseTimeSlotAssignments().put(course1, timeSlot2);
        solution.getCourseTimeSlotAssignments().put(course2, timeSlot1);
        
        // Update class assignments
        for (UUID classId : classesForCourse1) {
            AssignmentDetails details = solution.getAssignedClasses().get(classId);
            details.setTimeSlotId(timeSlot2);
            
            for (TimeSlot ts : data.getAvailableTimeSlots()) {
                if (ts.getId().equals(timeSlot2)) {
                    details.setSessionId(ts.getSessionId());
                    details.setDate(ts.getDate());
                    break;
                }
            }
        }
        
        for (UUID classId : classesForCourse2) {
            AssignmentDetails details = solution.getAssignedClasses().get(classId);
            details.setTimeSlotId(timeSlot1);
            
            for (TimeSlot ts : data.getAvailableTimeSlots()) {
                if (ts.getId().equals(timeSlot1)) {
                    details.setSessionId(ts.getSessionId());
                    details.setDate(ts.getDate());
                    break;
                }
            }
        }
        
        // Update time slot class assignments
        solution.getTimeSlotClassAssignments().put(timeSlot1, 
            solution.getTimeSlotClassAssignments().getOrDefault(timeSlot1, new ArrayList<>()));
        solution.getTimeSlotClassAssignments().put(timeSlot2, 
            solution.getTimeSlotClassAssignments().getOrDefault(timeSlot2, new ArrayList<>()));
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
     * Reassign rooms within a time slot
     */
    private void reassignRooms(ExamTimetableSolution solution, TimetablingData data, Random random) {
        // Get a random time slot
        List<UUID> timeSlots = new ArrayList<>(solution.getTimeSlotClassAssignments().keySet());
        if (timeSlots.isEmpty()) return;
        
        UUID timeSlotId = timeSlots.get(random.nextInt(timeSlots.size()));
        List<UUID> classesInTimeSlot = solution.getTimeSlotClassAssignments().get(timeSlotId);
        
        if (classesInTimeSlot == null || classesInTimeSlot.size() < 2) return;
        
        // Choose two random classes in this time slot
        UUID class1 = classesInTimeSlot.get(random.nextInt(classesInTimeSlot.size()));
        UUID class2;
        do {
            class2 = classesInTimeSlot.get(random.nextInt(classesInTimeSlot.size()));
        } while (class1.equals(class2));
        
        // Get room assignments
        AssignmentDetails details1 = solution.getAssignedClasses().get(class1);
        AssignmentDetails details2 = solution.getAssignedClasses().get(class2);
        
        String room1 = details1.getRoomId();
        String room2 = details2.getRoomId();
        
        // Check if rooms are suitable (capacity)
        ExamClass examClass1 = null;
        ExamClass examClass2 = null;
        
        for (ExamClass ec : data.getExamClasses()) {
            if (ec.getId().equals(class1)) examClass1 = ec;
            if (ec.getId().equals(class2)) examClass2 = ec;
            if (examClass1 != null && examClass2 != null) break;
        }
        
        if (examClass1 == null || examClass2 == null) return;
        
        ExamRoom examRoom1 = null;
        ExamRoom examRoom2 = null;
        
        for (ExamRoom room : data.getAvailableRooms()) {
            if (room.getId().equals(room1)) examRoom1 = room;
            if (room.getId().equals(room2)) examRoom2 = room;
            if (examRoom1 != null && examRoom2 != null) break;
        }
        
        if (examRoom1 == null || examRoom2 == null) return;
        
        // Check capacity constraints
        int requiredCapacity1 = examClass1.getNumberOfStudents() * 2;
        int requiredCapacity2 = examClass2.getNumberOfStudents() * 2;
        
        if (examRoom2.getNumberSeat() < requiredCapacity1 || 
            examRoom1.getNumberSeat() < requiredCapacity2) {
            return; // Rooms not suitable
        }
        
        // Swap rooms
        details1.setRoomId(room2);
        details2.setRoomId(room1);
        
        // Update room usage counts
        Map<String, Integer> roomUsage = solution.getRoomUsageCounts();
        roomUsage.put(room1, roomUsage.getOrDefault(room1, 0));
        roomUsage.put(room2, roomUsage.getOrDefault(room2, 0));
    }
    
    /**
     * Move a course to a new time slot
     */
    private void moveCourseToNewTimeSlot(ExamTimetableSolution solution, TimetablingData data, Random random) {
        // Choose a random course
        List<String> courseIds = new ArrayList<>(solution.getCourseTimeSlotAssignments().keySet());
        if (courseIds.isEmpty()) return;
        
        String courseId = courseIds.get(random.nextInt(courseIds.size()));
        UUID currentTimeSlotId = solution.getCourseTimeSlotAssignments().get(courseId);
        
        // Find a valid new time slot
        List<TimeSlot> validTimeSlots = new ArrayList<>();
        
        // Build course conflict graph
        Map<String, Set<String>> courseConflicts = buildCourseConflictGraph(data);
        Set<String> conflicts = courseConflicts.getOrDefault(courseId, Collections.emptySet());
        
        // Get time slots used by conflicting courses
        Set<UUID> conflictingTimeSlots = new HashSet<>();
        for (String conflictCourse : conflicts) {
            UUID timeSlot = solution.getCourseTimeSlotAssignments().get(conflictCourse);
            if (timeSlot != null) {
                conflictingTimeSlots.add(timeSlot);
                
                // add consecutive time slots on same day
                for (TimeSlot ts1 : data.getAvailableTimeSlots()) {
                    if (ts1.getId().equals(timeSlot)) {
                        for (TimeSlot ts2 : data.getAvailableTimeSlots()) {
                            if (ts1.isSameDayAs(ts2) && ts1.isConsecutiveWith(ts2)) {
                                conflictingTimeSlots.add(ts2.getId());
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        // Find valid time slots
        for (TimeSlot timeSlot : data.getAvailableTimeSlots()) {
            if (timeSlot.getId().equals(currentTimeSlotId)) continue;
            if (conflictingTimeSlots.contains(timeSlot.getId())) continue;
            
            // Check if this time slot is prohibited
            boolean prohibited = false;
            for (TimeSlotRoomPair pair : data.getProhibitedSlots()) {
                if (pair.getSessionId().equals(timeSlot.getSessionId()) && 
                    pair.getDate().equals(timeSlot.getDate())) {
                    prohibited = true;
                    break;
                }
            }
            
            if (!prohibited) {
                validTimeSlots.add(timeSlot);
            }
        }
        
        if (validTimeSlots.isEmpty()) return;
        
        // Choose a random valid time slot with preference for non-early slots
        TimeSlot newTimeSlot;
        List<TimeSlot> nonEarlySlots = validTimeSlots.stream()
            .filter(ts -> !data.getEarlyTimeSlots().contains(ts.getId()))
            .collect(Collectors.toList());
        
        if (!nonEarlySlots.isEmpty()) {
            newTimeSlot = nonEarlySlots.get(random.nextInt(nonEarlySlots.size()));
        } else {
            newTimeSlot = validTimeSlots.get(random.nextInt(validTimeSlots.size()));
        }
        
        // Move the course to the new time slot
        solution.getCourseTimeSlotAssignments().put(courseId, newTimeSlot.getId());
        
        // Update all classes of this course
        List<UUID> classIdsToUpdate = new ArrayList<>();
        for (Map.Entry<UUID, AssignmentDetails> entry : solution.getAssignedClasses().entrySet()) {
            UUID classId = entry.getKey();
            
            for (ExamClass ec : data.getExamClasses()) {
                if (ec.getId().equals(classId) && ec.getCourseId().equals(courseId)) {
                    classIdsToUpdate.add(classId);
                    break;
                }
            }
        }
        
        // Update class assignments
        for (UUID classId : classIdsToUpdate) {
            AssignmentDetails details = solution.getAssignedClasses().get(classId);
            details.setTimeSlotId(newTimeSlot.getId());
            details.setSessionId(newTimeSlot.getSessionId());
            details.setDate(newTimeSlot.getDate());
        }
        
        // Update time slot class assignments
        List<UUID> oldSlotClasses = solution.getTimeSlotClassAssignments().getOrDefault(currentTimeSlotId, new ArrayList<>());
        oldSlotClasses.removeAll(classIdsToUpdate);
        solution.getTimeSlotClassAssignments().put(currentTimeSlotId, oldSlotClasses);
        
        List<UUID> newSlotClasses = solution.getTimeSlotClassAssignments().getOrDefault(newTimeSlot.getId(), new ArrayList<>());
        newSlotClasses.addAll(classIdsToUpdate);
        solution.getTimeSlotClassAssignments().put(newTimeSlot.getId(), newSlotClasses);
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

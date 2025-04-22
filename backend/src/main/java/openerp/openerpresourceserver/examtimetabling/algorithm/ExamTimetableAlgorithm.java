package openerp.openerpresourceserver.examtimetabling.algorithm;

import openerp.openerpresourceserver.examtimetabling.entity.*;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.*;

import java.time.LocalDate;
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
    private static final int MAX_ITERATIONS = 100;
    
    /**
     * Main method to assign classes to rooms and time slots
     * 
     * @param data The preprocessed timetabling data
     * @return A solution with assignments
     */
    public TimetablingSolution assign(TimetablingData data) {
        log.info("Starting assignment algorithm for {} classes", data.getExamClasses().size());
        
        // Create initial solution with multiple attempts for unassigned classes
        TimetablingSolution solution = createInitialAssignmentWithRetries(data);
        
        if (solution.getAssignedClasses().size() < data.getExamClasses().size()) {
            log.warn("Could not assign all classes: {}/{} assigned", 
                    solution.getAssignedClasses().size(), data.getExamClasses().size());
        } else {
            log.info("All classes successfully assigned in initial solution");
        }
        
        // Phase 2: Improve solution using local search
        TimetablingSolution optimizedSolution = optimizeSolution(solution, data);
        
        log.info("Optimization complete. Final solution quality: {}", optimizedSolution.getQualityScore());
        
        return optimizedSolution;
    }

    /**
     * Create initial assignment with multiple retries for unassigned classes
     */
    private TimetablingSolution createInitialAssignmentWithRetries(TimetablingData data) {
        TimetablingSolution solution = new TimetablingSolution();
        
        // Set of class IDs that have been successfully assigned
        Set<UUID> assignedClassIds = new HashSet<>();
        // Total number of classes to assign
        int totalClasses = data.getExamClasses().size();
        // Number of classes assigned in the current iteration
        int newlyAssignedClasses;
        // Maximum number of retries
        int maxRetries = 10;
        // Current retry count
        int retryCount = 0;
        
        do {
            retryCount++;
            log.info("Initial assignment attempt #{}", retryCount);
            
            // Create filtered data for unassigned classes
            TimetablingData filteredData = filterUnassignedClasses(data, assignedClassIds);
            
            // Try to assign remaining classes
            TimetablingSolution iterationSolution = createInitialAssignment(filteredData);
            
            // Merge the new assignments with existing solution
            newlyAssignedClasses = mergeSolutions(solution, iterationSolution);
            
            // Update the set of assigned class IDs
            for (UUID classId : iterationSolution.getAssignedClasses().keySet()) {
                assignedClassIds.add(classId);
            }
            
            log.info("Assigned {} new classes in iteration #{}, total assigned: {}/{}", 
                    newlyAssignedClasses, retryCount, assignedClassIds.size(), totalClasses);
            
            // Continue if we made progress and still have unassigned classes
        } while (newlyAssignedClasses > 0 && assignedClassIds.size() < totalClasses && retryCount < maxRetries);
        
        // Calculate metrics for the final solution
        solution.calculateMetrics(data);
        
        return solution;
    }

    /**
     * Filter data to only include unassigned classes
     */
    private TimetablingData filterUnassignedClasses(TimetablingData originalData, Set<UUID> assignedClassIds) {
        TimetablingData filteredData = new TimetablingData();
        
        // Copy all basic data
        filteredData.setAvailableRooms(originalData.getAvailableRooms());
        filteredData.setAvailableTimeSlots(originalData.getAvailableTimeSlots());
        filteredData.setExamDates(originalData.getExamDates());
        filteredData.setProhibitedSlots(originalData.getProhibitedSlots());
        filteredData.setEarlyTimeSlots(originalData.getEarlyTimeSlots());
        filteredData.setExistingAssignments(originalData.getExistingAssignments());
        
        // Filter classes to only include unassigned ones
        List<ExamClass> unassignedClasses = originalData.getExamClasses().stream()
                .filter(ec -> !assignedClassIds.contains(ec.getId()))
                .collect(Collectors.toList());
        filteredData.setExamClasses(unassignedClasses);
        
        // Rebuild class groupings
        Map<String, List<ExamClass>> classesByCourseId = unassignedClasses.stream()
                .collect(Collectors.groupingBy(ExamClass::getCourseId));
        filteredData.setClassesByCourseId(classesByCourseId);
        
        Map<String, List<ExamClass>> classesByGroupId = unassignedClasses.stream()
                .filter(ec -> ec.getGroupId() != null && !ec.getGroupId().isEmpty())
                .collect(Collectors.groupingBy(ExamClass::getGroupId));
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
    private int mergeSolutions(TimetablingSolution existingSolution, TimetablingSolution newSolution) {
        int newlyAssignedCount = 0;
        
        // Merge assigned classes
        for (Map.Entry<UUID, AssignmentDetails> entry : newSolution.getAssignedClasses().entrySet()) {
            UUID classId = entry.getKey();
            AssignmentDetails details = entry.getValue();
            
            if (!existingSolution.getAssignedClasses().containsKey(classId)) {
                existingSolution.getAssignedClasses().put(classId, details);
                newlyAssignedCount++;
            }
        }
        
        // Merge course time slot assignments
        for (Map.Entry<String, UUID> entry : newSolution.getCourseTimeSlotAssignments().entrySet()) {
            String courseId = entry.getKey();
            UUID timeSlotId = entry.getValue();
            
            existingSolution.getCourseTimeSlotAssignments().put(courseId, timeSlotId);
        }
        
        // Merge time slot class assignments
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
        
        // Merge room usage counts
        for (Map.Entry<UUID, Integer> entry : newSolution.getRoomUsageCounts().entrySet()) {
            UUID roomId = entry.getKey();
            Integer count = entry.getValue();
            
            existingSolution.getRoomUsageCounts().put(roomId, 
                    existingSolution.getRoomUsageCounts().getOrDefault(roomId, 0) + count);
        }
        
        // Merge time slot usage counts
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
    private TimetablingSolution createInitialAssignment(TimetablingData data) {
        TimetablingSolution solution = new TimetablingSolution();
        
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
     * Sort course groups by number of constraints (most constrained first)
     */
    private List<Map.Entry<String, List<ExamClass>>> sortCourseGroupsByConstraints(
            Map<String, List<ExamClass>> courseGroups, TimetablingData data) {
        
        // Calculate how constrained each course is
        Map<String, Integer> courseConstraints = new HashMap<>();
        
        for (Map.Entry<String, List<ExamClass>> entry : courseGroups.entrySet()) {
            String courseId = entry.getKey();
            List<ExamClass> classes = entry.getValue();
            
            // Count number of conflicting courses
            Set<String> conflictingCourses = new HashSet<>();
            
            for (ExamClass examClass : classes) {
                Set<UUID> conflictingClasses = data.getConflictGraph().getOrDefault(examClass.getId(), Collections.emptySet());
                
                for (UUID conflictId : conflictingClasses) {
                    // Find the course ID of the conflicting class
                    for (ExamClass ec : data.getExamClasses()) {
                        if (ec.getId().equals(conflictId)) {
                            conflictingCourses.add(ec.getCourseId());
                            break;
                        }
                    }
                }
            }
            
            // The constraint score is the number of conflicting courses plus the number of students
            int totalStudents = classes.stream()
                .mapToInt(ExamClass::getNumberOfStudents)
                .sum();
            
            courseConstraints.put(courseId, conflictingCourses.size() * 100 + totalStudents);
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
     * trying to assign as many courses as possible
     */
    private Map<String, UUID> assignTimeSlotsToCourses(
        List<Map.Entry<String, List<ExamClass>>> sortedCourseGroups, TimetablingData data) {

        Map<String, UUID> courseToTimeSlot = new HashMap<>();
        Map<UUID, Set<String>> timeSlotToCourses = new HashMap<>();

        // Build course conflict graph (two courses conflict if any of their classes conflict)
        Map<String, Set<String>> courseConflictGraph = buildCourseConflictGraph(data);

        // Map to track courses in the same group
        Map<String, Set<String>> coursesByGroup = buildCourseGroupMap(data);

        int totalCourses = sortedCourseGroups.size();
        int assignedCourses = 0;

        // For each course, find a valid time slot
        for (Map.Entry<String, List<ExamClass>> courseEntry : sortedCourseGroups) {
            String courseId = courseEntry.getKey();
            List<ExamClass> classes = courseEntry.getValue();
            
            // Get set of time slots that can't be used (due to conflicts with already assigned courses)
            Set<UUID> invalidTimeSlots = new HashSet<>();
            Set<UUID> preferredToAvoidTimeSlots = new HashSet<>(); // For soft constraints
            
            // Hard constraint: Check conflicting courses (cannot be in same time slot)
            Set<String> conflictingCourses = courseConflictGraph.getOrDefault(courseId, Collections.emptySet());
            for (String conflictingCourse : conflictingCourses) {
                // If the conflicting course is already assigned, add its time slot to invalid set
                if (courseToTimeSlot.containsKey(conflictingCourse)) {
                    invalidTimeSlots.add(courseToTimeSlot.get(conflictingCourse));
                    
                    // Also add time slots on same day to avoid consecutive time slots for conflicts
                    // UUID conflictTimeSlotId = courseToTimeSlot.get(conflictingCourse);
                    // for (TimeSlot timeSlot : data.getAvailableTimeSlots()) {
                    //     if (timeSlot.getId().equals(conflictTimeSlotId)) {
                    //         // Find all time slots on same day
                    //         for (TimeSlot otherSlot : data.getAvailableTimeSlots()) {
                    //             if (otherSlot.isSameDayAs(timeSlot) && otherSlot.isConsecutiveWith(timeSlot)) {
                    //                 invalidTimeSlots.add(otherSlot.getId());
                    //             }
                    //         }
                    //         break;
                    //     }
                    // }
                }
            }
            
            // Add prohibited time slots to invalid set
            // for (TimeSlotRoomPair prohibited : data.getProhibitedSlots()) {
            //     for (TimeSlot timeSlot : data.getAvailableTimeSlots()) {
            //         if (timeSlot.getSessionId().equals(prohibited.getSessionId()) &&
            //             timeSlot.getDate().equals(prohibited.getDate())) {
            //             invalidTimeSlots.add(timeSlot.getId());
            //             break;
            //         }
            //     }
            // }
            
            // Soft constraint: Check courses in same group (should avoid same day if possible)
            Set<String> coursesInSameGroup = coursesByGroup.getOrDefault(courseId, Collections.emptySet());
            for (String groupCourse : coursesInSameGroup) {
                if (!courseId.equals(groupCourse) && courseToTimeSlot.containsKey(groupCourse)) {
                    UUID groupCourseTimeSlotId = courseToTimeSlot.get(groupCourse);
                    
                    // Find the timeslot for this course
                    for (TimeSlot groupTimeSlot : data.getAvailableTimeSlots()) {
                        if (groupTimeSlot.getId().equals(groupCourseTimeSlotId)) {
                            // Mark timeslots on the same day as "preferred to avoid"
                            for (TimeSlot otherSlot : data.getAvailableTimeSlots()) {
                                if (otherSlot.isSameDayAs(groupTimeSlot) && !invalidTimeSlots.contains(otherSlot.getId())) {
                                    preferredToAvoidTimeSlots.add(otherSlot.getId());
                                }
                            }
                            break;
                        }
                    }
                }
            }
            
            // Try to find a valid time slot (not in invalid set)
            UUID assignedTimeSlot = null;
            
            // First try: Find slots that satisfy both hard and soft constraints
            List<TimeSlot> bestTimeSlots = data.getAvailableTimeSlots().stream()
                .filter(ts -> !invalidTimeSlots.contains(ts.getId()))
                .filter(ts -> !preferredToAvoidTimeSlots.contains(ts.getId()))
                .sorted(createTimeSlotComparator(data, timeSlotToCourses))
                .collect(Collectors.toList());
            
            if (!bestTimeSlots.isEmpty()) {
                assignedTimeSlot = bestTimeSlots.get(0).getId();
            } else {
                // Second try: Find slots that satisfy only hard constraints
                List<TimeSlot> validTimeSlots = data.getAvailableTimeSlots().stream()
                    .filter(ts -> !invalidTimeSlots.contains(ts.getId()))
                    .sorted(createTimeSlotComparator(data, timeSlotToCourses))
                    .collect(Collectors.toList());
                
                if (!validTimeSlots.isEmpty()) {
                    assignedTimeSlot = validTimeSlots.get(0).getId();
                    log.warn("Course {} assigned to same-day timeslot as another course in its group", courseId);
                }
            }
            
            // If no valid time slot, skip this course but continue with others
            if (assignedTimeSlot == null) {
                log.warn("Could not find valid time slot for course {}. Skipping.", courseId);
                continue;
            }
            
            // Assign the time slot to the course
            courseToTimeSlot.put(courseId, assignedTimeSlot);
            
            // Update time slot usage
            timeSlotToCourses.computeIfAbsent(assignedTimeSlot, k -> new HashSet<>())
                .add(courseId);
            
            assignedCourses++;
        }

        log.info("Time slot assignment completed: {}/{} courses assigned successfully", 
            assignedCourses, totalCourses);

        return courseToTimeSlot;
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

        // Iterate through classes and group courses by group ID
        for (ExamClass examClass : data.getExamClasses()) {
            String courseId = examClass.getCourseId();
            String groupId = examClass.getGroupId();
            
            if (groupId != null && !groupId.isEmpty()) {
                // For each group ID, map the courses in that group
                for (ExamClass otherClass : data.getExamClasses()) {
                    if (groupId.equals(otherClass.getGroupId())) {
                        coursesByGroup.computeIfAbsent(courseId, k -> new HashSet<>())
                            .add(otherClass.getCourseId());
                    }
                }
            }
        }

        return coursesByGroup;
    }
    
    /**
     * Build a graph of course conflicts
     */
    private Map<String, Set<String>> buildCourseConflictGraph(TimetablingData data) {
        Map<String, Set<String>> courseConflictGraph = new HashMap<>();
        
        // Initialize empty sets for all courses
        for (String courseId : data.getClassesByCourseId().keySet()) {
            courseConflictGraph.put(courseId, new HashSet<>());
        }
        
        // Build course conflict graph from class conflict graph
        for (Map.Entry<UUID, Set<UUID>> classConflict : data.getConflictGraph().entrySet()) {
            UUID classId1 = classConflict.getKey();
            
            // Find course ID for class 1
            String courseId1 = null;
            for (ExamClass ec : data.getExamClasses()) {
                if (ec.getId().equals(classId1)) {
                    courseId1 = ec.getCourseId();
                    break;
                }
            }
            
            if (courseId1 == null) continue;
            
            // Add conflicts with all conflicting classes
            for (UUID classId2 : classConflict.getValue()) {
                // Find course ID for class 2
                String courseId2 = null;
                for (ExamClass ec : data.getExamClasses()) {
                    if (ec.getId().equals(classId2)) {
                        courseId2 = ec.getCourseId();
                        break;
                    }
                }
                
                if (courseId2 == null || courseId1.equals(courseId2)) continue;
                
                // Add conflict
                courseConflictGraph.get(courseId1).add(courseId2);
                courseConflictGraph.get(courseId2).add(courseId1);
            }
        }
        
        return courseConflictGraph;
    }
    
    /**
     * Assign rooms to classes, trying to assign as many as possible
     * With additional soft constraint to keep classes with same course and group in the same building
     */
    private boolean assignRoomsToClasses(
        TimetablingSolution solution,
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
                continue; // Skip this course but continue with others
            }
            
            // Sort classes by number of students (largest first)
            List<ExamClass> sortedClasses = new ArrayList<>(classesInCourse);
            sortedClasses.sort((c1, c2) -> 
                Integer.compare(c2.getNumberOfStudents(), c1.getNumberOfStudents()));
            
            // Assign rooms to classes
            for (ExamClass examClass : sortedClasses) {
                int requiredCapacity = examClass.getNumberOfStudents() * 15 / 10; // Room needs 2n seats
                
                // Create a key for course+group combination
                String courseGroupKey = courseId + "_" + (examClass.getGroupId() != null ? examClass.getGroupId() : "");
                
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
                
                // Add assignment to solution
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

        log.info("Room assignment completed: {}/{} classes assigned successfully", 
            assignedClasses, totalClasses);

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
     * Optimize solution using local search
     */
    private TimetablingSolution optimizeSolution(TimetablingSolution initialSolution, TimetablingData data) {
        TimetablingSolution currentSolution = initialSolution;
        currentSolution.calculateMetrics(data);
        
        double bestScore = currentSolution.getQualityScore();
        TimetablingSolution bestSolution = deepCopy(currentSolution);
        
        Random random = new Random();
        int iterations = 0;
        int noImprovementCount = 0;
        
        while (iterations < MAX_ITERATIONS && noImprovementCount < 100) {
            System.err.println("Iteration: " + iterations + ", Best Score: " + bestScore);
            iterations++;
            
            // Generate a neighbor solution by applying a random move
            TimetablingSolution neighborSolution = applyRandomMove(currentSolution, data, random);
            
            // Calculate metrics for the neighbor solution
            neighborSolution.calculateMetrics(data);
            
            // If the neighbor is better, accept it
            double neighborScore = neighborSolution.getQualityScore();
            if (neighborScore > bestScore) {
                bestScore = neighborScore;
                bestSolution = deepCopy(neighborSolution);
                currentSolution = neighborSolution;
                noImprovementCount = 0;
                
                log.debug("Improvement found at iteration {}. New score: {}", iterations, bestScore);
            } else {
                // Sometimes accept worse solutions (simulated annealing style)
                double acceptanceProbability = Math.exp((neighborScore - bestScore) / (1.0 - iterations / (double)MAX_ITERATIONS));
                if (random.nextDouble() < acceptanceProbability) {
                    currentSolution = neighborSolution;
                    log.debug("Accepting worse solution with probability {}", acceptanceProbability);
                }
                
                noImprovementCount++;
            }
        }
        
        log.info("Optimization completed after {} iterations", iterations);
        return bestSolution;
    }
    
    /**
     * Apply a random move to generate a neighbor solution
     */
    private TimetablingSolution applyRandomMove(TimetablingSolution solution, TimetablingData data, Random random) {
        TimetablingSolution neighbor = deepCopy(solution);
        
        // Choose a random move type
        int moveType = random.nextInt(3);
        
        switch (moveType) {
            case 0:
                // Swap time slots of two courses
                swapCourseTimeSlots(neighbor, data, random);
                break;
            case 1:
                // Reassign rooms within a time slot
                reassignRooms(neighbor, data, random);
                break;
            case 2:
                // Move a course to a different time slot
                moveCourseToNewTimeSlot(neighbor, data, random);
                break;
        }
        
        return neighbor;
    }
    
    /**
     * Swap time slots between two courses
     */
    private void swapCourseTimeSlots(TimetablingSolution solution, TimetablingData data, Random random) {
        // Get list of course IDs
        List<String> courseIds = new ArrayList<>(solution.getCourseTimeSlotAssignments().keySet());
        
        // Need at least 2 courses to swap
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
            return; // Invalid swap, don't make any changes
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
            
            // Find the time slot to get session ID and date
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
            
            // Find the time slot to get session ID and date
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
                                TimetablingSolution solution, TimetablingData data) {
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
    private void reassignRooms(TimetablingSolution solution, TimetablingData data, Random random) {
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
        
        UUID room1 = details1.getRoomId();
        UUID room2 = details2.getRoomId();
        
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
        Map<UUID, Integer> roomUsage = solution.getRoomUsageCounts();
        roomUsage.put(room1, roomUsage.getOrDefault(room1, 0));
        roomUsage.put(room2, roomUsage.getOrDefault(room2, 0));
    }
    
    /**
     * Move a course to a new time slot
     */
    private void moveCourseToNewTimeSlot(TimetablingSolution solution, TimetablingData data, Random random) {
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
                
                // Also add consecutive time slots on same day
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
            
            // Find course ID for this class
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
    private TimetablingSolution deepCopy(TimetablingSolution solution) {
        TimetablingSolution copy = new TimetablingSolution();
        
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
        
        // Copy course time slot assignments
        copy.setCourseTimeSlotAssignments(new HashMap<>(solution.getCourseTimeSlotAssignments()));
        
        // Copy time slot class assignments
        Map<UUID, List<UUID>> timeSlotClassAssignments = new HashMap<>();
        for (Map.Entry<UUID, List<UUID>> entry : solution.getTimeSlotClassAssignments().entrySet()) {
            timeSlotClassAssignments.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        copy.setTimeSlotClassAssignments(timeSlotClassAssignments);
        
        // Copy room usage counts
        copy.setRoomUsageCounts(new HashMap<>(solution.getRoomUsageCounts()));
        
        // Copy time slot usage counts
        copy.setTimeSlotUsageCounts(new HashMap<>(solution.getTimeSlotUsageCounts()));
        
        // Copy metrics
        copy.setGroupSpacingViolations(solution.getGroupSpacingViolations());
        copy.setRoomBalanceMetric(solution.getRoomBalanceMetric());
        copy.setTimeSlotBalanceMetric(solution.getTimeSlotBalanceMetric());
        copy.setEarlySlotAssignments(solution.getEarlySlotAssignments());
        copy.setQualityScore(solution.getQualityScore());
        
        return copy;
    }
}

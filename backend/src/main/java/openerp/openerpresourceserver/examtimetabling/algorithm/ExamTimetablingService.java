package openerp.openerpresourceserver.examtimetabling.algorithm;

import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataExamTimeTablingInput;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.AssignmentDetails;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.ExamTimetableSolution;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.TimetablingData;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.TimetablingSolution;
import openerp.openerpresourceserver.examtimetabling.entity.*;
import openerp.openerpresourceserver.examtimetabling.repository.*;

import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;
import openerp.openerpresourceserver.generaltimetabling.repo.ClassroomRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingRoomRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for automating exam timetabling assignments
 */
@Service
@Slf4j
public class ExamTimetablingService {

    @Autowired
    private ClassroomRepo classroomRepo;

    @Autowired
    private ExamClassRepository examClassRepository;
    
    @Autowired
    private ClassroomRepository examRoomRepository;
    
    @Autowired
    private ExamTimetableSessionRepository examTimetableSessionRepository;
    
    @Autowired
    private ExamTimetableAssignmentRepository examTimetableAssignmentRepository;
    
    @Autowired
    private ConflictExamTimetablingClassRepository conflictRepository;
    
    @Autowired
    private ExamTimetableRepository examTimetableRepository;
    
    @Autowired
    private ExamPlanRepository examPlanRepository;

    /**
     * Main method to automatically assign classes to rooms and time slots with time limit
     * 
     * @param examTimetableId The ID of the exam timetable
     * @param classIds List of class IDs to be assigned
     * @param examDates List of dates for the exam period in 'dd-MM-yyyy' format
     * @param algorithm Algorithm to use (currently ignored as there's only one)
     * @param timeLimit Maximum time in minutes to run the algorithm
     * @return true if assignment is successful, false otherwise
     */

    private boolean greedyAlgorithmPQD(TimetablingData data, List<String> examDates){
        List<Classroom> rooms = classroomRepo.findAll();
        DataMapper DM = new DataMapper();
        MapDataExamTimeTablingInput I = DM.mapData(data,rooms,examDates);
        GreedyAlgorithmPQD gSolverPQD = new GreedyAlgorithmPQD();
        gSolverPQD.solve(I);
        gSolverPQD.printSolution();
        //I.print();
        //for(String d: examDates) System.out.println("exam date " + d);
        return true;
    }
    @Transactional
    public boolean autoAssignClass(UUID examTimetableId, List<UUID> classIds, 
                                  List<String> examDates, String algorithm, Integer timeLimit) {
        log.info("Starting auto assignment for {} classes over {} dates with time limit {} minutes", 
                classIds.size(), examDates.size(), timeLimit);
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try {
            clearExistingAssignments(examTimetableId, classIds);
            log.info("Cleared existing assignments for {} classes", classIds.size());
            
            Future<Boolean> future = executor.submit(() -> {
                try {
                    // Step 1: Process data
                    ExamTimetableProcessor processor = new ExamTimetableProcessor(
                        examClassRepository,
                        examRoomRepository,
                        examTimetableSessionRepository,
                        examTimetableAssignmentRepository,
                        conflictRepository,
                        examTimetableRepository
                    );
                    
                    TimetablingData data = processor.processData(examTimetableId, classIds, examDates);
                    log.info("Data processing complete. {} classes, {} time slots, {} rooms", 
                        data.getExamClasses().size(), data.getAvailableTimeSlots().size(), data.getAvailableRooms().size());

                    if(algorithm.equals("Genetic Algorithm")){
                        return greedyAlgorithmPQD(data,examDates);
                    }


                    // Step 2: Apply algorithm
                    ExamTimetableAlgorithm timetableAlgorithm = new ExamTimetableAlgorithm();
                    ExamTimetableSolution solution = timetableAlgorithm.assign(data);

                    if (!solution.isComplete()) {
                        log.warn("Failed to find complete assignment. Assigned: {}/{}", 
                            solution.getAssignedClasses().size(), classIds.size());
                        return false;
                    }
                    
                    // Step 3: Save results
                    saveSolution(examTimetableId, solution);
                    log.info("Successfully assigned all {} classes", classIds.size());
                    return true;
                    
                } catch (Exception e) {
                    log.error("Error during auto assignment", e);
                    throw new RuntimeException("Failed to auto-assign classes: " + e.getMessage(), e);
                }
            });
            
            return future.get(timeLimit, TimeUnit.MINUTES);
            
        } catch (TimeoutException e) {
            log.warn("Algorithm timed out after {} minutes", timeLimit);
            return false;
        } catch (Exception e) {
            log.error("Error during auto assignment", e);
            throw new RuntimeException("Failed to auto-assign classes: " + e.getMessage(), e);
        } finally {
            executor.shutdownNow();
        }
    }
    
    /**
     * Clear existing assignments (room_id, session_id, date, week_number) 
     * for the classes that will be auto-assigned
     */
    private void clearExistingAssignments(UUID examTimetableId, List<UUID> classIds) {
        List<ExamTimetableAssignment> existingAssignments = 
            examTimetableAssignmentRepository.findByExamTimetableIdAndExamTimetablingClassIdIn(
                examTimetableId, classIds);
        
        for (ExamTimetableAssignment assignment : existingAssignments) {
            assignment.setRoomId(null);
            assignment.setExamSessionId(null);
            assignment.setDate(null);
            assignment.setWeekNumber(null);
        }
        
        examTimetableAssignmentRepository.saveAll(existingAssignments);
    }
    
    /**
     * Saves the solution by updating existing assignment records in the database
     */
    private void saveSolution(UUID examTimetableId, TimetablingSolution solution) {
        ExamTimetable examTimetable = examTimetableRepository.findById(examTimetableId)
            .orElseThrow(() -> new RuntimeException("Exam timetable not found: " + examTimetableId));
        
        ExamPlan examPlan = examPlanRepository.findById(examTimetable.getExamPlanId())
            .orElseThrow(() -> new RuntimeException("Exam plan not found: " + examTimetable.getExamPlanId()));

        Map<UUID, String> sessionNames = examTimetableSessionRepository.findAll().stream()
            .collect(Collectors.toMap(
                ExamTimetableSession::getId, 
                session -> session.getName()
            ));
        List<ExamTimetableAssignment> existingAssignments = 
            examTimetableAssignmentRepository.findByExamTimetableId(examTimetableId);
        
        Map<UUID, ExamTimetableAssignment> assignmentsByClassId = existingAssignments.stream()
            .collect(Collectors.toMap(
                ExamTimetableAssignment::getExamTimetablingClassId, 
                assignment -> assignment,
                (a1, a2) -> a1 
            ));
        
        List<ExamTimetableAssignment> updatedAssignments = new ArrayList<>();
        
        for (Map.Entry<UUID, AssignmentDetails> entry : solution.getAssignedClasses().entrySet()) {
            UUID classId = entry.getKey();
            AssignmentDetails details = entry.getValue();
            
            ExamTimetableAssignment assignment = assignmentsByClassId.get(classId);
            
            if (assignment != null) {
                assignment.setRoomId(details.getRoomId());
                assignment.setExamSessionId(details.getSessionId());
                assignment.setSession(sessionNames.get(details.getSessionId()));

                assignment.setDate(details.getDate());
                
                // Calculate week number based on exam plan
                Integer weekNumber = calculateWeekNumber(details.getDate(), examPlan);
                assignment.setWeekNumber(weekNumber);
                
                updatedAssignments.add(assignment);
            } else {
                log.warn("No existing assignment found for class ID: {}", classId);
            }
        }
        
        if (!updatedAssignments.isEmpty()) {
            examTimetableAssignmentRepository.saveAll(updatedAssignments);
            log.info("Updated {} assignment records", updatedAssignments.size());
        }
    }
    
    /**
     * Calculate week number based on exam plan start date and start week
     */
    private Integer calculateWeekNumber(LocalDate date, ExamPlan examPlan) {
        if (date == null || examPlan == null || examPlan.getStartTime() == null || examPlan.getStartWeek() == null) {
            return null;
        }
        
        LocalDate planStartDate = examPlan.getStartTime().toLocalDate();
        Integer planStartWeek = examPlan.getStartWeek();
        
        long daysBetween = ChronoUnit.DAYS.between(planStartDate, date);
        
        int weekDifference = (int) (daysBetween / 7);
        
        if (daysBetween % 7 > 0 && planStartDate.getDayOfWeek().getValue() > date.getDayOfWeek().getValue()) {
            weekDifference++;
        }
        
        return planStartWeek + weekDifference;
    }
}

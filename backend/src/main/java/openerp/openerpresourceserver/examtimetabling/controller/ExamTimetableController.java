package openerp.openerpresourceserver.examtimetabling.controller;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.algorithm.ExamTimetablingService;
import openerp.openerpresourceserver.examtimetabling.dtos.AssignmentUpdateDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.AutoAssignRequestDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ConflictDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ConflictResponseDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamAssignmentDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDetailDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetablingResponse;
import openerp.openerpresourceserver.examtimetabling.dtos.TimetableStatisticsDTO;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetable;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableAssignment;
import openerp.openerpresourceserver.examtimetabling.service.ExamTimetableAssignmentService;
import openerp.openerpresourceserver.examtimetabling.service.ExamTimetableService;

@RestController
@RequestMapping("/exam-timetable")
@RequiredArgsConstructor
public class ExamTimetableController {
    private final ExamTimetableService examTimetableService;
    private final ExamTimetableAssignmentService examTimetableAssignmentService;
    private final ExamTimetablingService examTimetablingService;
    
    @GetMapping("/plan/{examPlanId}")
    public ResponseEntity<List<ExamTimetableDTO>> getAllTimetablesByExamPlanId(@PathVariable UUID examPlanId) {
        try {
            List<ExamTimetableDTO> timetables = examTimetableService.getAllTimetablesByExamPlanId(examPlanId);
            return ResponseEntity.ok(timetables);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ExamTimetableDetailDTO> getTimetableDetail(@PathVariable UUID id) {
        try {
            ExamTimetableDetailDTO detail = examTimetableService.getTimetableDetail(id);
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statistic/{timetableId}")
    public ResponseEntity<TimetableStatisticsDTO> getTimetableStatistics(@PathVariable UUID timetableId) {
        try {
            TimetableStatisticsDTO statistics = examTimetableService.generateStatistics(timetableId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ExamTimetable> createExamTimetable(@Valid @RequestBody ExamTimetable examTimetable) {
        try {
            ExamTimetable createdTimetable = examTimetableService.createExamTimetable(examTimetable);
            return ResponseEntity.ok(createdTimetable);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTimetable(@PathVariable UUID id) {
        try {
            examTimetableService.deleteTimetable(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ExamTimetable> updateTimetable(@RequestBody Map<String, Object> payload) {
        try {
            // Extract and validate ID from payload
            String idStr = (String) payload.get("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            UUID id;
            try {
                id = UUID.fromString(idStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
            
            // Extract and validate name
            String name = (String) payload.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ExamTimetable updatedTimetable = examTimetableService.updateTimetable(id, name);
            return ResponseEntity.ok(updatedTimetable);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/assignment/check-conflict")
    public ResponseEntity<List<ConflictDTO>> checkConflicts(@RequestBody List<AssignmentUpdateDTO> assignmentChanges) {
        try {
            List<ConflictDTO> conflicts = examTimetableAssignmentService.checkForConflicts(assignmentChanges);
            return ResponseEntity.ok(conflicts);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/{timetableId}/conflicts")
    public ResponseEntity<?> checkTimetableConflicts(@PathVariable UUID timetableId) {
        try {
            List<ConflictResponseDTO> conflicts = examTimetableAssignmentService.checkTimetableConflicts(timetableId);
            return ResponseEntity.ok(conflicts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/assignment/update-batch")
    public ResponseEntity<Object> updateAssignments(@RequestBody List<AssignmentUpdateDTO> assignmentChanges) {
        try {
            examTimetableAssignmentService.updateAssignments(assignmentChanges);
            return ResponseEntity.ok(Map.of("success", true, "message", "Assignments updated successfully"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to update assignments"));
        }
    }

    @PostMapping("/assignment/unassign")
    public ResponseEntity<?> unassignAssignments(@RequestBody List<UUID> assignmentIds) {
        try {
            int unassignedCount = examTimetableAssignmentService.unassignAssignments(assignmentIds);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully unassigned " + unassignedCount + " assignments"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/assignment/{timetableId}")
    public ResponseEntity<List<ExamAssignmentDTO>> getAssignments(@PathVariable UUID timetableId) {
        try {
            List<ExamAssignmentDTO> assignments = examTimetableAssignmentService.getAssignmentsByTimetableId(timetableId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }

    @GetMapping("/assignment/algorithms")
    public ResponseEntity<String[]> getAssignmentAlgorithm() {
        try {
            String[] assignments = {
                "Greedy Algorithm (Khoa)",
                "Genetic Algorithm",
            };
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new String[0]);
        }
    }

    @PostMapping("/assignment/export")
    public ResponseEntity<Resource> exportAssignmentsToExcel(@RequestBody List<String> assignmentIds) {
        try {
            ByteArrayInputStream excelFile = examTimetableAssignmentService.exportAssignmentsToExcel(assignmentIds);
            
            String filename = "exam_assignments_export.xlsx";
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excelFile));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/assignment/auto-assign")
    public ResponseEntity<ExamTimetablingResponse> autoAssignClasses(
            @Valid @RequestBody AutoAssignRequestDTO request) {
        try {
            long startTime = System.currentTimeMillis();
            
            boolean success = examTimetablingService.autoAssignClass(
                request.getExamTimetableId(),
                request.getClassIds(),
                request.getExamDates(),
                request.getAlgorithm(),
                request.getTimeLimit()
            );
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            ExamTimetablingResponse response = new ExamTimetablingResponse();
            response.setSuccess(success);
            response.setExecutionTimeMs(executionTime);
            
            if (success) {
                response.setMessage("Successfully assigned all classes. Execution time: " + executionTime + "ms");
                return ResponseEntity.ok(response);
            } else {
                response.setMessage("Failed to assign all classes. Some constraints could not be satisfied or time limit exceeded. Execution time: " + executionTime + "ms");
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
            }
            
        } catch (Exception e) {
            ExamTimetablingResponse response = new ExamTimetablingResponse();
            response.setSuccess(false);
            response.setMessage("Error processing request: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

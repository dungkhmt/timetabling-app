package openerp.openerpresourceserver.examtimetabling.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.AssignmentUpdateDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ConflictDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamAssignmentDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamTimetableDetailDTO;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetable;
import openerp.openerpresourceserver.examtimetabling.service.ExamTimetableAssignmentService;
import openerp.openerpresourceserver.examtimetabling.service.ExamTimetableService;

@RestController
@RequestMapping("/exam-timetable")
@RequiredArgsConstructor
public class ExamTimetableController {
    private final ExamTimetableService examTimetableService;
    private final ExamTimetableAssignmentService examTimetableAssignmentService;
    
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
            examTimetableService.softDeleteTimetable(id);
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


    @GetMapping("/assignment/{timetableId}")
    public ResponseEntity<List<ExamAssignmentDTO>> getAssignments(@PathVariable UUID timetableId) {
        try {
            List<ExamAssignmentDTO> assignments = examTimetableAssignmentService.getAssignmentsByTimetableId(timetableId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }
}

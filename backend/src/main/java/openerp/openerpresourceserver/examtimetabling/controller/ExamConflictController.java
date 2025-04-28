package openerp.openerpresourceserver.examtimetabling.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.ConflictCreateDTO;
import openerp.openerpresourceserver.examtimetabling.entity.ConflictExamTimetablingClass;
import openerp.openerpresourceserver.examtimetabling.service.ExamConflictService;

@RestController
@RequestMapping("/exam-conflict")
@RequiredArgsConstructor
public class ExamConflictController {
    
    private final ExamConflictService examConflictService;
    
    @GetMapping("/plan/{examPlanId}")
    public ResponseEntity<?> getConflictsForPlan(@PathVariable UUID examPlanId) {
        try {
          System.err.println("ExamConflictController.getConflictsForPlan: examPlanId = " + examPlanId);
            Map<String, Object> result = examConflictService.getConflictsForPlan(examPlanId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteConflicts(@RequestBody List<UUID> conflictIds) {
        try {
            int deletedCount = examConflictService.deleteConflicts(conflictIds);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully deleted " + deletedCount + " conflict records"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/create")
    public ResponseEntity<?> createConflict(@RequestBody ConflictCreateDTO conflictCreateDTO) {
        try {
            ConflictExamTimetablingClass conflict = examConflictService.createConflict(
                conflictCreateDTO.getExamTimetablingClassId1(),
                conflictCreateDTO.getExamTimetablingClassId2()
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully created conflict record",
                "conflict", conflict
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}

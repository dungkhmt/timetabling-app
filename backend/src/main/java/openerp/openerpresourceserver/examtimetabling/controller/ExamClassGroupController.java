package openerp.openerpresourceserver.examtimetabling.controller;

import java.util.List;
import java.util.Map;

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
import openerp.openerpresourceserver.examtimetabling.dtos.ExamClassGroupBulkCreateDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamClassGroupWithUsageDTO;
import openerp.openerpresourceserver.examtimetabling.entity.ExamClassGroup;
import openerp.openerpresourceserver.examtimetabling.service.ExamClassGroupService;

@RestController
@RequestMapping("/exam-class-group")
@RequiredArgsConstructor
public class ExamClassGroupController {
    private final ExamClassGroupService examClassGroupService;
    
    @GetMapping
    public ResponseEntity<List<ExamClassGroupWithUsageDTO>> getAllExamClassGroups() {
        List<ExamClassGroupWithUsageDTO> examClassGroups = examClassGroupService.getAllExamClassGroups();
        return ResponseEntity.ok(examClassGroups);
    }

    @PostMapping("/bulk-create")
    public ResponseEntity<?> bulkCreateGroups(@Valid @RequestBody ExamClassGroupBulkCreateDTO request) {
        try {
            List<ExamClassGroup> createdGroups = examClassGroupService.bulkCreateGroups(request.getGroupNames());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Successfully created " + createdGroups.size() + " groups",
                "groups", createdGroups
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error creating exam class groups: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Integer id, @Valid @RequestBody ExamClassGroup groupDetails) {
        try {
            ExamClassGroup updatedGroup = examClassGroupService.updateGroup(id, groupDetails);
            return ResponseEntity.ok(updatedGroup);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error updating exam class group: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/delete")
    public ResponseEntity<?> deleteGroups(@RequestBody List<Integer> ids) {
        try {
            int deletedCount = examClassGroupService.deleteGroups(ids);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully deleted " + deletedCount + " exam class groups"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error deleting exam class groups: " + e.getMessage()
            ));
        }
    }
}

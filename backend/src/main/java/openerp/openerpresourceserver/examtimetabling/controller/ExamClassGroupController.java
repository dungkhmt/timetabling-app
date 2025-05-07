package openerp.openerpresourceserver.examtimetabling.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.entity.ExamClassGroup;
import openerp.openerpresourceserver.examtimetabling.service.ExamClassGroupService;

@RestController
@RequestMapping("/exam-class-group")
@RequiredArgsConstructor
public class ExamClassGroupController {
    private final ExamClassGroupService examClassGroupService;
    
    @GetMapping
    public ResponseEntity<List<ExamClassGroup>> getAllExamClassGroups() {
        List<ExamClassGroup> examClassGroups = examClassGroupService.getAllExamClassGroups();
        return ResponseEntity.ok(examClassGroups);
    }
}

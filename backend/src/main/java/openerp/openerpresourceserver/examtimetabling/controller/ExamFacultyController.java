package openerp.openerpresourceserver.examtimetabling.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.entity.ExamFaculty;
import openerp.openerpresourceserver.examtimetabling.service.ExamFacultyService;

@RestController
@RequestMapping("/exam-faculty")
@RequiredArgsConstructor
public class ExamFacultyController {
    private final ExamFacultyService examFacultyService;
    
    @GetMapping
    public ResponseEntity<List<ExamFaculty>> getAllExamFacultys() {
        List<ExamFaculty> examFaculties = examFacultyService.getAllExamFaculties();
        return ResponseEntity.ok(examFaculties);
    }
}

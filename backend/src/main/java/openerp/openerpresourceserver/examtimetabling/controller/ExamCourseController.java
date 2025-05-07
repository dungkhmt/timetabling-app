package openerp.openerpresourceserver.examtimetabling.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.entity.ExamCourse;
import openerp.openerpresourceserver.examtimetabling.service.ExamCourseService;

@RestController
@RequestMapping("/exam-course")
@RequiredArgsConstructor
public class ExamCourseController {
    private final ExamCourseService examCourseService;
    
    @GetMapping
    public ResponseEntity<List<ExamCourse>> getAllExamCourses() {
        List<ExamCourse> examCourses = examCourseService.getAllExamCourses();
        return ResponseEntity.ok(examCourses);
    }
}

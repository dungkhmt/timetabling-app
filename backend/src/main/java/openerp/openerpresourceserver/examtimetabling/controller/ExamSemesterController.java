package openerp.openerpresourceserver.examtimetabling.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.service.SemesterService;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Semester;

@RestController
@RequestMapping("/exam-semester")
@RequiredArgsConstructor
public class ExamSemesterController {
    private final SemesterService semesterService;
    
    @GetMapping
    public ResponseEntity<List<Semester>> getAllRooms() {
        List<Semester> semesters = semesterService.getAllSemesters();
        return ResponseEntity.ok(semesters);
    }
}

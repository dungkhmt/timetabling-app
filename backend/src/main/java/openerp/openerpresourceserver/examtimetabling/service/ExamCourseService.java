package openerp.openerpresourceserver.examtimetabling.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.entity.ExamCourse;
import openerp.openerpresourceserver.examtimetabling.repository.ExamCourseRepository;

@Service
@RequiredArgsConstructor
public class ExamCourseService {
    private final ExamCourseRepository examCourseRepository;
    
    public List<ExamCourse> getAllExamCourses() {
        return examCourseRepository.findAll();
    }
}

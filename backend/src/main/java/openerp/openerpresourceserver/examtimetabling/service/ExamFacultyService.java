package openerp.openerpresourceserver.examtimetabling.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.entity.ExamFaculty;
import openerp.openerpresourceserver.examtimetabling.repository.ExamFacultyRepository;

@Service
@RequiredArgsConstructor
public class ExamFacultyService {
    private final ExamFacultyRepository examFacultyRepository;
    
    public List<ExamFaculty> getAllExamFaculties() {
        return examFacultyRepository.findAll();
    }
}

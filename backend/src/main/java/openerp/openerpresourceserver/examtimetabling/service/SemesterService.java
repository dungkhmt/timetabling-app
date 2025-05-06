package openerp.openerpresourceserver.examtimetabling.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.repository.SemesterRepository;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Semester;

@Service
@RequiredArgsConstructor
public class SemesterService {
    private final SemesterRepository semesterRepository;
    
    public List<Semester> getAllSemesters() {
        return semesterRepository.findAll();
    }
}

package openerp.openerpresourceserver.examtimetabling.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.entity.ExamClassGroup;
import openerp.openerpresourceserver.examtimetabling.repository.ExamClassGroupRepository;

@Service
@RequiredArgsConstructor
public class ExamClassGroupService {
    private final ExamClassGroupRepository examClassGroupRepository;
    
    public List<ExamClassGroup> getAllExamClassGroups() {
        return examClassGroupRepository.findAll();
    }
}

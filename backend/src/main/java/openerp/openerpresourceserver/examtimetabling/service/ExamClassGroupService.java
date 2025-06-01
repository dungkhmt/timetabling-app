package openerp.openerpresourceserver.examtimetabling.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamClassGroupWithUsageDTO;
import openerp.openerpresourceserver.examtimetabling.entity.ExamClassGroup;
import openerp.openerpresourceserver.examtimetabling.repository.ExamClassGroupRepository;

@Service
@RequiredArgsConstructor
public class ExamClassGroupService {
    private final ExamClassGroupRepository examClassGroupRepository;
    private final EntityManager entityManager;
    
    public List<ExamClassGroupWithUsageDTO> getAllExamClassGroups() {
        String sql = "SELECT g.id, g.name, " +
                    "CASE WHEN COUNT(c.id) > 0 THEN true ELSE false END as is_using " +
                    "FROM exam_timetabling_group g " +
                    "LEFT JOIN exam_timetabling_class c ON g.id = c.exam_group_id " +
                    "GROUP BY g.id, g.name " +
                    "ORDER BY g.id";
        
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        
        return results.stream()
            .map(row -> new ExamClassGroupWithUsageDTO(
                ((Number) row[0]).intValue(),  
                (String) row[1],               
                (Boolean) row[2]             
            ))
            .collect(Collectors.toList());
    }

    public ExamClassGroup getGroupById(Integer id) {
        return examClassGroupRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Exam class group not found with id: " + id));
    }
    @Transactional
    public List<ExamClassGroup> bulkCreateGroups(List<String> groupNames) {
        if (groupNames == null || groupNames.isEmpty()) {
            throw new IllegalArgumentException("Group names list cannot be empty");
        }
        
        List<ExamClassGroup> newGroups = new ArrayList<>();
        
        for (String groupName : groupNames) {
            if (groupName == null || groupName.trim().isEmpty()) {
                continue; 
            }
            
            ExamClassGroup group = new ExamClassGroup();
            group.setName(groupName.trim());
            newGroups.add(group);
        }
        
        return examClassGroupRepository.saveAll(newGroups);
    }
    
    @Transactional
    public ExamClassGroup updateGroup(Integer id, ExamClassGroup groupDetails) {
        ExamClassGroup existingGroup = examClassGroupRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Exam class group not found with id: " + id));
        
        // Update fields
        existingGroup.setName(groupDetails.getName());
        
        // Save and return
        return examClassGroupRepository.save(existingGroup);
    }
    
    @Transactional
    public int deleteGroups(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        // Filter out any IDs that don't exist
        List<Integer> existingIds = ids.stream()
            .filter(id -> examClassGroupRepository.existsById(id))
            .collect(Collectors.toList());
        
        if (existingIds.isEmpty()) {
            return 0;
        }
        
        // Delete the groups in one efficient operation
        StringBuilder idListBuilder = new StringBuilder();
        for (int i = 0; i < existingIds.size(); i++) {
            if (i > 0) {
                idListBuilder.append(",");
            }
            idListBuilder.append(existingIds.get(i));
        }
        
        String sql = "DELETE FROM exam_timetabling_group WHERE id IN (" + idListBuilder.toString() + ")";
        Query query = entityManager.createNativeQuery(sql);
        int deletedCount = query.executeUpdate();
        
        return deletedCount;
    }
}

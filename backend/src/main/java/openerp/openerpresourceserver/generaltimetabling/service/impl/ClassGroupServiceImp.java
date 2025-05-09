package openerp.openerpresourceserver.generaltimetabling.service.impl;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ClassGroupSummary;
import openerp.openerpresourceserver.generaltimetabling.model.entity.ClassGroup;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Group;
import openerp.openerpresourceserver.generaltimetabling.repo.ClassGroupRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.GroupRepo;
import openerp.openerpresourceserver.generaltimetabling.service.ClassGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Log4j2
@Service
public class ClassGroupServiceImp implements ClassGroupService {

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private ClassGroupRepo classGroupRepo;

    @Override
    public List<ClassGroupSummary> getAllClassGroup(Long classId) {
        log.info("getAllClassGroup, classId = " + classId);
        if (classId == null || classId <= 0) {
            throw new IllegalArgumentException("Invalid classId");
        }

        try {
            List<Group> allGroups = groupRepo.findAll();
            if (allGroups.isEmpty()) {
                System.out.println("No groups found in the system.");
            } else {
                System.out.println("Found " + allGroups.size() + " groups.");
            }
            //log.info("getAllClassGroup, allGroups.sz = " + allGroups.size());
            List<Long> assignedGroupIds = classGroupRepo.findByClassId(classId)
                    .stream()
                    .map(ClassGroup::getGroupId)
                    .collect(Collectors.toList());
            log.info("getAllClassGroup, classId = " + classId + " allGroups.sz = " + allGroups.size() + ", assignGroupIds = " + assignedGroupIds.size());
            List<ClassGroupSummary> classGroupSummaries = allGroups.stream()
                    .map(group -> {
                        boolean isAssigned = assignedGroupIds.contains(group.getId());
                        log.info("getAllClassGroup: " + group.getGroupName() + ", isAssigned: " + isAssigned);
                        return new ClassGroupSummary(
                                group.getId(),
                                group.getGroupName(),
                                isAssigned
                        );
                    })
                    .collect(Collectors.toList());

            return classGroupSummaries;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch class groups", e);
        }
    }

    @Override
    public void addClassGroup(Long classId, Long groupId) {
        try {
            Optional<ClassGroup> existingClassGroup = classGroupRepo.findByClassIdAndGroupId(classId, groupId);
            if (existingClassGroup.isPresent()) {
                System.out.println("Class group with classId " + classId + " and groupId " + groupId + " already exists.");
                return;
            }
            ClassGroup classGroup = new ClassGroup();
            classGroup.setClassId(classId);
            classGroup.setGroupId(groupId);
            classGroupRepo.save(classGroup);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add class group", e);
        }
    }

    @Override
    public void deleteClassGroup(Long classId, Long groupId) {
        try {
            Optional<ClassGroup> classGroupOptional = classGroupRepo.findByClassIdAndGroupId(classId, groupId);
            if (classGroupOptional.isPresent()) {
                classGroupRepo.delete(classGroupOptional.get());
                System.out.println("Class group with classId " + classId + " and groupId " + groupId + " has been deleted.");
            } else {
                System.out.println("Class group with classId " + classId + " and groupId " + groupId + " does not exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete class group", e);
        }
    }

}

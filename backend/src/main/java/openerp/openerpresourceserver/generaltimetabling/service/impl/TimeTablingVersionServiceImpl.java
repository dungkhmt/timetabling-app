package openerp.openerpresourceserver.generaltimetabling.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingTimeTableVersion;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingVersionRepo;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingVersionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TimeTablingVersionServiceImpl implements TimeTablingVersionService {
    
    private final TimeTablingVersionRepo timeTablingVersionRepo;

    @Override
    public TimeTablingTimeTableVersion createVersion(String name, String status, String semester, String userId) {
        log.info("Creating new timetabling version with name: {}, status: {}, semester: {}, userId: {}", 
                name, status, semester, userId);
        
        TimeTablingTimeTableVersion version = new TimeTablingTimeTableVersion();
        version.setName(name);
        version.setStatus(status);
        version.setSemester(semester);
        version.setCreatedByUserId(userId);
        // ID sẽ được tự động tạo bởi cơ sở dữ liệu
        
        return timeTablingVersionRepo.save(version);
    }

    @Override
    public List<TimeTablingTimeTableVersion> getAllVersionsBySemesterAndName(String semester, String name) {
        log.info("Fetching timetabling versions by semester: {} and name containing: {}", semester, name);
        return timeTablingVersionRepo.findBySemesterAndNameContaining(semester, name);
    }

    @Override
    public List<TimeTablingTimeTableVersion> getAllVersionsByName(String name) {
        log.info("Fetching all timetabling versions with name containing: {}", name);
        return timeTablingVersionRepo.findByNameContaining(name);
    }

    @Override
    public List<TimeTablingTimeTableVersion> getAllVersions() {
        log.info("Fetching all timetabling versions");
        return timeTablingVersionRepo.findAll();
    }

    @Override
    public void deleteVersion(Long id) {
        log.info("Deleting timetabling version with id: {}", id);
        timeTablingVersionRepo.deleteById(id);
    }

    @Override
    public TimeTablingTimeTableVersion updateVersion(Long id, String name, String status) {
        log.info("Updating timetabling version with id: {}, name: {}, status: {}", 
                id, name, status);
        
        TimeTablingTimeTableVersion existingVersion = timeTablingVersionRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên bản thời khóa biểu với ID: " + id));
        
        existingVersion.setName(name);
        existingVersion.setStatus(status);

        return timeTablingVersionRepo.save(existingVersion);
    }
}
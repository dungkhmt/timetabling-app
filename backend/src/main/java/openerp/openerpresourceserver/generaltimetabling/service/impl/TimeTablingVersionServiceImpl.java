package openerp.openerpresourceserver.generaltimetabling.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingTimeTableVersion;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingClassRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingVersionRepo;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingClassService;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TimeTablingVersionServiceImpl implements TimeTablingVersionService {
    
    private final TimeTablingVersionRepo timeTablingVersionRepo;

    @Autowired
    private TimeTablingClassService timeTablingClassService;

    @Autowired
    TimeTablingClassRepo timeTablingClassRepo;

    @Override
    @Transactional
    public TimeTablingTimeTableVersion createVersion(String name, String status, String semester, String userId) {
        log.info("Creating new timetabling version with name: {}, status: {}, semester: {}, userId: {}", 
                name, status, semester, userId);
        
        TimeTablingTimeTableVersion version = new TimeTablingTimeTableVersion();
        version.setName(name);
        version.setStatus(status);
        version.setSemester(semester);
        version.setCreatedByUserId(userId);
        // ID sẽ được tự động tạo bởi cơ sở dữ liệu
        
        version = timeTablingVersionRepo.save(version);

        // create class-segments of classes of the semester
        List<TimeTablingClass> CLS = timeTablingClassRepo.findAllBySemester(semester);
        for(TimeTablingClass cls: CLS){
            TimeTablingClassSegment cs = timeTablingClassService.createClassSegment(cls.getId(),cls.getCrew(),cls.getDuration(),version.getId());
        }
        return version;
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
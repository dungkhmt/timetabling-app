package openerp.openerpresourceserver.generaltimetabling.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingBatch;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingTimeTableVersion;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingBatchRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingClassRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingVersionRepo;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingClassService;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class TimeTablingVersionServiceImpl implements TimeTablingVersionService {
    
    private final TimeTablingVersionRepo timeTablingVersionRepo;

    @Autowired
    private TimeTablingClassService timeTablingClassService;

    @Autowired
    private TimeTablingClassRepo timeTablingClassRepo;

    @Autowired
    private TimeTablingBatchRepo timeTablingBatchRepo;

    @Override
    @Transactional
    public TimeTablingTimeTableVersion createVersion(String name, String status, String semester, String userId, Integer numOfSlot, Long batchId) {
        log.info("createVersion, Creating new timetabling version with name: {}, status: {}, semester: {}, userId: {}",
                name, status, semester, userId);
        TimeTablingBatch batch = timeTablingBatchRepo.findById(batchId).orElse(null);
        if(batch == null) return null;

        TimeTablingTimeTableVersion version = new TimeTablingTimeTableVersion();
        version.setName(name);
        version.setStatus(status);
        version.setSemester(batch.getSemester());
        version.setCreatedByUserId(userId);
        version.setNumberSlotsPerSession(numOfSlot);
        version.setBatchId(batchId);
        version.setCreatedStamp(new Date());
        // ID sẽ được tự động tạo bởi cơ sở dữ liệu
        
        version = timeTablingVersionRepo.save(version);

        // create class-segments of classes of the semester
        //List<TimeTablingClass> CLS = timeTablingClassRepo.findAllBySemester(semester);
        List<TimeTablingClass> CLS = timeTablingClassRepo.findAllByBatchId(batchId);
        log.info("createVersion, classes of batch " + batchId + " -> CLS.sz = " + CLS.size());
        for(TimeTablingClass cls: CLS){
            TimeTablingClassSegment cs = timeTablingClassService.createClassSegment(cls.getId(),cls.getCrew(),cls.getDuration(),version.getId());
            log.info("createVersion, created class-segment " + cs.getId());
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
    public List<TimeTablingTimeTableVersion> getAllVersionsByBatchId(Long batchId) {
        List<TimeTablingTimeTableVersion> res = timeTablingVersionRepo.findAllByBatchId(batchId);
        return res;
    }

    @Override
    public List<TimeTablingTimeTableVersion> getAllVersions() {
        log.info("Fetching all timetabling versions");
        return timeTablingVersionRepo.findAll();
    }

    @Override
    public boolean approveVersion(Long id) {
        TimeTablingTimeTableVersion version = timeTablingVersionRepo.findById(id).orElse(null);
        if(version == null) return false;
        List<TimeTablingTimeTableVersion> versions = timeTablingVersionRepo.findAllByBatchId(version.getBatchId());
        for(TimeTablingTimeTableVersion ver: versions){
            ver.setStatus(TimeTablingTimeTableVersion.STATUS_DRAFT);
        }
        version.setStatus(TimeTablingTimeTableVersion.STATUS_PUBLISHED);
        timeTablingVersionRepo.saveAll(versions);

        return true;
    }

    @Override
    public void deleteVersion(Long id) {
        log.info("Deleting timetabling version with id: {}", id);
        timeTablingVersionRepo.deleteById(id);
    }

    @Override
    @Transactional
    public TimeTablingTimeTableVersion updateVersion(Long id, String name, String status, Integer numberSlotsPerSession) {
        log.info("Updating timetabling version with id: {}, name: {}, status: {}, numberSlotsPerSession: {}", 
                id, name, status, numberSlotsPerSession);
        
        TimeTablingTimeTableVersion version = timeTablingVersionRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Version not found with id: " + id));
        
        if (name != null && !name.isEmpty()) {
            version.setName(name);
        }
        if (status != null && !status.isEmpty()) {
            version.setStatus(status);
        }
        if (numberSlotsPerSession != null) {
            version.setNumberSlotsPerSession(numberSlotsPerSession);
        }
        
        return timeTablingVersionRepo.save(version);
    }
}
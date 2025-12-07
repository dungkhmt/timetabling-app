package openerp.openerpresourceserver.generaltimetabling.service;

import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingTimeTableVersion;
import java.util.List;

public interface TimeTablingVersionService {

    TimeTablingTimeTableVersion createVersion(String name, String status, String semester, String userId, Integer numberSlotsPerSession, Long batchId);
    
    TimeTablingTimeTableVersion updateVersion(Long id, String name, String status, Integer numberSlotsPerSession);

    void deleteVersion(Long id);
    
    List<TimeTablingTimeTableVersion> getAllVersionsBySemesterAndName(String semester, String name);
    
    List<TimeTablingTimeTableVersion> getAllVersionsByName(String name);
    List<TimeTablingTimeTableVersion> getAllVersionsByBatchId(Long batchId);

    List<TimeTablingTimeTableVersion> getAllVersions();

    boolean approveVersion(Long id);
    
}
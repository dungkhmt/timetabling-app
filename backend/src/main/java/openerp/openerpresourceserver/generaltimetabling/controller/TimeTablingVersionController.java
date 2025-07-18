package openerp.openerpresourceserver.generaltimetabling.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.generaltimetabling.model.dto.TimeTableVersionRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.UpdateVersionRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingTimeTableVersion;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingClassService;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/timetabling-versions")
@AllArgsConstructor
@Slf4j
public class TimeTablingVersionController {

    private final TimeTablingVersionService timeTablingVersionService;
    @Autowired
    private TimeTablingClassService timeTablingClassService;    
    
    @PostMapping("/create")
    public ResponseEntity<TimeTablingTimeTableVersion> createVersion(Principal principal, @RequestBody TimeTableVersionRequest request) {
        log.info("Received request to create new timetabling version: {}", request);
        
        if (request.getName() == null || request.getStatus() == null || request.getSemester() == null || request.getUserId() == null) {
            log.error("Missing required fields in request");
            return ResponseEntity.badRequest().build();
        }
        Long batchId = Long.parseLong(request.getBatchId());
        TimeTablingTimeTableVersion createdVersion = timeTablingVersionService.createVersion(
            request.getName(), 
            request.getStatus(), 
            request.getSemester(), 
            //request.getUserId(),
                principal.getName(),
                request.getNumberSlotsPerSession(),
                batchId
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVersion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVersion(@PathVariable Long id) {
        log.info("Received request to delete timetabling version with id: {}", id);
        
        try {
            timeTablingVersionService.deleteVersion(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting timetabling version with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/get-version-of-batch")
    public ResponseEntity<?> getVersionsOfBatch(Principal principal,
                                                @RequestParam(required = false) Long batchId){
        List<TimeTablingTimeTableVersion> res = timeTablingVersionService.getAllVersionsByBatchId(batchId);
        return ResponseEntity.ok().body(res);
    }
    @GetMapping("/")
    public ResponseEntity<List<TimeTablingTimeTableVersion>> getVersions(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false, defaultValue = "") String name) {
        
        if ((semester == null || semester.isEmpty()) && (name == null || name.isEmpty())) {
            log.info("Fetching all timetabling versions");
            List<TimeTablingTimeTableVersion> versions = timeTablingVersionService.getAllVersions();
            return ResponseEntity.ok(versions);
        } else if (semester == null || semester.isEmpty()) {
            log.info("Fetching all timetabling versions with name containing: {}", name);
            List<TimeTablingTimeTableVersion> versions = timeTablingVersionService.getAllVersionsByName(name);
            return ResponseEntity.ok(versions);
        } else {
            log.info("Fetching timetabling versions for semester: {} and name containing: {}", semester, name);
            List<TimeTablingTimeTableVersion> versions = timeTablingVersionService.getAllVersionsBySemesterAndName(semester, name);
            return ResponseEntity.ok(versions);
        }
    }    
    
    @PutMapping("/{id}")
    public ResponseEntity<TimeTablingTimeTableVersion> updateVersion(
            @PathVariable Long id,
            @RequestBody UpdateVersionRequest request) {
        log.info("Received request to update timetabling version with id {}: {}", id, request);
        
        // Name and status can be optional for updates, but numberSlotsPerSession might also be optional
        // Depending on requirements, you might want to validate if at least one field is present for an update.
        // For now, we assume that if they are provided, they should be updated.

        try {
            TimeTablingTimeTableVersion updatedVersion = timeTablingVersionService.updateVersion(
                id, 
                request.getName(), 
                request.getStatus(),
                request.getNumberSlotsPerSession() // Pass the new field
            );
            return ResponseEntity.ok(updatedVersion);
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument updating timetabling version with id: {}", id, e);
            return ResponseEntity.badRequest().body(null); // Or a more specific error response
        } catch (Exception e) {
            log.error("Error updating timetabling version with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

package openerp.openerpresourceserver.generaltimetabling.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingTimeTableVersion;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingClassService;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/timetabling-versions")
@AllArgsConstructor
@Slf4j
public class TimeTablingVersionController {

    private final TimeTablingVersionService timeTablingVersionService;
    @Autowired
    private TimeTablingClassService timeTablingClassService;


    @PostMapping("/create")
    public ResponseEntity<TimeTablingTimeTableVersion> createVersion(@RequestBody Map<String, String> payload) {
        log.info("Received request to create new timetabling version: {}", payload);
        
        String name = payload.get("name");
        String status = payload.get("status");
        String semester = payload.get("semester");
        String userId = payload.get("userId");
        
        if (name == null || status == null || semester == null || userId == null) {
            log.error("Missing required fields in request");
            return ResponseEntity.badRequest().build();
        }
        
        TimeTablingTimeTableVersion createdVersion = timeTablingVersionService.createVersion(name, status, semester, userId);
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
            @RequestBody Map<String, String> payload) {
        log.info("Received request to update timetabling version with id {}: {}", id, payload);
        
        String name = payload.get("name");
        String status = payload.get("status");
        
        if (name == null || status == null) {
            log.error("Missing required fields in update request");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            TimeTablingTimeTableVersion updatedVersion = timeTablingVersionService.updateVersion(id, name, status);
            return ResponseEntity.ok(updatedVersion);
        } catch (Exception e) {
            log.error("Error updating timetabling version with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

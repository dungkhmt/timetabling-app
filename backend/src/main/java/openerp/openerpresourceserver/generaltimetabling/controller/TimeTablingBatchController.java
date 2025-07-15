package openerp.openerpresourceserver.generaltimetabling.controller;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputCreateTimeTablingBatch;
import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingBatch;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingBatchRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;
@Log4j2
@RestController
@RequestMapping("/timetabling-batch")
public class TimeTablingBatchController {
    @Autowired
    private TimeTablingBatchRepo timeTablingBatchRepo;

    @PostMapping("/add-batch")
    public ResponseEntity<?> addBatch(Principal principal, @RequestBody ModelInputCreateTimeTablingBatch m){
        TimeTablingBatch batch = new TimeTablingBatch();
        Long id = timeTablingBatchRepo.getNextIdValue();
        batch.setId(id);
        batch.setName(m.getBatchName());
        batch.setCreatedStamp(new Date());
        batch.setCreatedByUserId(principal.getName());
        batch.setSemester(m.getSemester());
        batch = timeTablingBatchRepo.save(batch);
        return ResponseEntity.ok().body(batch);
    }
    @GetMapping("/get-all/{semester}")
    public ResponseEntity<?> getAllBatchByUser(Principal principal, @PathVariable String semester){
        List<TimeTablingBatch> res = timeTablingBatchRepo.findAllByCreatedByUserIdAndSemester(principal.getName(),semester);
        log.info("getAllBatchByUser, user = " + principal.getName() + " res.sz = " + res.size());
        return ResponseEntity.ok().body(res);
    }
}

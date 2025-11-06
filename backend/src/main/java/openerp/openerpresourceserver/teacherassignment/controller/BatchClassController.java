package openerp.openerpresourceserver.teacherassignment.controller;

import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.BatchClass;
import openerp.openerpresourceserver.teacherassignment.service.BatchClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/teacher-assignment-batch-class")
@Slf4j
public class BatchClassController {

    @Autowired
    private BatchClassService batchClassService;

    @GetMapping("/get-all-classes-by-batch/{batchId}")
    public ResponseEntity<List<BatchClass>> getAllClassesByBatch(@PathVariable Long batchId) {
        try {
            var batchClassList = batchClassService.findAllByBatchId(batchId);
            if (batchClassList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(batchClassList, HttpStatus.OK);
            // return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create-batch-class/{batchId}/{classId}")
    public ResponseEntity<BatchClass> createBatchClass( @PathVariable Long batchId, @PathVariable Long classId) {
//        log.info("=== CREATE BATCH CLASS ENDPOINT CALLED ===");
//        log.info("batchId: {}, classId: {}", batchId, classId);

        try {
            BatchClass batchClass = batchClassService.createBatchClass(batchId, classId);
//
//            log.info("Successfully created BatchClass: {}", batchClass);
//
            return ResponseEntity.ok(batchClass);
//             return new ResponseEntity<>(HttpStatus.NO_CONTENT);


        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

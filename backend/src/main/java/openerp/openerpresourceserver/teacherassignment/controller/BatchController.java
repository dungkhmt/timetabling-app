package openerp.openerpresourceserver.teacherassignment.controller;

import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
import openerp.openerpresourceserver.teacherassignment.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RequestMapping("/teacher-assignment-batch")
public class BatchController {
    @Autowired
    private BatchService batchService;

    @GetMapping("/get-batch-semester/{semester}")
    public ResponseEntity<List<Batch>> getAllSemesters(@PathVariable String semester) {
        try {
            List<Batch> batchList = batchService.getAllBatchBySemester(semester);
            if (batchList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(batchList, HttpStatus.OK);
            // return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
//            logger.error("Error fetching semesters: ", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}

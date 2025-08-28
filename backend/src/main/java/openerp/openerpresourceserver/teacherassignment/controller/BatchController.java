package openerp.openerpresourceserver.teacherassignment.controller;

import jakarta.validation.Valid;
import openerp.openerpresourceserver.teacherassignment.model.dto.BatchCreateDto;
import openerp.openerpresourceserver.teacherassignment.model.dto.BatchDto;
import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
import openerp.openerpresourceserver.teacherassignment.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/teacher-assignment-batch")
public class BatchController {
    @Autowired
    private BatchService batchService;

    @GetMapping("/get-batch-semester/{semester}")
    public ResponseEntity<List<BatchDto>> getAllSemesters(@PathVariable String semester) {
        try {
            List<BatchDto> batchList = batchService.getAllBatchBySemester(semester);
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

    @PostMapping("/create-batch")
    public ResponseEntity<Batch> createBatch(@Valid @RequestBody BatchCreateDto requestDto) {
        try {
            Batch batch = requestDto.toEntity();
            Batch createdBatch = batchService.createBatch(batch);
            return new ResponseEntity<>(createdBatch, HttpStatus.OK);

//            return ResponseEntity.ok(batch);
//             return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-batch/{batchId}")
    public ResponseEntity<BatchDto> getBatchById(@PathVariable Long batchId) {
        BatchDto batchDto = batchService.findById(batchId);
        if (batchDto != null) {
            return new ResponseEntity<>(batchDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

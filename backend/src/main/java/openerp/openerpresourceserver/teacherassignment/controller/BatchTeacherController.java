package openerp.openerpresourceserver.teacherassignment.controller;

import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.teacherassignment.model.entity.relation.BatchClass;
import openerp.openerpresourceserver.teacherassignment.model.entity.relation.BatchTeacher;
import openerp.openerpresourceserver.teacherassignment.service.BatchTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/teacher-assignment-batch-teacher")
@Slf4j
public class BatchTeacherController {

    @Autowired
    private BatchTeacherService batchTeacherService;

    @GetMapping("/get-all-teachers-by-batch/{batchId}")
    public ResponseEntity<List<BatchTeacher>> getAllTeachersByBatch(@PathVariable Long batchId) {
        List<BatchTeacher> batchTeacherList = batchTeacherService.getBatchTeacherByBatchId(batchId);
        return new ResponseEntity<>(batchTeacherList, HttpStatus.OK);
    }

    @PostMapping("/create-batch-teacher/{batchId}/{teacherUserId}")
    public ResponseEntity<BatchTeacher> createBatchTeacher( @PathVariable Long batchId, @PathVariable String teacherUserId) {
        try {
            BatchTeacher batchTeacher = batchTeacherService.createBatchTeacher(batchId, teacherUserId);
            return ResponseEntity.ok(batchTeacher);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

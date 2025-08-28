package openerp.openerpresourceserver.teacherassignment.controller;

import openerp.openerpresourceserver.teacherassignment.model.dto.AssignClassRequest;
import openerp.openerpresourceserver.teacherassignment.model.dto.OpenedClassDto;

import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;
import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeBatchClass;
import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.BatchClass;
import openerp.openerpresourceserver.teacherassignment.repo.BatchClassRepo;
import openerp.openerpresourceserver.teacherassignment.repo.BatchRepo;
import openerp.openerpresourceserver.teacherassignment.service.BatchClassService;
import openerp.openerpresourceserver.teacherassignment.service.OpenedClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/teacher-assignment-opened-class")
public class OpenedClassController {

    @Autowired
    private OpenedClassService openedClassService;

    @Autowired
    private BatchClassService batchClassService;

    @GetMapping("/get-all-classes-by-semester/{semester}")
    public ResponseEntity<List<OpenedClassDto>> getAllClassesBySemester(@PathVariable String semester) {

//        try {
            List<OpenedClassDto> openedClassList = openedClassService.findAllBySemester(semester);
            if (openedClassList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(openedClassList, HttpStatus.OK);
//             return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//        } catch (Exception e) {
////            logger.error("Error fetching semesters: ", e);
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
    }

    @GetMapping("/get-all-distinct-semester")
    public ResponseEntity<List<String>> getAllDistinctSemester() {

        try {
            List<String> semesterList = openedClassService.getAllDistinctSemester();
            if (semesterList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(semesterList, HttpStatus.OK);
            // return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
//            logger.error("Error fetching semesters: ", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/get-all-classes-by-batch/{batchId}")
    public ResponseEntity<List<OpenedClassDto>> getAllClassesByBatch(@PathVariable Long batchId) {
        try {
            var openedClassList = openedClassService.findAllByBatchId(batchId);
            if (openedClassList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(openedClassList, HttpStatus.OK);
            // return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/add-class-to-batch-based-on-courses")
    public ResponseEntity<Void> addClassToBatchBasedOnCourses(@RequestBody AssignClassRequest request) {

        request.getCourseIds().forEach(courseId -> {
            List<OpenedClass> classes = openedClassService.findAllBySemesterAndCourseId(request.getSemester(), courseId);

            classes.forEach(openedClass -> {
                BatchClass batchClass = batchClassService.createBatchClass(request.getBatchId(),openedClass.getClassId() );

            });

        });
//        List<OpenedClass> classes = openedClassService.findAllBySemesterAndCourseId(request.getSemester(), request.getCourseId());


        try {
            // Call the service method to perform the operation
            // openedClassService.addClassToBatchBasedOnCourses();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

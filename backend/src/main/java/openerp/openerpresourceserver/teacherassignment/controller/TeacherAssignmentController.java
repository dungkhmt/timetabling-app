package openerp.openerpresourceserver.teacherassignment.controller;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.teacherassignment.algorithm.TeacherAssignment;
import openerp.openerpresourceserver.teacherassignment.model.dto.BatchDto;
import openerp.openerpresourceserver.teacherassignment.model.dto.OpenedClassDto;
import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;
import openerp.openerpresourceserver.teacherassignment.service.BatchService;
import openerp.openerpresourceserver.teacherassignment.service.OpenedClassService;
import openerp.openerpresourceserver.thesisdefensejuryassignment.dto.TeacherDto;
import openerp.openerpresourceserver.thesisdefensejuryassignment.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher-assignment")
@Slf4j
public class TeacherAssignmentController {

    @Autowired
    private BatchService batchService;

    @Autowired
    private OpenedClassService openedClassService;



    @PostMapping("/assign-classes-for-teachers/{semester}")
    public ResponseEntity<Map<Long, String>> assignClassForTeacher(@PathVariable String semester) {
        String schoolId = "TCNTT";

        Map<Long, String> openedClassesList = openedClassService.assignmentTeacher(semester, schoolId);
        if (!openedClassesList.isEmpty()) {
            log.info("have solved");
        } else {
            log.info("no solved");
        }
        return new ResponseEntity<>(openedClassesList, HttpStatus.OK);
//
//        // Lọc các opened class có schoolId là "TCNTT"
//        List<OpenedClass> filteredClasses = openedClassesList.stream()
//                .filter(openedClass -> openedClass.getStudyingCourse() != null
//                        && "TCNTT".equals(openedClass.getStudyingCourse().getSchoolId()))
//                .toList();
//        List<TeacherDto> teacherList = teacherService.getAllTeacher();
//        Map<Long, String> res = TeacherAssignment.assignment(filteredClasses, teacherList);
//        if ( res != null){
//            log.info("have solved");
//        }

//        log.info("Found {} classes with schoolId TCNTT for semester {}", filteredClasses.size(), semester);

//        try {
//            // Call the service method to perform the operation
//            // openedClassService.addClassToBatchBasedOnCourses();
//            return new ResponseEntity<>(HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
    }
}

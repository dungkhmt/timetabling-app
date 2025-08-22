package openerp.openerpresourceserver.generaltimetabling.controller;

import jakarta.validation.Valid;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ClassroomDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.SemesterDto;
import openerp.openerpresourceserver.generaltimetabling.model.entity.OpenBatch;
import openerp.openerpresourceserver.generaltimetabling.model.entity.OpenClassPlan;
import openerp.openerpresourceserver.generaltimetabling.service.ModuleService;
import openerp.openerpresourceserver.generaltimetabling.service.OpenClassPlanService;
import openerp.openerpresourceserver.generaltimetabling.service.StudyingCourseService;
import openerp.openerpresourceserver.labtimetabling.controller.ClassPlanController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/open-class-plan")

public class OpenClassPlanController {
//    private static final Logger logger = LoggerFactory.getLogger(OpenClassPlanController.class);


    @Autowired
    private OpenClassPlanService openClassPlanService;

    @Autowired
    private StudyingCourseService studyingCourseService;


    @PostMapping("/get-all-class-plan-by-semester/{semester}")
    public ResponseEntity<List<OpenClassPlan>> getAllOpenBatch(@PathVariable String semester) {
        try {

//            logger.info("=== DEBUG START: getAllClassPlanBySemester ===");
//            logger.info("Semester parameter: {}", semester);
            List<OpenClassPlan> openClassPlanList = openClassPlanService.getAllBySemester(semester);

//            logger.info("OpenClassPlan list size: {}", openClassPlanList.size());


//
            if (openClassPlanList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(openClassPlanList, HttpStatus.OK);
//            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-semesters")
    public ResponseEntity<List<String>> getAllSemesters() {
        try {
            List<String> semesters = openClassPlanService.getAllSemesters();
            if (semesters.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(semesters, HttpStatus.OK);
        } catch (Exception e) {
//            logger.error("Error fetching semesters: ", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}

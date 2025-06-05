package openerp.openerpresourceserver.generaltimetabling.controller.general;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.exception.InvalidFieldException;
import openerp.openerpresourceserver.generaltimetabling.exception.NotFoundException;
import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateTimeTablingClassDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateSubClassDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.UpdateTimeTablingClassFromPlanDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ModelInputGenerateClassSegmentFromClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.UpdateTimeTablingClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.UpdatePlanClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.BulkMakeGeneralClassDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.ClearPlanClassInputModel;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.CreateSingleClassOpenDto;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.PlanGeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.service.PlanGeneralClassService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;

@Log4j2
@RequestMapping("/plan-general-classes")
@RestController
@AllArgsConstructor
public class PlanGeneralClassController {
    private PlanGeneralClassService planClassService;

    @ExceptionHandler(InvalidFieldException.class)
    public ResponseEntity resolveInvalidFieldException(InvalidFieldException e) {
        return ResponseEntity.status(410).body(e.getErrorMessage());
    }


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity resolveNotFoundException(NotFoundException e) {
        return ResponseEntity.status(410).body(e.getCustomMessage());
    }


    @PostMapping("/make-class")
    public ResponseEntity<?> requestMakeClass(@RequestBody CreateTimeTablingClassDto request) {
            //return ResponseEntity.ok(planClassService.makeClass(request));
            return null;
    }

    @PostMapping("/make-subclass")
    public ResponseEntity<?> requestMakeSubClass(@RequestBody CreateSubClassDto request) {
        //return ResponseEntity.ok(planClassService.makeSubClass(request));
        return ResponseEntity.ok(planClassService.makeSubClass(request));
    }

    @PostMapping("/make-multiple-classes")
    public ResponseEntity<List<TimeTablingClass>> requestMakeMultipleClasses(@RequestBody BulkMakeGeneralClassDto request) {
        return ResponseEntity.ok(planClassService.createMultipleClasses(request));
    }

    @PostMapping("/generate-class-segment-from-classes")
    public ResponseEntity<?> generateClassSegmentFromClass(Principal principal, @RequestBody ModelInputGenerateClassSegmentFromClass I){
        int cnt = 0;
        return ResponseEntity.ok().body(cnt);
    }
    
    @PostMapping("/generate-classes-from-plan")
    public ResponseEntity<?> generateClassesFromPlan(Principal principal, @RequestBody UpdateTimeTablingClassFromPlanDto I){
        log.info("generate-classes-from-plan, semester = " + I.getSemester());
        //List<GeneralClass> classes = planClassService.generateClassesFromPlan(I);
        List<TimeTablingClass> classes = planClassService.generateTimeTablingClassFromPlan(I);
        if(classes == null) return ResponseEntity.status(401).body("Cannot generated class from plab");
        return ResponseEntity.ok().body(classes);
    }

    @GetMapping("/")
    public ResponseEntity<List<PlanGeneralClass>> requestGetPlanClasses(@RequestParam("semester") String semester) {
        return ResponseEntity.ok(planClassService.getAllPlanClasses(semester));
    }


    @GetMapping("/view-class")
    public ResponseEntity requestViewPlanClass(@RequestParam("planClassId") Long planClassId){
        //return ResponseEntity.ok(planClassService.getPlanClassById(semester, planClassId));
        List<TimeTablingClass> timeTablingClasses = planClassService.getClassOfPlan(planClassId);
        return ResponseEntity.ok().body(timeTablingClasses);
    }

    @PostMapping("/update-general-class")
    public ResponseEntity<?> requestUpdateTimeTablingClass(@RequestBody UpdateTimeTablingClassRequest request) {
        return  ResponseEntity.ok(planClassService.updateTimeTablingClass(request.getTimetablingClass()));
    }

    @PostMapping("/update-plan-class")
    public ResponseEntity<?> requestUpdatePlanClass(@RequestBody UpdatePlanClassRequest request) {
        return  ResponseEntity.ok(planClassService.updatePlanClass(request.getPlanClass()));
    }
    @PostMapping("/clear-plan")
    public ResponseEntity<?> clearPlanClass(Principal principal, @RequestBody ClearPlanClassInputModel I){
        log.info("clearPlanClass semester = " + I.getSemesterId());
        planClassService.clearPlanClass(I.getSemesterId());
        return ResponseEntity.ok().body("OK");
    }

    @DeleteMapping("/delete-by-ids")
    public ResponseEntity<?> requestDeletePlanClasses(@RequestParam("planClassId") List<Long> planClassIds){
        return ResponseEntity.ok(planClassService.deleteClassesByIds(planClassIds));
    }
    
    @DeleteMapping("/")
    public ResponseEntity<PlanGeneralClass> requestDeletePlanClass(@RequestParam("planClassId") Long planClassId) {
        return ResponseEntity.ok(planClassService.deleteClassById(planClassId));
    }

    @PostMapping("/create-single")
    public ResponseEntity<PlanGeneralClass> createSingleClass(@RequestBody CreateSingleClassOpenDto planClass) {
        log.info("Received request to create single class: {}", planClass);
        try {
            if (planClass.getCreatedStamp() == null) {
                planClass.setCreatedStamp(new Date());
            }
            PlanGeneralClass createdClass = planClassService.createSingleClass(planClass);
            log.info("Successfully created class: {}", createdClass);
            return ResponseEntity.ok(createdClass); // Ensure the saved entity is returned
        } catch (InvalidFieldException e) {
            log.error("InvalidFieldException: {}", e.getErrorMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating single class: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}

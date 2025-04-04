package openerp.openerpresourceserver.generaltimetabling.controller.general;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.exception.InvalidFieldException;
import openerp.openerpresourceserver.generaltimetabling.exception.NotFoundException;
import openerp.openerpresourceserver.generaltimetabling.model.dto.MakeGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputCreateSubClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputGenerateClassesFromPlan;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ModelInputGenerateClassSegmentFromClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.UpdateGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.UpdatePlanClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.BulkMakeGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.ClearPlanClassInputModel;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.CreateSingleClassOpenRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.PlanGeneralClass;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingClassService;
import openerp.openerpresourceserver.generaltimetabling.service.impl.PlanGeneralClassService;
import org.aspectj.weaver.ast.Not;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;

@Log4j2
@RequestMapping("/plan-general-classes")
@Controller
@AllArgsConstructor
public class PlanGeneralClassController {
    private PlanGeneralClassService planClassService;
    private TimeTablingClassService timeTablingClassService;

    @ExceptionHandler(InvalidFieldException.class)
    public ResponseEntity resolveInvalidFieldException(InvalidFieldException e) {
        return ResponseEntity.status(410).body(e.getErrorMessage());
    }


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity resolveNotFoundException(NotFoundException e) {
        return ResponseEntity.status(410).body(e.getCustomMessage());
    }


    @PostMapping("/make-class")
    public ResponseEntity<?> requestMakeClass(@RequestBody MakeGeneralClassRequest request
    ) {
            //return ResponseEntity.ok(planClassService.makeClass(request));
            return null;
    }

    @PostMapping("/make-subclass")
    public ResponseEntity<?> requestMakeSubClass(@RequestBody ModelInputCreateSubClass request) {
        //return ResponseEntity.ok(planClassService.makeSubClass(request));
        return ResponseEntity.ok(planClassService.makeSubClassNew(request));

    }


    @PostMapping("/make-multiple-classes")
    public ResponseEntity<List<GeneralClass>> requestMakeMultipleClasses(@RequestBody BulkMakeGeneralClassRequest request) {
        return ResponseEntity.ok(planClassService.makeMultipleClasses(request));
    }
    @PostMapping("/generate-class-segment-from-classes")
    public ResponseEntity<?> generateClassSegmentFromClass(Principal principal, @RequestBody ModelInputGenerateClassSegmentFromClass I){
        int cnt = 0;//planClassService.generateClassSegmentFromClass(I);
        return ResponseEntity.ok().body(cnt);
    }
    @PostMapping("/generate-classes-from-plan")
    public ResponseEntity<?> generateClassesFromPlan(Principal principal, @RequestBody ModelInputGenerateClassesFromPlan I){
        log.info("generate-classes-from-plan, semester = " + I.getSemester());
        List<GeneralClass> classes = planClassService.generateClassesFromPlan(I);
        return ResponseEntity.ok().body(classes);
    }
    @GetMapping("/")
    public ResponseEntity<List<PlanGeneralClass>> requestGetPlanClasses(@RequestParam("semester") String semester) {
        return ResponseEntity.ok(planClassService.getAllClasses(semester));
    }


    @GetMapping("/view-class")
    public ResponseEntity requestViewPlanClass(@RequestParam("semester") String semester,
                                               @RequestParam("planClassId") Long planClassId){
        return ResponseEntity.ok(planClassService.getPlanClassById(semester, planClassId));
    }

    @PostMapping("/update-general-class")
    public ResponseEntity requestUpdateGeneralClass(@RequestBody UpdateGeneralClassRequest request) {
        return  ResponseEntity.ok(planClassService.updateGeneralClass(request.getGeneralClass()));
    }

    @PostMapping("/update-plan-class")
    public ResponseEntity requestUpdatePlanClass(@RequestBody UpdatePlanClassRequest request) {
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
    public ResponseEntity<PlanGeneralClass> createSingleClass(@RequestBody CreateSingleClassOpenRequest planClass) {
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

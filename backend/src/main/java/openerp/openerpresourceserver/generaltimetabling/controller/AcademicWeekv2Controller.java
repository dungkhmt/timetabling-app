package openerp.openerpresourceserver.generaltimetabling.controller;

import lombok.AllArgsConstructor;
import openerp.openerpresourceserver.generaltimetabling.model.entity.AcademicWeek;
import openerp.openerpresourceserver.generaltimetabling.service.AcademicWeekService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/academic-weeks-v2")
@AllArgsConstructor
public class AcademicWeekv2Controller {
    
    private final AcademicWeekService academicWeekService;
    
    @PostMapping
    public ResponseEntity<List<AcademicWeek>> createAcademicWeeks(@RequestBody Map<String, Object> requestData) {
        String semester = (String) requestData.get("semester");
        String startDate = (String) requestData.get("startDate");
        int startWeek = ((Number) requestData.get("startWeek")).intValue();
        int numberOfWeeks = ((Number) requestData.get("numberOfWeeks")).intValue();
        
        List<AcademicWeek> weeks = academicWeekService.saveAcademicWeeksV2(semester, startDate, startWeek, numberOfWeeks);
        return ResponseEntity.ok(weeks);
    }
}

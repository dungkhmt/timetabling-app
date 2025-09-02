package openerp.openerpresourceserver.teacherassignment.controller;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.teacherassignment.model.dto.BatchDto;
import openerp.openerpresourceserver.teacherassignment.model.dto.OpenedClassDto;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher-assignment")
@Slf4j
public class TeacherAssignmentController {

    @Autowired
    private BatchService batchService;

    @Autowired
    private OpenedClassService openedClassService;

    @Autowired
    private TeacherService teacherService;

    @PostMapping("/assign-classes-for-teachers/{semester}")
    public ResponseEntity<Void> assignClassForTeacher(@PathVariable String semester) {

        List<BatchDto> batchList = batchService.getAllBatchBySemester(semester);

        List<List<OpenedClassDto>> openedClassLists = new ArrayList<>(batchList.size());

        List<List<TeacherDto>> teacherLists = new ArrayList<>(batchList.size());

        batchList.forEach(batch -> {
            List<OpenedClassDto> openedClassList = openedClassService.findAllByBatchId(batch.getId());
            openedClassLists.add(openedClassList);

            List<TeacherDto> teacherList = teacherService.getAllTeacherByBatchId(batch.getId());
            teacherLists.add(teacherList);

        });

        Loader.loadNativeLibraries();
        CpModel model = new CpModel();




        // Solve the model
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);

        Map<Long, String> assignmentResults = new HashMap<>();

        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            log.info("Solution found with objective value: {}", solver.objectiveValue());

            // Extract and print the solution
            for (Map.Entry<Long, String> entry : assignmentResults.entrySet()) {
                Long classId = entry.getKey();
                String teacherId = entry.getValue();
                log.info("Class ID {} assigned to Teacher ID {}", classId, teacherId);
            }
        } else {
            log.warn("No solution found for teacher-class assignment");
        }
        try {
            // Call the service method to perform the operation
            // openedClassService.addClassToBatchBasedOnCourses();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

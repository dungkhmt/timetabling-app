package openerp.openerpresourceserver.teacherassignment.controller;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
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

import java.util.*;

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

        List<TeacherDto> allTeachers = teacherService.getAllTeacher();

        batchList.forEach(batch -> {
            List<OpenedClassDto> openedClassList = openedClassService.findAllByBatchId(batch.getId());
            openedClassLists.add(openedClassList);

            List<TeacherDto> teacherList = teacherService.getAllTeacherByBatchId(batch.getId());
            teacherLists.add(teacherList);

        });

        // Tạo map để lưu index của từng teacher trong allTeachers (với ID là String)
        Map<String, Integer> teacherIndexMap = new HashMap<>();
        for (int i = 0; i < allTeachers.size(); i++) {
            teacherIndexMap.put(allTeachers.get(i).getId(), i);
        }


        Loader.loadNativeLibraries();
        CpModel model = new CpModel();

        // Tạo mảng biến int cho openedClassLists
        // mỗi phần tử là giá trị int của giáo viên phụ trách lớp học đó
        List<List<IntVar>> openedClassTeacherVars = new ArrayList<>();

        for (int batchIndex = 0; batchIndex < openedClassLists.size(); batchIndex++) {
            List<OpenedClassDto> batchClasses = openedClassLists.get(batchIndex);
            List<IntVar> batchVars = new ArrayList<>();

            for (int classIndex = 0; classIndex < batchClasses.size(); classIndex++) {
                // Tạo biến với giá trị từ 0 đến allTeachers.size() - 1
                IntVar teacherVar = model.newIntVar(0, allTeachers.size() - 1,
                        "teacher_batch_" + batchIndex + "_class_" + classIndex);

                batchVars.add(teacherVar);
            }

            openedClassTeacherVars.add(batchVars);
        }

        for (int batchIndex = 0; batchIndex < openedClassLists.size(); batchIndex++) {
            List<OpenedClassDto> batchClasses = openedClassLists.get(batchIndex);
            List<TeacherDto> batchTeachers = teacherLists.get(batchIndex);
            List<IntVar> batchVars = openedClassTeacherVars.get(batchIndex);

            for (int classIndex = 0; classIndex < batchClasses.size(); classIndex++) {
                IntVar teacherVar = batchVars.get(classIndex);

                // Chỉ những giáo viên có thể dạy môn học của lớp học này
                List<TeacherDto> eligibleTeachers = teacherService.getTeacherByCourseId(batchClasses.get(classIndex).getCourseId(), batchList.get(batchIndex).getId());
                int[] eligibleTeacherIndices = eligibleTeachers.stream()
                        .mapToInt(teacher -> teacherIndexMap.get(teacher.getId()))
                        .toArray();
                model.addAllowedAssignments(new IntVar[]{teacherVar}).addTuple(eligibleTeacherIndices);

            }
        }
        // Tạo biến để tính tổng số tín chỉ mỗi giáo viên phải dạy
        IntVar[] teacherTotalCredits = new IntVar[allTeachers.size()];
        for (int i = 0; i < allTeachers.size(); i++) {
            teacherTotalCredits[i] = model.newIntVar(0, Integer.MAX_VALUE, "teacher_credit_" + i);
        }

        // Thêm ràng buộc: tổng credit của mỗi giáo viên không vượt quá maxCredit
        for (int teacherIndex = 0; teacherIndex < allTeachers.size(); teacherIndex++) {
            TeacherDto teacher = allTeachers.get(teacherIndex);
            model.addLessOrEqual(teacherTotalCredits[teacherIndex], teacher.getMaxCredit());
        }



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

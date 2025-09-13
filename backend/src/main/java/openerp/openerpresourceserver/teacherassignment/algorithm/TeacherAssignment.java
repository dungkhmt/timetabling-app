package openerp.openerpresourceserver.teacherassignment.algorithm;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.teacherassignment.model.dto.OpenedClassDto;
import openerp.openerpresourceserver.teacherassignment.model.dto.TeacherCapacityDto;
import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;
import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.TeacherCapacity;
import openerp.openerpresourceserver.thesisdefensejuryassignment.dto.TeacherDto;
import openerp.openerpresourceserver.thesisdefensejuryassignment.entity.Teacher;

import java.util.*;

@Slf4j
public class TeacherAssignment {
    public static Map<Long, String> assignment(List<OpenedClass> openedClassesList, List<Teacher> teacherList){

        log.info("OpenedClassList Size: " + openedClassesList.size());
        log.info("TeacherList Size: " + teacherList.size());

        // 1) Thu thập tất cả courseId mà có ít nhất một GV dạy được
        Set<String> teachableCourseIds = new HashSet<>();
        for (Teacher t : teacherList) {
            if (t == null || t.getTeacherCapacityList() == null) continue;
            for (TeacherCapacity cap : t.getTeacherCapacityList()) {
                if (cap != null && cap.getId() != null && cap.getId().getCourseId() != null) {
                    teachableCourseIds.add(cap.getId().getCourseId());
                }
            }
        }
        // 2) Lọc danh sách lớp: giữ lại những lớp có courseId nằm trong teachableCourseIds
        List<OpenedClass> filteredClasses = new ArrayList<>();
        for (OpenedClass oc : openedClassesList) {
            if (oc != null && oc.getCourseId() != null && teachableCourseIds.contains(oc.getCourseId())) {
                filteredClasses.add(oc);
            }
        }

        log.info("Filtered OpenedClassList Size (teachable only): {}", filteredClasses.size());

        openedClassesList = filteredClasses;

        Loader.loadNativeLibraries();
        CpModel model = new CpModel();

        int numTeachers = teacherList.size();
        int numClasses = openedClassesList.size();

        // Tạo mảng 2 chiều biến 0-1: x[i][j] = 1 nếu giáo viên i dạy lớp j
        Literal[][] assignmentVars = new Literal[numTeachers][numClasses];
        for (int i = 0; i < numTeachers; i++) {
            for (int j = 0; j < numClasses; j++) {
                assignmentVars[i][j] = model.newBoolVar("teacher_" + i + "_class_" + j);
            }
        }
        // Ràng buộc 1: Mỗi lớp phải được gán cho đúng 1 giáo viên có thể dạy môn đó (cùng mã học phần)
        for (int openedClass = 0; openedClass < numClasses; openedClass++) {
            List<Literal> allowedTeachers = new ArrayList<>();
            for( int teacher =0 ;teacher < numTeachers; teacher++) {
                if(teacherList.get(teacher).getTeacherCapacityList() != null ){
                    for(TeacherCapacity cap : teacherList.get(teacher).getTeacherCapacityList()) {
                        if(Objects.equals(cap.getId().getCourseId(), openedClassesList.get(openedClass).getCourseId())) {
                            allowedTeachers.add(assignmentVars[teacher][openedClass]);
                        }
                    }
                }

            }
            model.addExactlyOne(allowedTeachers);
        }
//
//        // Ràng buộc 3: Tránh xung đột thời gian (nếu có thông tin thời gian)
//        // Giả sử TimeClassDto có thông tin về thời gian học
//        for (int i = 0; i < openedClassesList.size(); i++) {
//            for (int j = i + 1; j < openedClassesList.size(); j++) {
//                OpenedClassDto class1 = openedClassesList.get(i);
//                OpenedClassDto class2 = openedClassesList.get(j);
//
//                // Kiểm tra nếu hai lớp có thời gian trùng nhau
//                if (hasTimeConflict(class1, class2)) {
//                    // Đảm bảo cùng một giáo viên không dạy hai lớp trùng giờ
//                    model.addDifferent(classTeacherVars.get(i), classTeacherVars.get(j));
//                }
//            }
//        }

//        hàm mục tiêu hiện tại: số lớp nhiều nhất của giảng viên đạt min

        // Biến để theo dõi số lớp mỗi giáo viên dạy
        IntVar[] teacherLoads = new IntVar[numTeachers];
        for (int i = 0; i < numTeachers; i++) {
            List<Literal> classesTaught = new ArrayList<>();
            for (int j = 0; j < numClasses; j++) {
                classesTaught.add(assignmentVars[i][j]);
            }
            // Chuyển List<Literal> sang LinearArgument[]
            LinearArgument[] classesArray = classesTaught.toArray(new LinearArgument[0]);
            teacherLoads[i] = model.newIntVar(0, numClasses, "teacher_load_" + i);
            model.addEquality(teacherLoads[i], LinearExpr.sum(classesArray));
        }

        // Biến để biểu diễn số lớp tối đa mà bất kỳ giáo viên nào dạy
        IntVar maxLoad = model.newIntVar(0, numClasses, "max_load");
        model.addMaxEquality(maxLoad, teacherLoads);


        // Objective: Minimize the maximum number of classes assigned to any teacher
        model.minimize(maxLoad);

        // Solve the model
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);

        Map<Long, String> assignmentResult = new HashMap<>();

        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            log.info("Solution found with objective value: {}", solver.objectiveValue());

            // Extract and return the solution
            for (int j = 0; j < numClasses; j++) {
                for (int i = 0; i < numTeachers; i++) {
                    if (solver.value(assignmentVars[i][j]) == 1) {
                        String teacherId = teacherList.get(i).getId();
                        Long classId = openedClassesList.get(j).getClassId();
                        assignmentResult.put(classId, teacherId);
                        log.info("Class {} assigned to teacher {}", classId, teacherId);
                        break;
                    }
                }
            }
            return assignmentResult;

        } else {
            log.warn("No solution found for teacher-class assignment");
            return null;
        }
    }

}

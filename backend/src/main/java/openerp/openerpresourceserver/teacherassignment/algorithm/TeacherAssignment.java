package openerp.openerpresourceserver.teacherassignment.algorithm;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;
import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.TeacherCapacity;
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

        // mô hình hiện tại chỉ hỗ trợ phân lớp những học phần mà giảng viên có thể dạy
        // những học phần chưa có dữ liệu giảng viên dạy tạm thời bị bỏ qua

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
//        // Ràng buộc 2: Tránh xung đột thời gian (nếu có thông tin thời gian)
//        // Giả sử TimeClassDto có thông tin về thời gian học
        // --- RÀNG BUỘC TRÙNG GIỜ ---
        for (int j = 0; j < numClasses; j++) {
            for (int k = j + 1; k < numClasses; k++) {
                if (hasTimeConflict(openedClassesList.get(j), openedClassesList.get(k))) {
                    for (int i = 0; i < numTeachers; i++) {
                        // Không cho GV i dạy cả 2 lớp j & k
                        model.addAtMostOne(new Literal[]{
                                assignmentVars[i][j],
                                assignmentVars[i][k]
                        });
                    }
                }
            }
        }


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

    private static int parseToMinutes(String s) {
        if (s == null) return -1;
        String t = s.trim();
        if (t.isEmpty()) return -1;
        int h = Integer.parseInt(t.substring(0, t.length() - 2));
        int m = Integer.parseInt(t.substring(t.length() - 2));
        return h * 60 + m;
    }

    private static boolean isOverlap(String s1, String e1, String s2, String e2) {
        int aStart = parseToMinutes(s1);
        int aEnd   = parseToMinutes(e1);
        int bStart = parseToMinutes(s2);
        int bEnd   = parseToMinutes(e2);
        if (aStart < 0 || aEnd < 0 || bStart < 0 || bEnd < 0) return false;
        return aStart < bEnd && bStart < aEnd; // overlap
    }

    /** Kiểm tra 2 lớp có buổi nào trùng nhau không */
    private static boolean hasTimeConflict(OpenedClass c1, OpenedClass c2) {
        if (c1.getTimeClasses() == null || c2.getTimeClasses() == null) return false;
        for (var t1 : c1.getTimeClasses()) {
            for (var t2 : c2.getTimeClasses()) {
                if (isOverlap(t1.getStartTime(), t1.getEndTime(),
                        t2.getStartTime(), t2.getEndTime())) {
                    return true;
                }
            }
        }
        return false;
    }

}

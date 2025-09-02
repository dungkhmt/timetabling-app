//package openerp.openerpresourceserver.teacherassignment.algorithm;
//
//import com.google.ortools.linearsolver.MPSolver;
//import com.google.ortools.sat.CpSolverStatus;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import com.google.ortools.Loader;
//import com.google.ortools.sat.CpModel;
//import com.google.ortools.sat.CpSolver;
//import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
//import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;
//import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.BatchClass;
//import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.BatchTeacher;
//import openerp.openerpresourceserver.teacherassignment.repo.OpenedClassRepo;
//import openerp.openerpresourceserver.thesisdefensejuryassignment.entity.Teacher;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Slf4j
//@AllArgsConstructor
//public class ConstraintProgramming {
//
//    List<Batch> batchList;
//
//
//
//    public Map<Long, String> solve() {
//
//
//        Loader.loadNativeLibraries();
//        CpModel model = new CpModel();
//
////        // Create a mapping for easy access
//        Map<String, Teacher> teacherMap = teacherList.stream()
//                .collect(Collectors.toMap(Teacher::getId, teacher -> teacher));
////
//        Map<Long, OpenedClass> classMap = new HashMap<>();
//
//        Map<Long,Batch> batchMap = batchList.stream()
//                .collect(Collectors.toMap(Batch::getId, batch -> batch));
//
////
////        // Collect all classes from all batches
////        List<OpenedClass> allClasses = new ArrayList<>();
////        for (Batch batch : batches) {
////            if (batch.getBatchClasses() != null) {
////                for (BatchClass batchClass : batch.getBatchClasses()) {
////                    if (batchClass.getOpenedClass() != null) {
////                        allClasses.add(batchClass.getOpenedClass());
////                        classMap.put(batchClass.getOpenedClass().getClassId(), batchClass.getOpenedClass());
////                    }
////                }
////            }
////        }
////        // Create decision variables: assignment[teacherId][classId] = 1 if teacher is assigned to class
////        Map<String, Map<Long, IntVar>> assignmentVars = new HashMap<>();
////
////        for (Teacher teacher : teachers) {
////            Map<Long, IntVar> teacherAssignments = new HashMap<>();
////            for (OpenedClass openedClass : allClasses) {
////                IntVar var = model.newBoolVar("assign_" + teacher.getId() + "_" + openedClass.getClassId());
////                teacherAssignments.put(openedClass.getClassId(), var);
////            }
////            assignmentVars.put(teacher.getId(), teacherAssignments);
////        }
////
////        // Constraint 1: Each class must have exactly one teacher assigned
////        for (OpenedClass openedClass : allClasses) {
////            List<IntVar> classAssignments = new ArrayList<>();
////            for (Teacher teacher : teachers) {
////                classAssignments.add(assignmentVars.get(teacher.getId()).get(openedClass.getClassId()));
////            }
////            model.addEquality(LinearExpr.sum(classAssignments), 1);
////        }
////
////        // Constraint 2: Teacher cannot exceed maximum credit capacity
////        for (Teacher teacher : teachers) {
////            List<IntVar> teacherClassVars = new ArrayList<>();
////            List<Integer> classCredits = new ArrayList<>();
////
////            for (OpenedClass openedClass : allClasses) {
////                teacherClassVars.add(assignmentVars.get(teacher.getId()).get(openedClass.getClassId()));
////                // Assuming each class has a credit value (you might need to adjust this)
////                classCredits.add(3); // Default 3 credits per class
////            }
////
////            // Teacher's total assigned credits should not exceed maxCredit
////            model.addLessOrEqual(
////                    LinearExpr.scalProd(teacherClassVars, classCredits),
////                    teacher.getMaxCredit()
////            );
////        }
////
////        // Constraint 3: Teacher availability (from BatchTeacher)
////        for (Batch batch : batches) {
////            if (batch.getBatchTeachers() != null) {
////                for (BatchTeacher batchTeacher : batch.getBatchTeachers()) {
////                    String teacherId = batchTeacher.getTeacherId(); // Assuming BatchTeacher has getTeacherId()
////                    Teacher teacher = teacherMap.get(teacherId);
////
////                    if (teacher != null && batch.getBatchClasses() != null) {
////                        for (BatchClass batchClass : batch.getBatchClasses()) {
////                            if (batchClass.getOpenedClass() != null) {
////                                // If teacher is not available for this batch, ensure they are not assigned
////                                if (!batchTeacher.isAvailable()) { // Assuming BatchTeacher has isAvailable()
////                                    model.addEquality(
////                                            assignmentVars.get(teacherId).get(batchClass.getOpenedClass().getClassId()),
////                                            0
////                                    );
////                                }
////                            }
////                        }
////                    }
////                }
////            }
////        }
////
////        // Constraint 4: Teacher time conflicts (classes at the same time)
////        // This would require time slot information from TimeClass
////        for (Teacher teacher : teachers) {
////            // You would need to implement time conflict detection based on TimeClass entities
////            // This is a placeholder for time conflict constraints
////        }
////
////        // Objective: Maximize teacher preference or minimize conflicts
////        // For simplicity, let's maximize the number of assignments that match teacher preferences
////        LinearExprBuilder objective = LinearExpr.newBuilder();
////
////        for (Teacher teacher : teachers) {
////            for (OpenedClass openedClass : allClasses) {
////                // Add weight based on teacher preference (you might want to customize this)
////                double preferenceWeight = calculatePreferenceWeight(teacher, openedClass);
////                objective.addTerm(assignmentVars.get(teacher.getId()).get(openedClass.getClassId()), (int)(preferenceWeight * 100));
////            }
////        }
////
////        model.maximize(objective);
//
//        // Solve the model
//        CpSolver solver = new CpSolver();
//        CpSolverStatus status = solver.solve(model);
//
//        Map<Long, String> assignmentResults = new HashMap<>();
//
//        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
//            log.info("Solution found with objective value: {}", solver.objectiveValue());
//
////            // Extract the assignment results
////            for (OpenedClass openedClass : allClasses) {
////                for (Teacher teacher : teachers) {
////                    IntVar assignmentVar = assignmentVars.get(teacher.getId()).get(openedClass.getClassId());
////                    if (solver.value(assignmentVar) > 0.5) {
////                        assignmentResults.put(openedClass.getClassId(), teacher.getId());
////                        log.info("Class {} assigned to teacher {}", openedClass.getClassId(), teacher.getTeacherName());
////                        break;
////                    }
////                }
////            }
//        } else {
//            log.warn("No solution found for teacher-class assignment");
//        }
//
//        return assignmentResults;
//    }
//
//    private double calculatePreferenceWeight(Teacher teacher, OpenedClass openedClass) {
//        // Implement your preference calculation logic here
//        // This could be based on teacher's keywords, previous experience, etc.
//
//        // Simple example: higher weight if teacher has keywords matching course subject
//        if (teacher.getAcademicKeywordList() != null && openedClass.getCourseId() != null) {
//            String courseSubject = openedClass.getCourseId().toLowerCase();
//            for (var keyword : teacher.getAcademicKeywordList()) {
//                if (keyword.getKeyword().toLowerCase().contains(courseSubject) ||
//                        courseSubject.contains(keyword.getKeyword().toLowerCase())) {
//                    return 2.0; // Higher preference
//                }
//            }
//        }
//
//        return 1.0; // Default preference
//    }
//
//    // Helper method to get all classes from a batch
//    private List<OpenedClass> getClassesFromBatch(Batch batch) {
//        List<OpenedClass> classes = new ArrayList<>();
//        if (batch.getBatchClasses() != null) {
//            for (BatchClass batchClass : batch.getBatchClasses()) {
//                if (batchClass.getOpenedClass() != null) {
//                    classes.add(batchClass.getOpenedClass());
//                }
//            }
//        }
//        return classes;
//    }
//
//}

package openerp.openerpresourceserver.teacherassignment.service;

import openerp.openerpresourceserver.teacherassignment.model.dto.OpenedClassDto;
import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;

import java.util.List;
import java.util.Map;

public interface OpenedClassService {
    List<OpenedClassDto> findAllBySemester(String semester);

    List<String> getAllDistinctSemester();

    List<OpenedClassDto> findAllByBatchId(Long batchId);
    List<OpenedClass> findAllBySemesterAndCourseId(String semester, String courseId);

    Map<Long, String> assignmentTeacher(String semester, String schoolId);
}

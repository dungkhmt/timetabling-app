package openerp.openerpresourceserver.generaltimetabling.service;

import openerp.openerpresourceserver.generaltimetabling.model.entity.StudyingCourse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StudyingCourseService {
    List<StudyingCourse> findAllBySchoolId(String schoolId);

    List<String> findAllDistinctInSchoolId();
}

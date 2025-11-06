package openerp.openerpresourceserver.generaltimetabling.service.impl;


import openerp.openerpresourceserver.generaltimetabling.model.entity.StudyingCourse;
import openerp.openerpresourceserver.generaltimetabling.repo.StudyingCourseRepo;
import openerp.openerpresourceserver.generaltimetabling.service.StudyingCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyingCourseImpl implements StudyingCourseService {
    @Autowired
    private StudyingCourseRepo studyingCourseRepo;

    @Override
    public List<StudyingCourse> findAllBySchoolId(String schoolId) {
        return studyingCourseRepo.findAllBySchoolId(schoolId);
    }

    @Override
    public List<String> findAllDistinctInSchoolId() {
        return studyingCourseRepo.getDistinctInSchoolId();
    }
}

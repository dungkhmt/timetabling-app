package openerp.openerpresourceserver.generaltimetabling.controller;

import openerp.openerpresourceserver.generaltimetabling.model.entity.State;
import openerp.openerpresourceserver.generaltimetabling.model.entity.StudyingCourse;
import openerp.openerpresourceserver.generaltimetabling.service.SemesterService;
import openerp.openerpresourceserver.generaltimetabling.service.StudyingCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/studying-course")
public class StudyingCourseController {
    @Autowired
    private StudyingCourseService studyingCourseService;

    @GetMapping("/get-all-school")
    public ResponseEntity<List<String>> getAllSchool() {
        try {
            List<String> schoolList = studyingCourseService.findAllDistinctInSchoolId();
            if (schoolList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(schoolList, HttpStatus.OK);
//            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-courses-by-school/{schoolId}")
    public ResponseEntity<List<StudyingCourse>> getCoursesBySchool(@PathVariable String schoolId) {
        try {
            List<StudyingCourse> courseList = studyingCourseService.findAllBySchoolId(schoolId);
            if (courseList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(courseList, HttpStatus.OK);

        }catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

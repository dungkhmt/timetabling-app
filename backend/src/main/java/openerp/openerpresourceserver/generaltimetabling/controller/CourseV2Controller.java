package openerp.openerpresourceserver.generaltimetabling.controller;


import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.exception.CourseNotFoundException;
import openerp.openerpresourceserver.generaltimetabling.exception.CourseUsedException;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputCreateTimeTablingBatch;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.CourseDto;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Course;
import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingBatch;
import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingCourse;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingBatchRepo;
import openerp.openerpresourceserver.generaltimetabling.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;
@Log4j2
@RestController
@RequestMapping("/course-v2")
public class CourseV2Controller {
    @Autowired
    private CourseService service;

    @GetMapping("/get-all")
    public ResponseEntity<List<Course>> getAllCourse() {
        try {
            List<Course> courseList = service.getCourse();
            if (courseList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(courseList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateCourse(@Valid @RequestBody CourseDto requestDto) {
        try {
            service.updateCourse(requestDto);
            return new ResponseEntity<>(HttpStatus.OK);
        }  catch (CourseNotFoundException e) {
            return new ResponseEntity<>(e.getCustomMessage(), HttpStatus.BAD_REQUEST);
        } catch (CourseUsedException e) {
            return new ResponseEntity<>(e.getCustomMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createNewCourse(@Valid @RequestBody CourseDto courseDto) {
        try {
            Course course = service.create(courseDto);
            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (CourseUsedException e) {
            return new ResponseEntity<>(e.getCustomMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteById(@RequestParam String id) {
        try {
            service.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CourseNotFoundException e) {
            return new ResponseEntity<>(e.getCustomMessage(), HttpStatus.BAD_REQUEST);
        } catch (CourseUsedException e) {
            return new ResponseEntity<>(e.getCustomMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseDetail(@PathVariable String id) {
        TimeTablingCourse course = service.getCourseDetail(id);
        log.info("getCourseDetail, id = " + id);
        return ResponseEntity.ok().body(course);
    }
}

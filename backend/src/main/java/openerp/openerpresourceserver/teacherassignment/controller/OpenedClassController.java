package openerp.openerpresourceserver.teacherassignment.controller;

import openerp.openerpresourceserver.teacherassignment.model.dto.OpenedClassDto;

import openerp.openerpresourceserver.teacherassignment.service.OpenedClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/teacher-assignment-opened-class")
public class OpenedClassController {

    @Autowired
    private OpenedClassService openedClassService;

    @PostMapping("/get-all-classes-by-semester/{semester}")
    public ResponseEntity<List<OpenedClassDto>> getAllClassesBySemester(@PathVariable String semester) {

//        try {
            List<OpenedClassDto> openedClassList = openedClassService.findAllBySemester(semester);
            if (openedClassList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(openedClassList, HttpStatus.OK);
//             return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//        } catch (Exception e) {
////            logger.error("Error fetching semesters: ", e);
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
    }

    @GetMapping("/get-all-distinct-semester")
    public ResponseEntity<List<String>> getAllDistinctSemester() {

        try {
            List<String> semesterList = openedClassService.getAllDistinctSemester();
            if (semesterList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(semesterList, HttpStatus.OK);
            // return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
//            logger.error("Error fetching semesters: ", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}

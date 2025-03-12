package openerp.openerpresourceserver.generaltimetabling.service.impl;

import openerp.openerpresourceserver.generaltimetabling.exception.CourseNotFoundException;
import openerp.openerpresourceserver.generaltimetabling.exception.CourseUsedException;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.CourseDto;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Course;
import openerp.openerpresourceserver.generaltimetabling.repo.CourseRepo;
import openerp.openerpresourceserver.generaltimetabling.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {
    @Autowired
    private CourseRepo courseRepo;

    @Override
    public List<Course> getCourse() {
        if(courseRepo.findAll() == null) throw new CourseUsedException("k có lop");
        return courseRepo.findAll();
    }

    @Override
    public void updateCourse(CourseDto requestDto) {
        String id = requestDto.getId();
        String courseName = requestDto.getCourseName();
        Course course = courseRepo.findById(id).orElse(null);
        if (course == null) {
            throw new CourseNotFoundException("Mã môn học không tồn tại");
        }
        if (!courseName.equals(course.getCourseName())) {
            List<Course> courseList = courseRepo.getAllByCourseName(courseName);
            if (!courseList.isEmpty()) {
                throw new CourseUsedException("Tên môn học đã tồn tại!");
            }
        }
        String slotsPriority = requestDto.getSlotsPriority();
        Integer maxTeacherInCharge = requestDto.getMaxTeacherInCharge();

        course.setCourseName(courseName);
        course.setMaxTeacherInCharge(maxTeacherInCharge);
        course.setSlotsPriority(slotsPriority);

        courseRepo.save(course);
    }

    @Override
    public Course create(CourseDto courseDto) {
        String id = courseDto.getId();
        String courseName = courseDto.getCourseName();


        Course course = courseRepo.findById(id).orElse(null);

        if (course != null) {
            throw new CourseUsedException("Mã môn học đã tồn tại");
        }

        List<Course> courseList = courseRepo.getAllByCourseName(courseName);
        if (!courseList.isEmpty()) {
            throw new CourseUsedException("Tên môn học đã tồn tại!");
        }

        Course newCourse = new Course();
        newCourse.setId(id);
        newCourse.setCourseName(courseName);
        newCourse.setSlotsPriority(courseDto.getSlotsPriority());
        newCourse.setMaxTeacherInCharge(courseDto.getMaxTeacherInCharge());

        courseRepo.save(newCourse);

        return newCourse;
    }

    @Override
    public void deleteById(String id) {
        Course course = courseRepo.findById(id).orElse(null);
        if (course == null) {
            throw new RuntimeException("Không tồn tại môn học " + id);
        }

        courseRepo.deleteById(id);
    }
}

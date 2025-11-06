package openerp.openerpresourceserver.generaltimetabling.service.impl;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.exception.CourseNotFoundException;
import openerp.openerpresourceserver.generaltimetabling.exception.CourseUsedException;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.CourseDto;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Course;
import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingCourse;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.repo.CourseRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingClassRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingCourseRepo;
import openerp.openerpresourceserver.generaltimetabling.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
public class CourseServiceImpl implements CourseService {
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private TimeTablingCourseRepo timeTablingCourseRepo;

    @Autowired
    private TimeTablingClassRepo timeTablingClassRepo;

    private void synchronizeCourses(){
        //List<GeneralClass> cls = gcoRepo.findAll();
        List<TimeTablingClass> CLS = timeTablingClassRepo.findAll();
        Set<String> courseCodes = new HashSet<>();
        Map<String, String> mCourseCode2Name = new HashMap<>();
        Map<String, String> mCourseCode2Volumn = new HashMap<>();
        //for(GeneralClass gc: cls){
        for(TimeTablingClass gc: CLS){
            String courseCode = gc.getModuleCode();
            courseCodes.add(courseCode);
            mCourseCode2Name.put(courseCode,gc.getModuleName());
            mCourseCode2Volumn.put(courseCode,gc.getMass());
        }
        for(String courseCode: courseCodes){
            TimeTablingCourse course = timeTablingCourseRepo.findById(courseCode).orElse(null);
            if(course == null) {
                String courseName = mCourseCode2Name.get(courseCode);
                course = new TimeTablingCourse();
                course.setId(courseCode);
                course.setName(courseName);
                course.setMaxTeacherInCharge(50);
                course.setVolumn(mCourseCode2Volumn.get(courseCode));
                timeTablingCourseRepo.save(course);
                log.info("synchronizeCourses save " + courseCode + "," + courseName + " volumn " + course.getVolumn());
            }else{
                course.setVolumn(mCourseCode2Volumn.get(courseCode));
                log.info("synchronizeCourses save " + courseCode + "," + course.getName() + " volumn " + course.getVolumn());
                course = timeTablingCourseRepo.save(course);
            }
        }
    }

    @Override
    public List<Course> getCourse() {
        //synchronizeCourses();
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
        course.setVolumn(requestDto.getVolumn());
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
        newCourse.setVolumn(courseDto.getVolumn());
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

    @Override
    public TimeTablingCourse getCourseDetail(String courseId) {
        TimeTablingCourse course = timeTablingCourseRepo.findById(courseId).orElse(null);
        return course;
    }
}

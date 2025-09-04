package openerp.openerpresourceserver.thesisdefensejuryassignment.service;

import openerp.openerpresourceserver.thesisdefensejuryassignment.dto.TeacherDto;
import openerp.openerpresourceserver.thesisdefensejuryassignment.entity.Teacher;
import openerp.openerpresourceserver.thesisdefensejuryassignment.repo.TeacherRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class TeacherServiceImpl implements TeacherService {

    @Autowired
    private TeacherRepo teacherRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<TeacherDto> getAllTeacher() {

        List<Teacher> teachers = teacherRepo.getAllTeacher();

        return teachers.stream()
                .map(teacher -> modelMapper.map(teacher, TeacherDto.class))
                .toList();
    }

    @Override
    public List<TeacherDto> getAllTeacherByBatchId(Long batchId) {
        List<Teacher> teachers = teacherRepo.findAllByBatchId(batchId);

        return teachers.stream()
                .map(teacher -> modelMapper.map(teacher, TeacherDto.class))
                .toList();
    }

    @Override
    public List<TeacherDto> getTeacherByCourseId(String courseId, Long batchId) {
        List<Teacher> teachers = teacherRepo.getAllTeacherByCourseId(courseId, batchId);
        return teachers.stream()
                .map(teacher -> modelMapper.map(teacher, TeacherDto.class))
                .toList();
    }

}

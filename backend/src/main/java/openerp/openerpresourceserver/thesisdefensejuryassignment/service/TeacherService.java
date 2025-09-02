package openerp.openerpresourceserver.thesisdefensejuryassignment.service;

import openerp.openerpresourceserver.thesisdefensejuryassignment.dto.TeacherDto;
import openerp.openerpresourceserver.thesisdefensejuryassignment.entity.Teacher;

import java.util.List;

public interface TeacherService {
    List<TeacherDto> getAllTeacher();

    List<TeacherDto> getAllTeacherByBatchId(Long batchId);
}

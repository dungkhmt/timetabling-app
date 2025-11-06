package openerp.openerpresourceserver.teacherassignment.service;

import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.BatchTeacher;

import java.util.List;

public interface BatchTeacherService {
    BatchTeacher createBatchTeacher(Long batchId, String teacherUserId);

    List<BatchTeacher> getBatchTeacherByBatchId(Long batchId);
}

package openerp.openerpresourceserver.teacherassignment.service.impl;

import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeBatchTeacher;
import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.BatchTeacher;
import openerp.openerpresourceserver.teacherassignment.repo.BatchRepo;
import openerp.openerpresourceserver.teacherassignment.repo.BatchTeacherRepo;
import openerp.openerpresourceserver.teacherassignment.service.BatchTeacherService;
import openerp.openerpresourceserver.thesisdefensejuryassignment.entity.Teacher;
import openerp.openerpresourceserver.thesisdefensejuryassignment.repo.TeacherRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchTeacherServiceImpl implements BatchTeacherService {
    @Autowired
    private BatchTeacherRepo batchTeacherRepo;

    @Autowired
    private BatchRepo batchRepo;

    @Autowired
    private TeacherRepo teacherRepo;

    @Override
    public BatchTeacher createBatchTeacher(Long batchId, String teacherUserId) {
        // Kiểm tra Batch
        Batch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found with ID: " + batchId));

        // Kiểm tra Teacher
        Teacher teacher = teacherRepo.findById(teacherUserId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with User ID: " + teacherUserId));

        CompositeBatchTeacher compositeId = new CompositeBatchTeacher(batchId, teacherUserId);
        if (batchTeacherRepo.existsById(compositeId)) {
            throw new RuntimeException("Relation already exists between batch " + batchId + " and teacher " + teacherUserId);
        }
        BatchTeacher batchTeacher = BatchTeacher.builder()
                .id(compositeId)
                .batch(batch)
                .teacher(teacher)
                .build();

        return batchTeacherRepo.save(batchTeacher);
    }

    @Override
    public List<BatchTeacher> getBatchTeacherByBatchId(Long batchId) {
        return batchTeacherRepo.findAllByBatchId(batchId);
    }
}

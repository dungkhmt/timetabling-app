package openerp.openerpresourceserver.teacherassignment.service.impl;

import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;
import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeBatchClass;
import openerp.openerpresourceserver.teacherassignment.model.entity.relation.BatchClass;
import openerp.openerpresourceserver.teacherassignment.repo.BatchClassRepo;
import openerp.openerpresourceserver.teacherassignment.repo.BatchRepo;
import openerp.openerpresourceserver.teacherassignment.repo.OpenedClassRepo;
import openerp.openerpresourceserver.teacherassignment.service.BatchClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchClassImpl implements BatchClassService {

    @Autowired
    private BatchClassRepo batchClassRepo;

    @Autowired
    private BatchRepo batchRepo;

    @Autowired
    private OpenedClassRepo openedClassRepo;

    @Override
    public boolean existsBatchClassById(CompositeBatchClass id) {
        return batchClassRepo.existsBatchClassById(id);
    }

    @Override
    public BatchClass createBatchClass(Long batchId, Long classId) {
        // Kiểm tra Batch
        Batch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found with ID: " + batchId));

        // Kiểm tra OpenedClass
        OpenedClass openedClass = openedClassRepo.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found with ID: " + classId));

        // Kiểm tra tồn tại quan hệ BatchClass
        CompositeBatchClass compositeBatchClass = new CompositeBatchClass(batchId, classId);
        if (batchClassRepo.existsBatchClassById(compositeBatchClass)) {
            throw new RuntimeException("Relation already exists between batch " + batchId + " and class " + classId);
        }
        // Tạo BatchClass
        BatchClass batchClass = BatchClass.builder()
                .id(compositeBatchClass)
                .batch(batch)
                .openedClass(openedClass)
                .build();

        return batchClassRepo.save(batchClass);
    }

    @Override
    public List<BatchClass> findAllByBatchId(Long batchId) {
        return batchClassRepo.findAllByBatchId(batchId);
    }
}

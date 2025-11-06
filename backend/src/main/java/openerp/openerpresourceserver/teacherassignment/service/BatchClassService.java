package openerp.openerpresourceserver.teacherassignment.service;

import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeBatchClass;
import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.BatchClass;

import java.util.List;

public interface BatchClassService {
    boolean existsBatchClassById(CompositeBatchClass id);

    BatchClass createBatchClass(Long batchId, Long classId);

    List<BatchClass> findAllByBatchId(Long batchId);
}

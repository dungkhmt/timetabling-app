package openerp.openerpresourceserver.teacherassignment.service;

import openerp.openerpresourceserver.teacherassignment.model.dto.BatchDto;
import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;

import java.util.List;

public interface BatchService {
    List<BatchDto> getAllBatchBySemester(String semester);

    Batch createBatch(Batch batch);
}

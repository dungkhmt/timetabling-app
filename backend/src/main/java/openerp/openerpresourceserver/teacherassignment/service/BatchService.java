package openerp.openerpresourceserver.teacherassignment.service;

import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;

import java.util.List;

public interface BatchService {
    List<Batch> getAllBatchBySemester( String semester);
}

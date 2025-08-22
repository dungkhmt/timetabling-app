package openerp.openerpresourceserver.teacherassignment.service.impl;

import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
import openerp.openerpresourceserver.teacherassignment.repo.BatchRepo;
import openerp.openerpresourceserver.teacherassignment.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class BatchServiceImpl implements BatchService {

    @Autowired
    private BatchRepo batchRepo;
    @Override
    public List<Batch> getAllBatchBySemester(String semester) {
        return batchRepo.findAllBySemester(semester);
    }
}

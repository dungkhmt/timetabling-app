package openerp.openerpresourceserver.teacherassignment.service.impl;

import openerp.openerpresourceserver.teacherassignment.model.dto.BatchDto;
import openerp.openerpresourceserver.teacherassignment.model.dto.OpenedClassDto;
import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
import openerp.openerpresourceserver.teacherassignment.repo.BatchRepo;
import openerp.openerpresourceserver.teacherassignment.service.BatchService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BatchServiceImpl implements BatchService {

    @Autowired
    private BatchRepo batchRepo;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public List<BatchDto> getAllBatchBySemester(String semester) {
        List<Batch> batches = batchRepo.findAllBySemester(semester);

        return batches.stream()
                .map(batch -> modelMapper.map(batch, BatchDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public Batch createBatch(Batch batch) {
        return batchRepo.save(batch);
    }
}

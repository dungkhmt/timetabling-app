package openerp.openerpresourceserver.generaltimetabling.service.impl;

import openerp.openerpresourceserver.generaltimetabling.model.entity.general.BatchRoom;
import openerp.openerpresourceserver.generaltimetabling.repo.BatchRoomRepo;
import openerp.openerpresourceserver.generaltimetabling.service.BatchRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchRoomImpl implements BatchRoomService {


    @Autowired
    private BatchRoomRepo batchRoomRepo;

    @Override
    public List<BatchRoom> getBathRoomsByBatchId(Long batchId) {
        return batchRoomRepo.findAllByBatchId(batchId);
    }

    @Override
    public void addBatchRoom(BatchRoom batchRoom) {
        batchRoomRepo.save(batchRoom);
    }

}

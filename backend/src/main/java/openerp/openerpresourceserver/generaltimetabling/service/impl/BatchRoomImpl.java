package openerp.openerpresourceserver.generaltimetabling.service.impl;

import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeBatchRoomId;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.BatchRoom;
import openerp.openerpresourceserver.generaltimetabling.repo.BatchRoomRepo;
import openerp.openerpresourceserver.generaltimetabling.service.BatchRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Override
    public void deleteByBatchIdAndRoomId(Long batchId, String roomId) {
//        / Tạo composite key
        CompositeBatchRoomId id = new CompositeBatchRoomId(batchId, roomId);
        batchRoomRepo.deleteById(id);
    }


}

package openerp.openerpresourceserver.generaltimetabling.service;

import openerp.openerpresourceserver.generaltimetabling.model.entity.general.BatchRoom;

import java.util.List;

public interface BatchRoomService {
    List<BatchRoom> getBathRoomsByBatchId(Long batchId);

    void addBatchRoom(BatchRoom batchRoom);


}

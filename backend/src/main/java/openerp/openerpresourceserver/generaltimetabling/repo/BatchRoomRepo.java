package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeBatchRoomId;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.BatchRoom;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchRoomRepo extends JpaRepository<BatchRoom, CompositeBatchRoomId> {

    List<BatchRoom> findAllByBatchId(Long batchId);

}

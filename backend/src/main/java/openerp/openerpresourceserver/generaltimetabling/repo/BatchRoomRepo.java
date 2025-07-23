package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeBatchRoomId;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.BatchRoom;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRoomRepo extends JpaRepository<BatchRoom, CompositeBatchRoomId> {

    List<BatchRoom> findAllByBatchId(Long batchId);
}

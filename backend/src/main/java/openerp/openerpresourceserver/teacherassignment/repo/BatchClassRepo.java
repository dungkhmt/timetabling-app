package openerp.openerpresourceserver.teacherassignment.repo;

import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeBatchClass;
import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.BatchClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchClassRepo extends JpaRepository<BatchClass, CompositeBatchClass> {
    boolean existsBatchClassById(CompositeBatchClass id);

    List<BatchClass> findAllByBatchId(Long batchId);

}
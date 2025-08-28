package openerp.openerpresourceserver.teacherassignment.repo;

import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeBatchTeacher;
import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.BatchTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchTeacherRepo extends JpaRepository<BatchTeacher, CompositeBatchTeacher> {
    List<BatchTeacher> findAllByBatchId(Long batchId);
}

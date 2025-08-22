package openerp.openerpresourceserver.teacherassignment.repo;

import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchRepo extends JpaRepository<Batch, Long> {
    List<Batch> findAllBySemester(String semester);
}

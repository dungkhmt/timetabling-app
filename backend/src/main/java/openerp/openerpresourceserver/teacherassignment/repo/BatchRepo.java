package openerp.openerpresourceserver.teacherassignment.repo;

import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
import openerp.openerpresourceserver.teacherassignment.model.entity.relation.BatchClass;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepo extends JpaRepository<Batch, Long> {
    List<Batch> findAllBySemester(String semester);

    @NotNull
    Optional<Batch> findById(@NotNull Long id);
}

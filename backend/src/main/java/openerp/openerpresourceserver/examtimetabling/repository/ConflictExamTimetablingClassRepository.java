package openerp.openerpresourceserver.examtimetabling.repository;

import java.util.List;
import java.util.UUID;

import openerp.openerpresourceserver.examtimetabling.entity.ConflictExamTimetablingClass;

import org.springframework.data.jpa.repository.JpaRepository;
public interface ConflictExamTimetablingClassRepository extends JpaRepository<ConflictExamTimetablingClass, UUID> {
  public List<ConflictExamTimetablingClass> findByExamTimetablingClassId1InOrExamTimetablingClassId2In(List<UUID> classIds, List<UUID> classIds2);

  boolean existsByExamTimetablingClassId1AndExamTimetablingClassId2(UUID examClassId1, UUID examClassId2);
}

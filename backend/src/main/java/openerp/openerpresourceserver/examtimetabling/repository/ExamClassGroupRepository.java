package openerp.openerpresourceserver.examtimetabling.repository;

import openerp.openerpresourceserver.examtimetabling.entity.ExamClassGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ExamClassGroupRepository extends JpaRepository<ExamClassGroup, Integer> {
  boolean existsByName(String name);
}

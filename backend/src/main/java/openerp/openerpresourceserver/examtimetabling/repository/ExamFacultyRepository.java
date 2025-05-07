package openerp.openerpresourceserver.examtimetabling.repository;

import openerp.openerpresourceserver.examtimetabling.entity.ExamFaculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ExamFacultyRepository extends JpaRepository<ExamFaculty, String> {
}

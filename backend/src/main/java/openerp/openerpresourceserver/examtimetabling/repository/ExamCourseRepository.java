package openerp.openerpresourceserver.examtimetabling.repository;

import openerp.openerpresourceserver.examtimetabling.entity.ExamCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ExamCourseRepository extends JpaRepository<ExamCourse, String> {
}

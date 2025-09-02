package openerp.openerpresourceserver.thesisdefensejuryassignment.repo;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import openerp.openerpresourceserver.thesisdefensejuryassignment.entity.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherRepo extends JpaRepository<Teacher, String> {

    @Query(
            "Select t from Teacher t where t.teacherName like %:keyword% or t.id like %:keyword% or t.userLoginId like %:keyword%")
    Page<Teacher> findAllContain(@Param("keyword") String keyword, Pageable pageable);

    Optional<Teacher> findByUserLoginId(String userLoginId);

    Optional<Teacher> findByTeacherName(String teacherName);

    @Query(value = "select * from teacher;",nativeQuery = true)
    List<Teacher> getAllTeacher();

    @Query("SELECT t FROM Teacher t " +
            "JOIN FETCH t.batchTeachers bt " +
            "left join fetch t.teacherCapacityList "+
            "WHERE bt.id.batchId = :batchId")
    List<Teacher> findAllByBatchId(@Param("batchId") Long batchId);

}


package openerp.openerpresourceserver.generaltimetabling.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;

@Repository
public interface GeneralClassRepository extends JpaRepository<GeneralClass, Long> {

    @Transactional
    void deleteBySemester(String semester);

    @Transactional
    @Modifying
    @Query("DELETE FROM GeneralClass gc WHERE gc.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);


    List<GeneralClass> findAllByParentClassId(Long parentClassId);

    //@Query("SELECT gc FROM GeneralClass gc WHERE gc.classCode = :parentClassId AND gc.parentClassId IS NULL")
    @Query("SELECT gc FROM GeneralClass gc WHERE gc.parentClassId = :parentClassId")

    List<GeneralClass> findSubClassesByParentClassId(@Param("parentClassId") String parentClassId);

    @Query("SELECT gc FROM GeneralClass gc WHERE gc.classCode IN :parentClassIds AND gc.parentClassId IS NULL")
    List<GeneralClass> findSubClassesByParentClassIds(@Param("parentClassIds") List<String> parentClassIds);

    List<GeneralClass> findAllBySemester(String semester);

    List<GeneralClass> findAllBySemesterAndGroupName(String semester, String groupName);

    @Query(value = "SELECT gc.* FROM public.timetabling_general_classes gc " +
            "INNER JOIN public.timetabling_class_group cg ON gc.id = cg.class_id " +
            "WHERE gc.semester = :semester AND cg.group_id = :groupId",
            nativeQuery = true)
    List<GeneralClass> findAllBySemesterAndGroupId(@Param("semester") String semester, @Param("groupId") Long groupId);

    List<GeneralClass> findAllByIdIn(List<Long> ids);

    @Query("SELECT gc FROM GeneralClass gc WHERE gc.refClassId = :refClassId AND gc.semester = :semester")
    List<GeneralClass> findClassesByRefClassIdAndSemester(@Param("refClassId") Long refClassId, @Param("semester") String semester);

    @Transactional
    void deleteAllBySemester(String semester);

    @Query("SELECT gc FROM GeneralClass gc WHERE gc.classCode = :classCode")
    List<GeneralClass> findByClassCode(@Param("classCode") String classCode);
}

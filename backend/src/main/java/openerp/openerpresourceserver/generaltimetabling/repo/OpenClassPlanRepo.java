package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.OpenClassPlan;
import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeOpenClassPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenClassPlanRepo extends JpaRepository<OpenClassPlan, CompositeOpenClassPlan> {

    List<OpenClassPlan> findAllBySemester(String semester);

   @Query("SELECT DISTINCT o.semester FROM OpenClassPlan o")
   List<String> getAllSemesters();
}

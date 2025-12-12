package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.TimetablingLogSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimetablingLogScheduleRepo extends JpaRepository<TimetablingLogSchedule, UUID> {
    List<TimetablingLogSchedule> findAllByVersionId(Long versionId);
}

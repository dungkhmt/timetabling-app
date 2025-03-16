package openerp.openerpresourceserver.generaltimetabling.repo;

import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingConfigParams;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeTablingConfigParamsRepo extends JpaRepository<TimeTablingConfigParams, String> {

}

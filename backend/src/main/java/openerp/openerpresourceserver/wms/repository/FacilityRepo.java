package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FacilityRepo extends JpaRepository<Facility, String>, JpaSpecificationExecutor<Facility> {
}

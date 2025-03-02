package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepo extends JpaRepository<Facility, String> {
}

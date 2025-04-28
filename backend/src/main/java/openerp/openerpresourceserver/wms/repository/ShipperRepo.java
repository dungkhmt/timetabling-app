package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipperRepo extends JpaRepository<Shipper, String> {
}

package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryPlanShipper;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPlanShipperRepo extends JpaRepository<DeliveryPlanShipper, String> {
}

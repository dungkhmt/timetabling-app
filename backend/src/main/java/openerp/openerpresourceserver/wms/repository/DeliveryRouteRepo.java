package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRouteRepo extends JpaRepository<DeliveryRoute, String> {
}

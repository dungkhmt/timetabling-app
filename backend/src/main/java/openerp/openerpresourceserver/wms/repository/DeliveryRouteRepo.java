package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DeliveryRouteRepo extends JpaRepository<DeliveryRoute, String>, JpaSpecificationExecutor<DeliveryRoute> {
    List<DeliveryRoute> findByDeliveryPlanId(String id);
}

package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryRouteItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRouteItemRepo extends JpaRepository<DeliveryRouteItem, String> {
    void deleteByDeliveryRouteId(String id);
}

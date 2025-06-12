package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryPlanShipper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryPlanShipperRepo extends JpaRepository<DeliveryPlanShipper, String> {
    List<DeliveryPlanShipper> findByDeliveryPlanId(String id);

    void deleteByDeliveryPlanIdAndShipperIdIn(String id, List<String> unAssignedShipperIds);
}

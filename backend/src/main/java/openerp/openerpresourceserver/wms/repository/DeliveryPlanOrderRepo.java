package openerp.openerpresourceserver.wms.repository;


import openerp.openerpresourceserver.wms.entity.DeliveryPlanOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryPlanOrderRepo extends JpaRepository<DeliveryPlanOrder, String> {
    List<DeliveryPlanOrder> findByDeliveryPlanId(String id);

    void deleteByDeliveryPlanIdAndDeliveryBillIdIn(String id, List<String> unAssignedBillIds);
}

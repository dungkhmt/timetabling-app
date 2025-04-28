package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPlanRepo extends JpaRepository<DeliveryPlan, String> {
}

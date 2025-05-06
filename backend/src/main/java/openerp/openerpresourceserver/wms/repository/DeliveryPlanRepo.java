package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryPlan;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeliveryPlanRepo extends JpaRepository<DeliveryPlan, String>, JpaSpecificationExecutor<DeliveryPlan> {
}

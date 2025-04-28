package openerp.openerpresourceserver.wms.repository;


import openerp.openerpresourceserver.wms.entity.DeliveryPlanOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPlanOrderRepo extends JpaRepository<DeliveryPlanOrder, String> {
}

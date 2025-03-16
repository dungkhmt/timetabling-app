package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.OrderItemBilling;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemBillingRepo extends JpaRepository<OrderItemBilling, String> {
}

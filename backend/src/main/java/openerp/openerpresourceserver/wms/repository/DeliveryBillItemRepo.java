package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryBillItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryBillItemRepo extends JpaRepository<DeliveryBillItem, String> {
}

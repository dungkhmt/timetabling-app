package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeliveryBillRepo extends JpaRepository<DeliveryBill, String>, JpaSpecificationExecutor<DeliveryBill> {
}

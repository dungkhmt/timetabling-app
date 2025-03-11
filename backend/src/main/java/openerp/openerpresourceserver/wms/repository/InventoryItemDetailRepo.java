package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.InventoryItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemDetailRepo extends JpaRepository<InventoryItemDetail, String> {
}

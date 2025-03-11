package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepo extends JpaRepository<InventoryItem, String> {
}

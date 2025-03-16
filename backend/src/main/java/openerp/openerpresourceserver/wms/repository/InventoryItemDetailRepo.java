package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.InventoryItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryItemDetailRepo extends JpaRepository<InventoryItemDetail, String> {
    List<InventoryItemDetail> findByShipmentId(String shipmentId);
}

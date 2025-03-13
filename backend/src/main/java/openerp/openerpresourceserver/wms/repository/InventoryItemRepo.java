package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryItemRepo extends JpaRepository<InventoryItem, String> {
    Page<InventoryItem> findByProductIdIn(List<String> productIds, PageRequest pageRequest);
}

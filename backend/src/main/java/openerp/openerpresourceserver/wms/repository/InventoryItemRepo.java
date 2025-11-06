package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface InventoryItemRepo extends JpaRepository<InventoryItem, String> {
    Page<InventoryItem> findByProductIdIn(List<String> productIds, PageRequest pageRequest);

    List<InventoryItem> findByProductIdIn(List<String> productIds);

    List<InventoryItem> findByQuantityLessThan(int lowStockThreshold);

    List<InventoryItem> findByProductIdAndFacilityId(String id, String id1);

    @Query("SELECT SUM(i.quantity) FROM InventoryItem i WHERE i.product.id = :productId")
    int sumByProductId(String productId);

    InventoryItem findByFacilityIdAndProductIdAndLotIdAndManufacturingDateAndExpirationDate(String facilityId, String productId, String lotId,
                                                                                            LocalDate manufacturingDate,
                                                                                            LocalDate expirationDate);

    Page<InventoryItem> findByProductId(String productId, PageRequest pageRequest);
}

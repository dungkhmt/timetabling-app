package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.InventoryItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryItemDetailRepo extends JpaRepository<InventoryItemDetail, String> {
    List<InventoryItemDetail> findByShipmentId(String shipmentId);

    @Query("SELECT FUNCTION('DATE', iid.shipment.createdStamp) as date, " +
            "SUM(iid.quantity) as totalQuantity " +
            "FROM InventoryItemDetail iid " +
            "WHERE iid.product.id = :productId " +
            "AND iid.shipment.shipmentTypeId = :shipmentTypeId " +
            "AND iid.shipment.createdStamp BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', iid.shipment.createdStamp) " +
            "ORDER BY FUNCTION('DATE', iid.shipment.createdStamp)")
    List<Object[]> getDailyQuantitiesByProductAndShipmentType(String productId, String shipmentTypeId, LocalDateTime startDate, LocalDateTime endDate);
}

package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.OrderItemBilling;
import openerp.openerpresourceserver.wms.entity.Product;
import openerp.openerpresourceserver.wms.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemBillingRepo extends JpaRepository<OrderItemBilling, String> {
    List<OrderItemBilling> findByShipmentAndProduct(Shipment shipment, Product product);

    @Query("SELECT FUNCTION('DATE', oib.shipment.createdStamp) as date, " +
            "SUM(oib.quantity) as totalQuantity " +
            "FROM OrderItemBilling oib " +
            "WHERE oib.product.id = :productId " +
            "AND oib.shipment.shipmentTypeId = :shipmentTypeId " +
            "AND oib.shipment.createdStamp BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('DATE', oib.shipment.createdStamp) " +
            "ORDER BY date")
    List<Object[]> getDailyQuantitiesByProductAndShipmentType(
            @Param("productId") String productId,
            @Param("shipmentTypeId") String shipmentTypeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.OrderItemBilling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemBillingRepo extends JpaRepository<OrderItemBilling, String>, JpaSpecificationExecutor<OrderItemBilling> {
    @Query("SELECT oib.createdStamp, oib.quantity, p.id, p.name, oib.orderItemBillingTypeId " +
            "FROM OrderItemBilling oib " +
            "JOIN oib.product p " +
            "WHERE oib.createdStamp BETWEEN :startDate AND :endDate " +
            "ORDER BY oib.createdStamp")
    List<Object[]> getInventoryMovements(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("SELECT oib.createdStamp, oib.quantity, p.id, p.name, oib.orderItemBillingTypeId " +
            "FROM OrderItemBilling oib " +
            "JOIN oib.product p " +
            "WHERE oib.facility.id = :facilityId " +
            "AND oib.createdStamp BETWEEN :startDate AND :endDate " +
            "ORDER BY oib.createdStamp")
    List<Object[]> getFacilityMovements(
            @Param("facilityId") String facilityId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("SELECT f.id, f.name, oib.quantity, oib.orderItemBillingTypeId " +
            "FROM OrderItemBilling oib " +
            "JOIN oib.facility f " +
            "WHERE oib.createdStamp BETWEEN :startDate AND :endDate")
    List<Object[]> getFacilitySummary(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
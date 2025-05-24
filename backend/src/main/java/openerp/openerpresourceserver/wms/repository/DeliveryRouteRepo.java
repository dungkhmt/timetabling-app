package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeliveryRouteRepo extends JpaRepository<DeliveryRoute, String>, JpaSpecificationExecutor<DeliveryRoute> {
    List<DeliveryRoute> findByDeliveryPlanId(String id);

    @Query("SELECT COUNT(dr) FROM DeliveryRoute dr WHERE dr.deliveryPlan.createdStamp BETWEEN :start AND :end")
    int countByCreatedStampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT dr.statusId as status, COUNT(dr) as count FROM DeliveryRoute dr " +
           "WHERE dr.deliveryPlan.createdStamp BETWEEN :start AND :end GROUP BY dr.statusId")
    List<Object[]> countByStatusBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('DATE', dp.createdStamp) as date, COUNT(dr) as count " +
           "FROM DeliveryRoute dr JOIN dr.deliveryPlan dp " +
           "WHERE dp.createdStamp BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('DATE', dp.createdStamp) ORDER BY date")
    List<Object[]> countDailyBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s.userLoginId, s.userLogin.firstName, s.userLogin.lastName, " +
           "COUNT(dr) as assigned, " +
           "SUM(CASE WHEN dr.statusId = 'COMPLETED' THEN 1 ELSE 0 END) as completed, " +
           "SUM(CASE WHEN dr.statusId = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress " +
           "FROM DeliveryRoute dr " +
           "JOIN dr.assignToShipper s " +
           "WHERE dr.deliveryPlan.createdStamp BETWEEN :start AND :end " +
           "GROUP BY s.userLoginId, s.userLogin.firstName, s.userLogin.lastName " +
           "ORDER BY assigned DESC")
    List<Object[]> findShipperPerformanceBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

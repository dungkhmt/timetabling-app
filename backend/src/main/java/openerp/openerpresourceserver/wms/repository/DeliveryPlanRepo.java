package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeliveryPlanRepo extends JpaRepository<DeliveryPlan, String>, JpaSpecificationExecutor<DeliveryPlan> {

    List<DeliveryPlan> findAllByCreatedStampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(dp) FROM DeliveryPlan dp WHERE dp.createdStamp BETWEEN :start AND :end")
    int countByCreatedStampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT dp.statusId as status, COUNT(dp) as count FROM DeliveryPlan dp " +
           "WHERE dp.createdStamp BETWEEN :start AND :end GROUP BY dp.statusId")
    List<Object[]> countByStatusBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT FUNCTION('DATE', dp.createdStamp) as date, COUNT(dp) as count " +
           "FROM DeliveryPlan dp WHERE dp.createdStamp BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('DATE', dp.createdStamp) ORDER BY date")
    List<Object[]> countDailyBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT SUM(dp.totalWeight) FROM DeliveryPlan dp WHERE dp.createdStamp BETWEEN :start AND :end")
    java.math.BigDecimal sumTotalWeightBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryBill;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeliveryBillRepo extends JpaRepository<DeliveryBill, String>, JpaSpecificationExecutor<DeliveryBill> {

    List<DeliveryBill> findAllByCreatedStampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(db) FROM DeliveryBill db WHERE db.createdStamp BETWEEN :start AND :end")
    int countByCreatedStampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT db.statusId as status, COUNT(db) as count FROM DeliveryBill db " +
           "WHERE db.createdStamp BETWEEN :start AND :end GROUP BY db.statusId")
    List<Object[]> countByStatusBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT FUNCTION('DATE', db.createdStamp) as date, COUNT(db) as count " +
           "FROM DeliveryBill db WHERE db.createdStamp BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('DATE', db.createdStamp) ORDER BY date")
    List<Object[]> countDailyBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c.id as customerId, c.name as customerName, COUNT(db) as count, SUM(db.totalWeight) as weight " +
            "FROM DeliveryBill db JOIN db.toCustomer c " +
            "WHERE db.createdStamp BETWEEN :start AND :end " +
            "GROUP BY c.id, c.name ORDER BY count DESC")
    List<Object[]> findTopCustomersBetween(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           Pageable pageable);

}

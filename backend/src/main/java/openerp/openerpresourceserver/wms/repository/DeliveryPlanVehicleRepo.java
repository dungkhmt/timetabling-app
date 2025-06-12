package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.DeliveryPlanVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryPlanVehicleRepo extends JpaRepository<DeliveryPlanVehicle, String> {
    List<DeliveryPlanVehicle> findByDeliveryPlanId(String id);

    void deleteByDeliveryPlanIdAndVehicleIdIn(String id, List<String> unAssignedVehicleIds);
}

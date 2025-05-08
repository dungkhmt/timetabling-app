package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepo extends JpaRepository<Vehicle, String> {
    List<Vehicle> findByStatusId(String available);
}

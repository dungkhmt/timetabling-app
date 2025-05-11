package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepo extends JpaRepository<Vehicle, String> {
    Page<Vehicle> findAllByStatusId(String statusId, PageRequest pageReq);
}

package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VehicleRepo extends JpaRepository<Vehicle, String>, JpaSpecificationExecutor<Vehicle> {
    Page<Vehicle> findAllByStatusId(String statusId, PageRequest pageReq);
}

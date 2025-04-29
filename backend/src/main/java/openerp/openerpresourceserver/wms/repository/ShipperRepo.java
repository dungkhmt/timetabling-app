package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.constant.enumrator.ShipperStatus;
import openerp.openerpresourceserver.wms.entity.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipperRepo extends JpaRepository<Shipper, String> {
    List<Shipper> findByStatusId(String shipperStatus);
}

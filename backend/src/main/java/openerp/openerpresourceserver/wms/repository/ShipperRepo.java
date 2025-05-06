package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.constant.enumrator.ShipperStatus;
import openerp.openerpresourceserver.wms.entity.DeliveryBill;
import openerp.openerpresourceserver.wms.entity.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ShipperRepo extends JpaRepository<Shipper, String>, JpaSpecificationExecutor<Shipper> {
    List<Shipper> findByStatusId(String shipperStatus);
}

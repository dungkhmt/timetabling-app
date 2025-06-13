package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ShipmentRepo extends JpaRepository<Shipment, String>, JpaSpecificationExecutor<Shipment> {
    Page<Shipment> findByOrderId(String orderId, PageRequest pageRequest);

    Page<Shipment> findByShipmentTypeIdAndStatusId(String shipmentType,String name, PageRequest pageRequest, Sort by);
}

package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentRepo extends JpaRepository<Shipment, String> {
    Page<Shipment> findByOrderId(String orderId, PageRequest pageRequest);
}

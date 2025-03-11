package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepo extends JpaRepository<Shipment, String> {
}

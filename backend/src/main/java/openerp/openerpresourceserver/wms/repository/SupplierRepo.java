package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepo extends JpaRepository<Supplier, String> {
}

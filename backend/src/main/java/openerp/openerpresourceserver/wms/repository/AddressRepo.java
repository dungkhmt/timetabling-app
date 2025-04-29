package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepo extends JpaRepository<Address, String> {
    List<Address> findAllByEntityIdInAndEntityType(List<String> entityIds, String entityType);
    Address findByEntityIdAndEntityType(String entityId, String entityType);
}

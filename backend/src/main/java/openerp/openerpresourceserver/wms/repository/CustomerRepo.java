package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepo extends JpaRepository<Customer, String> {
}

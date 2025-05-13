package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerRepo extends JpaRepository<Customer, String>, JpaSpecificationExecutor<Customer> {
}

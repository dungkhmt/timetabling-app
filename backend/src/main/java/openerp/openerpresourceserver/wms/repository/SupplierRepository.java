package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SupplierRepository extends JpaRepository<Supplier, String>, JpaSpecificationExecutor<Supplier> {

    Page<Supplier> findAllByStatusId(PageRequest pageReq, String statusId);
}

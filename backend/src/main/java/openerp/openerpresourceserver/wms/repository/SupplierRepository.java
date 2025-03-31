package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Page<Supplier> findAllByStateId(PageRequest pageReq, String statusId);
}

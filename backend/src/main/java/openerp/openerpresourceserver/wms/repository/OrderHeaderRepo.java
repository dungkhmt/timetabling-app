package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.OrderHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderHeaderRepo extends JpaRepository<OrderHeader, String>, JpaSpecificationExecutor<OrderHeader> {

    Page<OrderHeader> findAllByStatus(String name, PageRequest pageable);

    Page<OrderHeader> findAllByOrderTypeId(PageRequest pageReq, String name);

}

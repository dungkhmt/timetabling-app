package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.OrderHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderHeaderRepo extends JpaRepository<OrderHeader, String>, JpaSpecificationExecutor<OrderHeader> {

    Page<OrderHeader> findAllByStatus(String name, PageRequest pageable);

    List<OrderHeader> findAllByCreatedStampBetweenAndOrderTypeId(LocalDateTime startDateTime, LocalDateTime endDateTime, String name);
}

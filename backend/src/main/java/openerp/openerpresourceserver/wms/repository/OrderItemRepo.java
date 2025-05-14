package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepo extends JpaRepository<OrderItem, String> {
    List<OrderItem> findAllByOrderIdIn(List<String> orderIds);
}

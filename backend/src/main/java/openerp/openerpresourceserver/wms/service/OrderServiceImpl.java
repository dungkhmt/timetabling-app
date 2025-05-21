package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.repository.OrderHeaderRepo;
import openerp.openerpresourceserver.wms.repository.UserLoginRepo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderHeaderRepo orderHeaderRepo;
    private final UserLoginRepo userLoginRepo;
    @Override
    public ApiResponse<Void> approveSaleOrder(String id, String name) {
        var orderHeader = orderHeaderRepo.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + id));
        orderHeader.setStatus(OrderStatus.APPROVED.name());

        var userApproved = userLoginRepo.findById(name)
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + name));
        orderHeader.setUserApproved(userApproved);

        orderHeaderRepo.save(orderHeader);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Order approved successfully")
                .build();
    }
}

package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.service.OrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderManagementController {
    private final OrderService orderService;
    @PutMapping("/approve/{id}")
    public ApiResponse<Void> approveSaleOrder(@PathVariable String id, Principal principal) {
        return orderService.approveSaleOrder(id, principal.getName());
    }
}

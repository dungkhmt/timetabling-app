package openerp.openerpresourceserver.wms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.CreateSaleOrderReq;
import openerp.openerpresourceserver.wms.service.SaleOrderService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/sale-order")
@RequiredArgsConstructor
public class SaleOrderController {
    private final SaleOrderService saleOrderService;

    @PostMapping("/create")
    public ApiResponse<Void> createSaleOrder(@RequestBody CreateSaleOrderReq saleOrder, Principal principal) {
        return saleOrderService.createSaleOrder(saleOrder, principal.getName());
    }

}

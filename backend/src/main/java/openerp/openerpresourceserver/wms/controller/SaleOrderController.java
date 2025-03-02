package openerp.openerpresourceserver.wms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.CreateSaleOrderReq;
import openerp.openerpresourceserver.wms.service.SaleOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sale-order")
@RequiredArgsConstructor
public class SaleOrderController {
    private final SaleOrderService saleOrderService;

    @PostMapping("/create")
    public ApiResponse<Void> createSaleOrder(@RequestBody @Valid CreateSaleOrderReq saleOrder) {
        return saleOrderService.createSaleOrder(saleOrder);
    }
}

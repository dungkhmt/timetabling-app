package openerp.openerpresourceserver.wms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.CreatePurchaseOrderReq;
import openerp.openerpresourceserver.wms.service.PurchaseOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purchase-order")
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;
    @PostMapping("/create")
    public ApiResponse<Void> createPurchaseOrder(@RequestBody @Valid CreatePurchaseOrderReq purchaseOrder
            , Principal principal) {
        return purchaseOrderService.createPurchaseOrder(purchaseOrder, principal.getName());
    }
}

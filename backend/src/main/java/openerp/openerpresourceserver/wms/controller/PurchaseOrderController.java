package openerp.openerpresourceserver.wms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.PurchaseOrderGetListFilter;
import openerp.openerpresourceserver.wms.dto.filter.SaleOrderGetListFilter;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.CreatePurchaseOrderReq;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.PurchaseOrderListRes;
import openerp.openerpresourceserver.wms.service.PurchaseOrderService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/get-all")
    public ApiResponse<Pagination<PurchaseOrderListRes>> getAllPurchaseOrder(@RequestParam int page, @RequestParam int limit,
                                                                             @RequestBody PurchaseOrderGetListFilter filters) {
        return purchaseOrderService.getAllPurchaseOrder(page, limit, filters);
    }
}

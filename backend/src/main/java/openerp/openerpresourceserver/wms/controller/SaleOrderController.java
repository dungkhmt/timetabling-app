package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.saleOrder.CreateOutBounndReq;
import openerp.openerpresourceserver.wms.dto.saleOrder.CreateSaleOrderReq;
import openerp.openerpresourceserver.wms.dto.saleOrder.OrderListRes;
import openerp.openerpresourceserver.wms.dto.saleOrder.SalesOrderDetailRes;
import openerp.openerpresourceserver.wms.service.SaleOrderService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/sale-order")
@RequiredArgsConstructor
public class SaleOrderController {
    private final SaleOrderService saleOrderService;

    @PostMapping("/create")
    public ApiResponse<Void> createSaleOrder(@RequestBody CreateSaleOrderReq saleOrder, Principal principal) {
        return saleOrderService.createSaleOrder(saleOrder, principal.getName());
    }

    @GetMapping("/details/{id}")
    public ApiResponse<SalesOrderDetailRes> getSaleOrderDetails(@PathVariable String id) {
        return saleOrderService.getSaleOrderDetails(id);
    }

    @PutMapping("/approve/{id}")
    public ApiResponse<Void> approveSaleOrder(@PathVariable String id, Principal principal) {
        return saleOrderService.approveSaleOrder(id, principal.getName());
    }

    @PostMapping("/get-all")
    public ApiResponse<Pagination<OrderListRes>>  getAllSaleOrders(@RequestParam int page, @RequestParam int limit, @RequestBody Map<String, Object> filters) {
        return saleOrderService.getAllSaleOrders(page, limit, filters);
    }

    @PostMapping("/outbound/create")
    public ApiResponse<Void> createOutboundSaleOrder(@RequestBody CreateOutBounndReq saleOrder, Principal principal) {
        return saleOrderService.createOutboundSaleOrder(saleOrder, principal.getName());
    }

    @GetMapping("/get-approved")
    public ApiResponse<Pagination<OrderListRes>> getApprovedSaleOrders(@RequestParam int page, @RequestParam int limit) {
        return saleOrderService.getApprovedSaleOrders(page, limit);
    }


}

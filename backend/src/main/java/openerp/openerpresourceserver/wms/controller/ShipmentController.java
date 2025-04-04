package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.shipment.*;
import openerp.openerpresourceserver.wms.service.ShipmentService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RequestMapping("/shipment")
@RestController
public class ShipmentController {
    private final ShipmentService shipmentService;
    @PostMapping("/outbound/create")
    public ApiResponse<Void> createOutboundSaleOrder(@RequestBody CreateOutBoundReq saleOrder, Principal principal) {
        return shipmentService.createOutboundSaleOrder(saleOrder, principal.getName());
    }

    @PostMapping("/inbound/create")
    public ApiResponse<Void> createInboundPurchaseOrder(@RequestBody CreateInBoundReq purchaseOrder, Principal principal) {
        return shipmentService.createInboundPurchaseOrder(purchaseOrder, principal.getName());
    }

    @GetMapping("/outbound/order/{orderId}")
    public ApiResponse<Pagination<OutBoundByOrderRes>> outboundOrder(
            @RequestParam int page,
            @RequestParam int limit,
            @PathVariable String orderId) {
        return shipmentService.getOutBoundByOrder(orderId, page, limit);
    }

    @GetMapping("/outbound/{shipmentId}")
    public ApiResponse<OutBoundDetailRes> outboundOrder(@PathVariable String shipmentId) {
        return shipmentService.getOutBoundDetail(shipmentId);
    }

    @GetMapping("/inbound/order/{orderId}")
    public ApiResponse<Pagination<InboundByOrderRes>> inboundOrder(
            @RequestParam int page,
            @RequestParam int limit,
            @PathVariable String orderId) {
        return shipmentService.getInBoundByOrder(orderId, page, limit);
    }

    @GetMapping("/inbound/{shipmentId}")
    public ApiResponse<InboundDetailRes> inboundOrder(@PathVariable String shipmentId) {
        return shipmentService.getInBoundDetail(shipmentId);
    }



}

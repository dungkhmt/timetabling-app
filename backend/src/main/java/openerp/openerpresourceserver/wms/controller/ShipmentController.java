package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.shipment.CreateOutBounndReq;
import openerp.openerpresourceserver.wms.dto.shipment.OutBoundByOrderRes;
import openerp.openerpresourceserver.wms.dto.shipment.OutBoundDetailRes;
import openerp.openerpresourceserver.wms.service.ShipmentService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RequestMapping("/shipment")
@RestController
public class ShipmentController {
    private final ShipmentService shipmentService;
    @PostMapping("/outbound/create")
    public ApiResponse<Void> createOutboundSaleOrder(@RequestBody CreateOutBounndReq saleOrder, Principal principal) {
        return shipmentService.createOutboundSaleOrder(saleOrder, principal.getName());
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




}

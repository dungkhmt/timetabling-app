package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryBill;
import openerp.openerpresourceserver.wms.entity.DeliveryBill;
import openerp.openerpresourceserver.wms.service.DeliveryBillService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/delivery-bill")
public class DeliveryBillController {
    private final DeliveryBillService deliveryBillService;
    @PostMapping
    public ApiResponse<Void> createDeliveryBill(@RequestBody CreateDeliveryBill req) {
        return deliveryBillService.createDeliveryBill(req);
    }
}

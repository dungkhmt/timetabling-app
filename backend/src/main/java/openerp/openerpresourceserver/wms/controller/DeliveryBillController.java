package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryBill;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryBillGetListFilter;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryListPageRes;
import openerp.openerpresourceserver.wms.service.DeliveryBillService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/delivery-bill")
public class DeliveryBillController {
    private final DeliveryBillService deliveryBillService;
    @PostMapping("/create")
    public ApiResponse<Void> createDeliveryBill(@RequestBody CreateDeliveryBill req, Principal principal) {
        return deliveryBillService.createDeliveryBill(req, principal);
    }

    @PostMapping("/get-all")
    public ApiResponse<Pagination<DeliveryListPageRes>> getDeliveryBills(@RequestParam int page, @RequestParam int limit, @RequestBody DeliveryBillGetListFilter filters) {
        return deliveryBillService.getDeliveryBills(page, limit, filters);
    }
}

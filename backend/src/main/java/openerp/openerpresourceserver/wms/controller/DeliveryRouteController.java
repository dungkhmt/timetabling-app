package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryRouteGetListRes;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryRouteResponseDTO;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryRouteGetListFilter;
import openerp.openerpresourceserver.wms.service.DeliveryRouteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/delivery-route")
public class DeliveryRouteController {
    private final DeliveryRouteService deliveryRouteService;

    @GetMapping("/auto-assign/{deliveryPlanId}")
    public ApiResponse<DeliveryRouteResponseDTO> autoAssignDeliveryRoute(@PathVariable String deliveryPlanId) {
        return deliveryRouteService.autoAssignDeliveryRoutesForPlan(deliveryPlanId);
    }

    @PostMapping("get-all")
    public ApiResponse<Pagination<DeliveryRouteGetListRes>> getAlls(@RequestParam int page,
                                                                    @RequestParam int limit,
                                                                    @RequestBody DeliveryRouteGetListFilter filters) {
        return deliveryRouteService.getAlls(page, limit, filters);
    }
}

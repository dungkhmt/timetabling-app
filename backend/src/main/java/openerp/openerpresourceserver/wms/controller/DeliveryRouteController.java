package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryRouteResponseDTO;
import openerp.openerpresourceserver.wms.service.DeliveryRouteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/delivery-route")
public class DeliveryRouteController {
    private final DeliveryRouteService deliveryRouteService;

    @GetMapping("/auto-assign/{deliveryPlanId}")
    public ApiResponse<DeliveryRouteResponseDTO> autoAssignDeliveryRoute(@PathVariable String deliveryPlanId) {
        return deliveryRouteService.autoAssignDeliveryRoutesForPlan(deliveryPlanId);
    }
}

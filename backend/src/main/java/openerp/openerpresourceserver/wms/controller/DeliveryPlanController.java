package openerp.openerpresourceserver.wms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryPlan;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryPlanGetListFilter;
import openerp.openerpresourceserver.wms.entity.DeliveryPlan;
import openerp.openerpresourceserver.wms.service.DeliveryPlanService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/delivery-plan")
public class DeliveryPlanController {
    private final DeliveryPlanService deliveryPlanService;

    @PostMapping("/create")
    public ApiResponse<Void> createDeliveryPlan(@RequestBody @Valid CreateDeliveryPlan req, Principal principal) {
        return deliveryPlanService.createDeliveryPlan(req, principal);
    }

    @GetMapping("/get-all")
    public ApiResponse<Pagination<DeliveryPlan>> getAllDeliveryPlans(@RequestParam int page, @RequestParam int limit, @RequestBody DeliveryPlanGetListFilter filters) {
        return deliveryPlanService.getAllDeliveryPlans(page, limit, filters);
    }
}

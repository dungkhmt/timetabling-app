package openerp.openerpresourceserver.wms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryPlan;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryPlanDetailRes;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryPlanPageRes;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryPlanGetListFilter;
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

    @PostMapping("/get-all")
    public ApiResponse<Pagination<DeliveryPlanPageRes>> getAllDeliveryPlans(@RequestParam int page, @RequestParam int limit, @RequestBody DeliveryPlanGetListFilter filters) {
        return deliveryPlanService.getAllDeliveryPlans(page, limit, filters);
    }

    @GetMapping("details/{deliveryPlanId}")
    public ApiResponse<DeliveryPlanDetailRes> getDeliveryPlanById(@PathVariable String deliveryPlanId) {
        return deliveryPlanService.getDeliveryPlanById(deliveryPlanId);
    }
}

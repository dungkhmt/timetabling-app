package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryPlan;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryPlanPageRes;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryPlanGetListFilter;
import openerp.openerpresourceserver.wms.entity.DeliveryPlan;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

public interface DeliveryPlanService {
    @Transactional
    ApiResponse<Void> createDeliveryPlan(CreateDeliveryPlan req, Principal principal);

    ApiResponse<Pagination<DeliveryPlanPageRes>> getAllDeliveryPlans(int page, int limit, DeliveryPlanGetListFilter filters);
}

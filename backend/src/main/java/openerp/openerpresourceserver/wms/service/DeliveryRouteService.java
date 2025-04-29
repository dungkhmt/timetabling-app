package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryRouteResponseDTO;

public interface DeliveryRouteService {
    ApiResponse<DeliveryRouteResponseDTO> autoAssignDeliveryRoutesForPlan(String deliveryPlanId);
}

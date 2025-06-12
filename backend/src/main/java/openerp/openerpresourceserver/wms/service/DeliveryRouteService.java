package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryRouteGetListRes;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryRouteResponseDTO;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryRouteGetListFilter;

public interface DeliveryRouteService {
    ApiResponse<Pagination<DeliveryRouteGetListRes>> getAlls(int page, int limit, DeliveryRouteGetListFilter filters);

    ApiResponse<DeliveryRouteResponseDTO> autoAssignDeliveryRoutesForPlan(String deliveryPlanId, String solverName);
}

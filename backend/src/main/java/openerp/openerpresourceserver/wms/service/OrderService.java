package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;

public interface OrderService {

    ApiResponse<Void> approveSaleOrder(String id, String name);
}

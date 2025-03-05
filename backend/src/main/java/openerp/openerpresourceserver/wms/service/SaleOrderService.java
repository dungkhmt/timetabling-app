package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.CreateSaleOrderReq;

public interface SaleOrderService {
    ApiResponse<Void> createSaleOrder(CreateSaleOrderReq saleOrder, String name);
}

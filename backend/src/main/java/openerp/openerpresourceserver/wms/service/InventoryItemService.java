package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.saleOrder.CreateOutBounndReq;
import openerp.openerpresourceserver.wms.dto.saleOrder.InventoryItemForOrderRes;

public interface InventoryItemService {
    ApiResponse<Pagination<InventoryItemForOrderRes>> getInventoryItems(int page, int limit, String orderId);

    ApiResponse<Void> createOutboundSaleOrder(CreateOutBounndReq saleOrder, String name);
}

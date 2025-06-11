package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.inventoryItem.InventoryItemForOrderRes;

public interface InventoryItemService {
    ApiResponse<Pagination<InventoryItemForOrderRes>> getInventoryItemsForOutBound(int page, int limit, String orderId);

    ApiResponse<Pagination<InventoryItemForOrderRes>> getInventoryItemsForInBound(int page, int limit, String orderId);
}

package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.CreatePurchaseOrderReq;

public interface PurchaseOrderService {
    ApiResponse<Void> createPurchaseOrder(CreatePurchaseOrderReq purchaseOrder, String name);
}

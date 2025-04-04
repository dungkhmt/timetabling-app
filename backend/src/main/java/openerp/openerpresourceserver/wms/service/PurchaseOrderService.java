package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.PurchaseOrderGetListFilter;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.CreatePurchaseOrderReq;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.PurchaseOrderDetailRes;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.PurchaseOrderListRes;

public interface PurchaseOrderService {
    ApiResponse<Void> createPurchaseOrder(CreatePurchaseOrderReq purchaseOrder, String name);

    ApiResponse<Pagination<PurchaseOrderListRes>> getAllPurchaseOrder(int page, int limit, PurchaseOrderGetListFilter filters);

    ApiResponse<PurchaseOrderDetailRes> getPurchaseOrderDetail(String id);
}

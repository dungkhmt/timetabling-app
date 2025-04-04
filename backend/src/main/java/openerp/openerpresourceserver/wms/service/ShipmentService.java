package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.shipment.*;

public interface ShipmentService {
    ApiResponse<Void> createOutboundSaleOrder(CreateOutBoundReq saleOrder, String name);

    ApiResponse<Pagination<OutBoundByOrderRes>> getOutBoundByOrder(String orderId, int page, int limit);

    ApiResponse<OutBoundDetailRes> getOutBoundDetail(String shipmentId);

    ApiResponse<Void> createInboundPurchaseOrder(CreateInBoundReq purchaseOrder, String name);

    ApiResponse<Pagination<InboundByOrderRes>> getInBoundByOrder(String orderId, int page, int limit);

    ApiResponse<InboundDetailRes> getInBoundDetail(String shipmentId);
}

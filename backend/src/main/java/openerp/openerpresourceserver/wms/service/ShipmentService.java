package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.shipment.*;
import openerp.openerpresourceserver.wms.entity.InventoryItem;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import openerp.openerpresourceserver.wms.entity.UserLogin;

import java.util.List;
import java.util.Map;

public interface ShipmentService {
    ApiResponse<Void> createOutboundSaleOrder(CreateOutBoundReq saleOrder, String name);

    ApiResponse<Pagination<OutBoundByOrderRes>> getOutBoundByOrder(String orderId, int page, int limit);

    ApiResponse<OutBoundDetailRes> getOutBoundDetail(String shipmentId);

    ApiResponse<Void> createInboundPurchaseOrder(CreateInBoundReq purchaseOrder, String name);

    ApiResponse<Pagination<InboundByOrderRes>> getInBoundByOrder(String orderId, int page, int limit);

    ApiResponse<InboundDetailRes> getInBoundDetail(String shipmentId);

    void simulateOuboundShipment(OrderHeader orderHeader, UserLogin userLogin, Map<String, List<InventoryItem>> inventoryItemMap);
}

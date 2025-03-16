package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.shipment.CreateOutBounndReq;
import openerp.openerpresourceserver.wms.dto.shipment.OutBoundByOrderRes;
import openerp.openerpresourceserver.wms.dto.shipment.OutBoundDetailRes;

public interface ShipmentService {
    ApiResponse<Void> createOutboundSaleOrder(CreateOutBounndReq saleOrder, String name);

    ApiResponse<Pagination<OutBoundByOrderRes>> getOutBoundByOrder(String orderId, int page, int limit);

    ApiResponse<OutBoundDetailRes> getOutBoundDetail(String shipmentId);

}

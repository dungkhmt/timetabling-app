package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.saleOrder.CreateSaleOrderReq;
import openerp.openerpresourceserver.wms.dto.saleOrder.OrderListRes;
import openerp.openerpresourceserver.wms.dto.saleOrder.SalesOrderDetailRes;

import java.util.Map;

public interface SaleOrderService {
    ApiResponse<Void> createSaleOrder(CreateSaleOrderReq saleOrder, String name);

    ApiResponse<SalesOrderDetailRes> getSaleOrderDetails(String id);

    ApiResponse<Void> approveSaleOrder(String id, String name);

    ApiResponse<Pagination<OrderListRes>> getAllSaleOrders(int page, int size, Map<String, Object> filters);


    ApiResponse<Pagination<OrderListRes>> getApprovedSaleOrders(int page, int limit);

}

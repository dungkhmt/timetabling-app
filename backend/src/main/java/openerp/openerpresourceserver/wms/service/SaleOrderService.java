package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.SaleOrderGetListFilter;
import openerp.openerpresourceserver.wms.dto.saleOrder.CreateSaleOrderReq;
import openerp.openerpresourceserver.wms.dto.saleOrder.OrderListExportedRes;
import openerp.openerpresourceserver.wms.dto.saleOrder.OrderListRes;
import openerp.openerpresourceserver.wms.dto.saleOrder.SalesOrderDetailRes;

public interface SaleOrderService {
    ApiResponse<Void> createSaleOrder(CreateSaleOrderReq saleOrder, String name);

    ApiResponse<SalesOrderDetailRes> getSaleOrderDetails(String id);

    ApiResponse<Pagination<OrderListRes>> getAllSaleOrders(int page, int size, SaleOrderGetListFilter filters);


    ApiResponse<Pagination<OrderListRes>> getApprovedSaleOrders(int page, int limit);

    ApiResponse<Pagination<OrderListExportedRes>> exportSaleOrders(int page, int limit);

    void simulateSaleOrder() throws InterruptedException;
}

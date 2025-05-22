package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.OrderItemBillingGetListFilter;
import openerp.openerpresourceserver.wms.dto.orderBillItem.OrderItemBillingGetListRes;

public interface OrderBillItemService {
    ApiResponse<Pagination<OrderItemBillingGetListRes>> getAll(int page, int limit, OrderItemBillingGetListFilter filters);
}

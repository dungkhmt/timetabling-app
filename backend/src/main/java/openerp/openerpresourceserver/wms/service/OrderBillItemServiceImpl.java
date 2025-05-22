package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.OrderItemBillingGetListFilter;
import openerp.openerpresourceserver.wms.dto.orderBillItem.OrderItemBillingGetListRes;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.OrderItemBillingRepo;
import openerp.openerpresourceserver.wms.repository.specification.OrderItemBillingSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderBillItemServiceImpl implements OrderBillItemService {
    private final OrderItemBillingRepo orderItemBillingRepo;
    private final GeneralMapper generalMapper;
    @Override
    public ApiResponse<Pagination<OrderItemBillingGetListRes>> getAll(int page, int limit, OrderItemBillingGetListFilter filters) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var orderItemBillingSpec = new OrderItemBillingSpecification(filters);

        var orderItemBillingPage = orderItemBillingRepo.findAll(orderItemBillingSpec, pageReq);

        var orderItemBillingGetListResPage = orderItemBillingPage.getContent().stream().map(
                orderItemBilling ->{
                    var orderItemBillingGetListRes  = generalMapper.convertToDto(orderItemBilling, OrderItemBillingGetListRes.class);
                    orderItemBillingGetListRes.setProductName(orderItemBilling.getProduct().getName());
                    orderItemBillingGetListRes.setProductId(orderItemBilling.getProduct().getId());

                    orderItemBillingGetListRes.setFacilityId(orderItemBilling.getFacility().getId());
                    orderItemBillingGetListRes.setFacilityName(orderItemBilling.getFacility().getName());

                    orderItemBillingGetListRes.setOrderItemId(orderItemBilling.getOrderItem().getId());

                    return orderItemBillingGetListRes;
                } ).toList();

        var pagination = Pagination.<OrderItemBillingGetListRes>builder()
                .page(page)
                .size(limit)
                .totalPages(orderItemBillingPage.getTotalPages())
                .totalElements(orderItemBillingPage.getTotalElements())
                .data(orderItemBillingGetListResPage)
                .build();

        return ApiResponse.<Pagination<OrderItemBillingGetListRes>>builder()
                .code(200)
                .message("Get all order item billing successfully")
                .data(pagination)
                .build();

    }
}

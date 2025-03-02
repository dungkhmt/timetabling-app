package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderType;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.CreateSaleOrderReq;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.repository.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaleOrderServiceImpl implements SaleOrderService{
    private final OrderHeaderRepo orderHeaderRepo;
    private final OrderItemRepo orderItemRepo;
    private final CustomerRepo customerRepo;
    private final FacilityRepo facilityRepo;
    @Override
    public ApiResponse<Void> createSaleOrder(CreateSaleOrderReq saleOrder) {
        var facility = facilityRepo.findById(saleOrder.getFacilityId())
                .orElseThrow(() -> new DataNotFoundException("Facility not found with id: " + saleOrder.getFacilityId()));
        var toCustomer = customerRepo.findById(saleOrder.getCustomerId())
                .orElseThrow(() -> new DataNotFoundException("Customer not found with id" + saleOrder.getCustomerId()));

        // create order header
        var orderHeader = OrderHeader.builder()
                .orderTypeId(OrderType.SALES_ORDER.name())
                .toCustomer(toCustomer)
                .build();

        return null;

    }
}

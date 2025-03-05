package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderType;
import openerp.openerpresourceserver.wms.constant.enumrator.SaleOrderStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.CreateSaleOrderReq;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import openerp.openerpresourceserver.wms.entity.OrderItem;
import openerp.openerpresourceserver.wms.entity.OrderItemPK;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SaleOrderServiceImpl implements SaleOrderService{
    private final OrderHeaderRepo orderHeaderRepo;
    private final OrderItemRepo orderItemRepo;
    private final CustomerRepo customerRepo;
    private final FacilityRepo facilityRepo;
    private final UserLoginRepo userLoginRepo;
    private final ProductRepo productRepo;
    @Override
    public ApiResponse<Void> createSaleOrder(CreateSaleOrderReq request, String name) {
        var facility = facilityRepo.findById(request.getFacilityId())
                .orElseThrow(() -> new DataNotFoundException("Facility not found with id: " + request.getFacilityId()));
        var toCustomer = customerRepo.findById(request.getCustomerId())
                .orElseThrow(() -> new DataNotFoundException("Customer not found with id" + request.getCustomerId()));
        var userLogin = userLoginRepo.findById(request.getUserCreatedId())
                .orElseThrow(() -> new DataNotFoundException("User not found with id" + request.getUserCreatedId()));
        var userCreated = userLoginRepo.findById(name)
                .orElseThrow(() -> new DataNotFoundException("User not found with id" + name));
        // create order header
        var orderHeader = OrderHeader.builder()
                .orderTypeId(OrderType.SALES_ORDER.name())
                .status(SaleOrderStatus.CREATED.name())
                .createdByUser(userCreated)
                .toCustomer(toCustomer)
                .facility(facility)
                .createdByUser(userLogin)
                .build();

        if ((request.getId() == null || request.getId().isEmpty())) {
            orderHeader.setId(CommonUtil.getUUID());

        } else {
            orderHeader.setId(request.getId());
        }

        var orderItems = request.getOrderItems()
                .stream()
                .map(orderItem -> {
                   var product = productRepo.findById(orderItem.getProductId())
                           .orElseThrow(() -> new DataNotFoundException("Product not found in List of OrderItems with id: " + orderItem.getProductId()));
                   var increment = 1;
                   return OrderItem.builder()
                           .order(orderHeader)
                            .product(product)
                            .quantity(orderItem.getQuantity())
                           .amount(product.getWholeSalePrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                           .id(OrderItemPK.builder()
                                   .orderId(orderHeader.getId())
                                   .orderItemSeqId(CommonUtil.getSequenceId("ORDITEM", 4, increment))
                                   .build())
                            .build();
                })
                .toList();


        orderHeaderRepo.save(orderHeader);
        orderItemRepo.saveAll(orderItems);
        return ApiResponse.<Void>builder()
                .code(201)
                .message("Order created successfully")
                .build();

    }
}

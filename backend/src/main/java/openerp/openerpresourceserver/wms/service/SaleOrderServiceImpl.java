package openerp.openerpresourceserver.wms.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderType;
import openerp.openerpresourceserver.wms.constant.enumrator.SaleOrderStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentType;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.saleOrder.*;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.entity.Shipment;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class SaleOrderServiceImpl implements SaleOrderService{
    private final OrderHeaderRepo orderHeaderRepo;
    private final OrderItemRepo orderItemRepo;
    private final CustomerRepo customerRepo;
    private final FacilityRepo facilityRepo;
    private final UserLoginRepo userLoginRepo;
    private final ProductRepo productRepo;
    private final InventoryItemRepo inventoryItemRepo;
    private final InventoryItemDetailRepo inventoryItemDetailRepo;
    private final ShipmentRepo shipmentRepo;
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
        AtomicInteger increment = new AtomicInteger(0);
        var orderItems = request.getOrderItems()
                .stream()
                .map(orderItem -> {
                    var product = productRepo.findById(orderItem.getProductId())
                            .orElseThrow(() -> new DataNotFoundException("Product not found in List of OrderItems with id: " + orderItem.getProductId()));
                    return OrderItem.builder()
                            .order(orderHeader)
                            .product(product)
                            .quantity(orderItem.getQuantity())
                            .amount(product.getWholeSalePrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                            .id(OrderItemPK.builder()
                                    .orderId(orderHeader.getId())
                                    .orderItemSeqId(CommonUtil.getSequenceId("ORDITEM", 4, increment.incrementAndGet())) // Tăng dần đúng cách
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

    @Override
    public ApiResponse<SalesOrderDetailRes> getSaleOrderDetails(String id) {
        var orderHeader = orderHeaderRepo.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + id));

        var orderItems = orderHeader.getOrderItems();
        var orderItemResponses = orderItems
                .stream()
                .map(orderItem -> {
                    var orderItemRes = OrderProductRes.builder()
                            .id(orderItem.getId().getOrderId())
                        .productId(orderItem.getProduct().getId())
                        .quantity(orderItem.getQuantity())
                        .amount(orderItem.getAmount())
                        .build();
                    orderItemRes.setUnit(orderItem.getUnit());
                    orderItemRes.setPrice(orderItem.getPrice());
                    orderItemRes.setDiscount(orderItem.getDiscount());
                    orderItemRes.setTax(orderItem.getTax());
                    return orderItemRes;
                })
                .toList();
        return ApiResponse.<SalesOrderDetailRes>builder()
                .code(200)
                .message("Success")
                .data(SalesOrderDetailRes.builder()
                        .id(orderHeader.getId())
                        .customer(orderHeader.getToCustomer().getName())
                        .facility(orderHeader.getFacility().getName())
                        .createdByUser(orderHeader.getCreatedByUser().getFullName())
                        .createdStamp(orderHeader.getCreatedStamp())
                        .orderItems(orderItemResponses)
                        .build())
                .build();
    }

    @Override
    public ApiResponse<Void> approveSaleOrder(String id, String name) {
        var orderHeader = orderHeaderRepo.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + id));
        orderHeader.setStatus(SaleOrderStatus.APPROVED.name());

        var userApproved = userLoginRepo.findById(name)
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + name));
        orderHeader.setUserApproved(userApproved);

        orderHeaderRepo.save(orderHeader);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Order approved successfully")
                .build();
    }

    @Override
    public ApiResponse<Pagination<OrderListRes>> getAllSaleOrders(int page, int size, Map<String, Object> filters) {
        var pageable = PageRequest.of(page, size);
        var orderHeaders = orderHeaderRepo.findAll(pageable);

        var orderListRes = orderHeaders
                .stream()
                .map(orderHeader -> OrderListRes.builder()
                        .id(orderHeader.getId())
                        .customerName(orderHeader.getToCustomer().getName())
                        .facilityName(orderHeader.getFacility().getName())
                        .createdStamp(orderHeader.getCreatedStamp())
                        .status(orderHeader.getStatus())
                        .totalAmount(orderHeader.getOrderItems()
                                .stream()
                                .map(OrderItem::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .build())
                .toList();

        return ApiResponse.<Pagination<OrderListRes>>builder()
                .code(200)
                .message("Success")
                .data(Pagination.<OrderListRes>builder()
                        .page(page)
                        .size(size)
                        .data(orderListRes)
                        .totalElements(orderHeaders.getTotalElements())
                        .totalPages(orderHeaders.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public ApiResponse<Void> createOutboundSaleOrder(CreateOutBounndReq req, String name) {
        var order = orderHeaderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + req.getOrderId()));

        var orderSeq = orderItemRepo.findById(req.getOrderItemSeqId())
                .orElseThrow(() -> new DataNotFoundException("OrderItem not found with id: " + req.getOrderItemSeqId()));

        var userLogin = userLoginRepo.findById(name)
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + name));

        var inventoryItem = inventoryItemRepo.findById(req.getInventoryItemId())
                .orElseThrow(() -> new DataNotFoundException("InventoryItem not found with id: " + req.getInventoryItemId()));

        var product = productRepo.findById(req.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + orderSeq.getProduct().getId()));

        if(orderSeq.getQuantity() < req.getQuantity()) {
            throw new RuntimeException("Quantity not available in stock");
        }

        inventoryItemRepo.save(inventoryItem);
        var shipment = Shipment.builder()
                .id(CommonUtil.getUUID())
                .shipmentTypeId(ShipmentType.OUTBOUND.name())
                .createdByUser(userLogin)
                .order(order)
                .toCustomer(order.getToCustomer())
                .build();
        var inventoryItemDetail = InventoryItemDetail.builder()
                .inventoryItem(inventoryItem)
                .quantity(req.getQuantity())
                .product(product)
                .shipment(shipment)
                .build();
        shipmentRepo.save(shipment);
        inventoryItemDetailRepo.save(inventoryItemDetail);

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Create Outbound Sale Order successfully")
                .build();

    }

    @Override
    public ApiResponse<Pagination<OrderListRes>> getApprovedSaleOrders(int page, int limit) {
        var pageable = PageRequest.of(page, limit);
        var orderHeaders = orderHeaderRepo.findAllByStatus(SaleOrderStatus.APPROVED.name(), pageable);

        var orderListRes = orderHeaders
                .stream()
                .map(orderHeader -> OrderListRes.builder()
                        .id(orderHeader.getId())
                        .customerName(orderHeader.getToCustomer().getName())
                        .facilityName(orderHeader.getFacility().getName())
                        .createdStamp(orderHeader.getCreatedStamp())
                        .status(orderHeader.getStatus())
                        .totalAmount(orderHeader.getOrderItems()
                                .stream()
                                .map(OrderItem::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .build())
                .toList();

        return ApiResponse.<Pagination<OrderListRes>>builder()
                .code(200)
                .message("Success")
                .data(Pagination.<OrderListRes>builder()
                        .page(page)
                        .size(limit)
                        .data(orderListRes)
                        .totalElements(orderHeaders.getTotalElements())
                        .totalPages(orderHeaders.getTotalPages())
                        .build())
                .build();

    }

}

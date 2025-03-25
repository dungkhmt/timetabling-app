package openerp.openerpresourceserver.wms.service;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderType;
import openerp.openerpresourceserver.wms.constant.enumrator.SaleOrderStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.SaleOrderGetListFilter;
import openerp.openerpresourceserver.wms.dto.saleOrder.*;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.repository.specification.SaleOrderSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
                .saleChannelId(request.getSaleChannel())
                .orderName(request.getSaleOrderName())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryAfterDate(request.getDeliveryAfterDate())
                .deliveryBeforeDate(request.getDeliveryBeforeDate())
                .note(request.getNote())
                .createdByUser(userLogin)
                .deliveryPhone(request.getDeliveryPhone())
                .build();

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
                            .amount(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                            .id(CommonUtil.getUUID())
                            .orderItemSeqId(CommonUtil.getSequenceId("ORDITM",5, increment.incrementAndGet()))
                            .price(orderItem.getPrice())
                            .unit(orderItem.getUnit())
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
        AtomicInteger totalQuantity = new AtomicInteger(0);
        AtomicDouble totalAmount = new AtomicDouble(0);
        var orderItemResponses = orderItems
                .stream()
                .map(orderItem -> {
                    var orderItemRes = OrderProductRes.builder()
                            .id(orderItem.getId())
                            .orderItemSeqId(orderItem.getOrderItemSeqId())
                        .productId(orderItem.getProduct().getId())
                        .productName(orderItem.getProduct().getName())
                        .quantity(orderItem.getQuantity())
                        .amount(orderItem.getAmount())
                        .build();
                    orderItemRes.setUnit(orderItem.getUnit());
                    orderItemRes.setPrice(orderItem.getPrice());
                    orderItemRes.setDiscount(orderItem.getDiscount());
                    orderItemRes.setTax(orderItem.getTax());
                    orderItemRes.setAmount(orderItem.getAmount());
                    totalAmount.addAndGet(orderItem.getQuantity());
                    totalAmount.addAndGet(orderItem.getAmount().doubleValue());
                    return orderItemRes;
                })
                .toList();
        return ApiResponse.<SalesOrderDetailRes>builder()
                .code(200)
                .message("Success")
                .data(SalesOrderDetailRes.builder()
                        .id(orderHeader.getId())
                        .customerName(orderHeader.getToCustomer().getName())
                        .facilityName(orderHeader.getFacility().getName())
                        .createdByUser(orderHeader.getCreatedByUser().getFullName())
                        .createdStamp(orderHeader.getCreatedStamp())
                        .status(orderHeader.getStatus())
                        .note(orderHeader.getNote())
                        .deliveryAddress(orderHeader.getDeliveryAddress())
                        .deliveryAfterDate(orderHeader.getDeliveryAfterDate())
                        .priority(orderHeader.getPriority())
                        .totalAmount(BigDecimal.valueOf(totalAmount.get()))
                        .totalQuantity(totalQuantity.get())
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
    public ApiResponse<Pagination<OrderListRes>> getAllSaleOrders(int page, int size, SaleOrderGetListFilter filters) {
        var pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdStamp"));

        var saleOrderSpec = new SaleOrderSpecification(filters);
        var orderHeaders = orderHeaderRepo.findAll(saleOrderSpec ,pageable);

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

    @Override
    public ApiResponse<Pagination<OrderListExportedRes>> exportSaleOrders(int page, int limit) {
        var pageable = PageRequest.of(page-1, limit);
        var orderHeaders = orderHeaderRepo.findAll(pageable);

        var orderListExportedResponses = orderHeaders
                .stream()
                .map(orderHeader -> {
                    AtomicInteger totalQuantity = new AtomicInteger(0);
                    AtomicDouble totalAmount = new AtomicDouble(0);

                    orderHeader.getOrderItems().forEach(orderItem -> {
                        totalQuantity.addAndGet(orderItem.getQuantity());
                        totalAmount.addAndGet(orderItem.getAmount().doubleValue());
                    });

                    return OrderListExportedRes.builder()
                            .id(orderHeader.getId())
                            .customerName(orderHeader.getToCustomer().getName())
                            .createdStamp(orderHeader.getCreatedStamp())
                            .status(orderHeader.getStatus())
                            .customerId(orderHeader.getToCustomer().getId())
                            .orderName(orderHeader.getOrderName())
                            .deliveryAddress(orderHeader.getDeliveryAddress())
                            .deliveryPhone(orderHeader.getDeliveryPhone())
                            .deliveryAfterDate(orderHeader.getDeliveryAfterDate())
                            .saleChannelId(orderHeader.getSaleChannelId())
                            .totalQuantity(totalQuantity.get()) // Gán tổng số lượng
                            .totalAmount(BigDecimal.valueOf(totalAmount.get())) // Gán tổng tiền
                            .build();
                })
                .toList();
        return ApiResponse.<Pagination<OrderListExportedRes>>builder()
                .code(200)
                .message("Get all sale orders to export successfully")
                .data(Pagination.<OrderListExportedRes>builder()
                        .page(page)
                        .size(limit)
                        .data(orderListExportedResponses)
                        .totalElements(orderHeaders.getTotalElements())
                        .totalPages(orderHeaders.getTotalPages())
                        .build())
                .build();
    }

}

package openerp.openerpresourceserver.wms.service;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderType;
import openerp.openerpresourceserver.wms.constant.enumrator.SaleChannel;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.SaleOrderGetListFilter;
import openerp.openerpresourceserver.wms.dto.saleOrder.*;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.repository.specification.SaleOrderSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static openerp.openerpresourceserver.wms.constant.Constants.DEFAULT_ADMIN_USER_NAME;
import static openerp.openerpresourceserver.wms.util.CommonUtil.getAllWeeklyStartDates;
import static openerp.openerpresourceserver.wms.util.CommonUtil.getRandomElements;

@Service
@RequiredArgsConstructor
public class SaleOrderServiceImpl implements SaleOrderService{
    private final OrderHeaderRepo orderHeaderRepo;
    private final OrderItemRepo orderItemRepo;
    private final CustomerRepo customerRepo;
    private final FacilityRepo facilityRepo;
    private final UserLoginRepo userLoginRepo;
    private final ProductRepo productRepo;
    private final ShipmentService shipmentService;
    private final InventoryItemRepo inventoryItemRepo;
    @Qualifier("customExecutor")
    private final ThreadPoolTaskExecutor executor;
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
                .status(OrderStatus.CREATED.name())
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
                            .id(CommonUtil.getUUID())
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
        orderHeader.setStatus(OrderStatus.APPROVED.name());

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
        var orderHeaders = orderHeaderRepo.findAllByStatus(OrderStatus.APPROVED.name(), pageable);

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

    @Override
    @Transactional
    public void simulateSaleOrder() throws InterruptedException {
        List<LocalDate> weeks = getAllWeeklyStartDates(LocalDate.now().minusYears(2), LocalDate.now());
        List<Customer> customers = customerRepo.findAll();
        List<Facility> facilities = facilityRepo.findAll();
        List<Product> products = productRepo.findAll();
        List<SaleChannel> saleChannels = List.of(SaleChannel.values());
        UserLogin userLogin = userLoginRepo.findById(DEFAULT_ADMIN_USER_NAME)
                .orElseThrow(() -> new DataNotFoundException("Default admin user not found"));
        List<InventoryItem> inventoryItems = inventoryItemRepo.findAll();
        // Map inventory Items to Map <ProductId, List<InventoryItem>>
        var inventoryItemMap = inventoryItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getId()));

        for (LocalDate weekStart : weeks) {
            for (int i = 0; i < 7; i++) {
                LocalDate day = weekStart.plusDays(i);

                var numberOfCustomers = ThreadLocalRandom.current().nextInt(1, 51);
                List<Customer> assignedCustomers = getRandomElements(customers, 0, numberOfCustomers);

                for (Customer customer : assignedCustomers) {
                    LocalDateTime timestamp = day.atTime(
                            ThreadLocalRandom.current().nextInt(0, 24),
                            ThreadLocalRandom.current().nextInt(0, 60));

                    executor.execute(() -> {
                        var orderHeader = saveOrderForCustomer(customer, timestamp, facilities, saleChannels, products, userLogin);
                        if (orderHeader == null) {
                            return;
                        }
                        shipmentService.simulateOuboundShipment(orderHeader, userLogin, inventoryItemMap);
                    });
                }

                // Sleep for a short duration to avoid overwhelming the system
                Thread.sleep(100);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderHeader saveOrderForCustomer(Customer customer, LocalDateTime orderDateTime, List<Facility> facilities,
                                     List<SaleChannel> saleChannels, List<Product> products, UserLogin userLogin) {
        try {
            Facility facility = CommonUtil.getRandomElement(facilities);
            SaleChannel saleChannel = CommonUtil.getRandomElement(saleChannels);

            OrderHeader orderHeader = OrderHeader.builder()
                    .id(CommonUtil.getUUID())
                    .orderTypeId(OrderType.SALES_ORDER.name())
                    .status(OrderStatus.CREATED.name())
                    .toCustomer(customer)
                    .facility(facility)
                    .saleChannelId(saleChannel.name())
                    .orderName("Simulated Order " + orderDateTime)
                    .deliveryAddress(customer.getAddress())
                    .deliveryPhone(customer.getPhone())
                    .deliveryAfterDate(orderDateTime.toLocalDate().plusDays(1))
                    .deliveryBeforeDate(orderDateTime.toLocalDate().plusDays(5))
                    .createdByUser(userLogin)
                    .build();

            orderHeader.setCreatedStamp(orderDateTime);
            orderHeaderRepo.save(orderHeader);

            int numItems = ThreadLocalRandom.current().nextInt(1, 6);
            List<Product> selectedProducts = CommonUtil.getRandomElements(products, 0, numItems);

            AtomicInteger increment = new AtomicInteger(0);
            List<OrderItem> orderItems = selectedProducts.stream()
                    .map(product -> {
                        int quantity = ThreadLocalRandom.current().nextInt(1, 100);
                        BigDecimal price = product.getWholeSalePrice();

                        var orderItem =  OrderItem.builder()
                                .id(CommonUtil.getUUID())
                                .order(orderHeader)
                                .product(product)
                                .quantity(quantity)
                                .price(price)
                                .amount(price.multiply(BigDecimal.valueOf(quantity)))
                                .unit("Cái")
                                .orderItemSeqId(CommonUtil.getSequenceId("ORDITM", 5, increment.incrementAndGet()))
                                .build();
                        orderItem.setCreatedStamp(orderDateTime);
                        return orderItem;
                    })
                    .toList();

            orderHeader.setOrderItems(orderItems);
            orderItemRepo.saveAll(orderItems);

            return orderHeader;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

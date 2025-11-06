package openerp.openerpresourceserver.wms.service;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.algorithm.SnowFlakeIdGenerator;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderType;
import openerp.openerpresourceserver.wms.constant.enumrator.SaleChannel;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.OrderItemReq;
import openerp.openerpresourceserver.wms.dto.OrderProductRes;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.SaleOrderGetListFilter;
import openerp.openerpresourceserver.wms.dto.saleOrder.CreateSaleOrderReq;
import openerp.openerpresourceserver.wms.dto.saleOrder.OrderListExportedRes;
import openerp.openerpresourceserver.wms.dto.saleOrder.OrderListRes;
import openerp.openerpresourceserver.wms.dto.saleOrder.SalesOrderDetailRes;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.micrometer.common.util.StringUtils.isBlank;
import static openerp.openerpresourceserver.wms.constant.Constants.DEFAULT_ADMIN_USER_NAME;
import static openerp.openerpresourceserver.wms.constant.Constants.ORDER_ITEM_ID_PREFIX;
import static openerp.openerpresourceserver.wms.util.CommonUtil.getAllWeeklyStartDates;

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
    private final GeneralMapper generalMapper;
    @Qualifier("customExecutor")
    private final ThreadPoolTaskExecutor executor;
    private final AddressRepo addressRepo;

    @Override
    public ApiResponse<Void> createSaleOrder(CreateSaleOrderReq request, String name) {
        var toCustomer = customerRepo.findById(request.getToCustomerId())
                .orElseThrow(() -> new DataNotFoundException("Customer not found with id" + request.getToCustomerId()));
        var userCreated = userLoginRepo.findById(name)
                .orElseThrow(() -> new DataNotFoundException("User not found with id" + name));
        var orderHeader = generalMapper.convertToEntity(request, OrderHeader.class);

        if(isBlank(orderHeader.getId())) {
            orderHeader.setId(SnowFlakeIdGenerator.getInstance().nextId(ORDER_ITEM_ID_PREFIX));
        }

        orderHeader.setOrderTypeId(OrderType.SALES_ORDER.name());
        orderHeader.setStatusId(OrderStatus.CREATED.name());
        orderHeader.setToCustomer(toCustomer);
        orderHeader.setCreatedByUser(userCreated);

        var productIds = request.getOrderItems()
                .stream()
                .map(OrderItemReq::getProductId)
                .distinct()
                .toList();

        List<Product> products = productRepo.findAllById(productIds);

        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        var totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        var seq = 1;
        var orderItems = new ArrayList<OrderItem>();

        for (OrderItemReq orderItemReq : request.getOrderItems()) {
            var product = productMap.get(orderItemReq.getProductId());
            if (product == null) {
                throw new DataNotFoundException("Product not found in List of OrderItems with id: " + orderItemReq.getProductId());
            }

            var orderItem = generalMapper.convertToEntity(orderItemReq, OrderItem.class);
            orderItem.setOrder(orderHeader);
            orderItem.setProduct(product);
            orderItem.setOrderItemSeqId(seq++);
            orderItem.setId(SnowFlakeIdGenerator.getInstance().nextId(ORDER_ITEM_ID_PREFIX));
            totalQuantity += orderItem.getQuantity();
            var amount = (orderItem.getPrice()
                    .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                    .subtract(orderItem.getDiscount());
            orderItem.setAmount(amount);
            orderItems.add(orderItem);

            totalAmount = totalAmount.add(amount);
        }

        totalAmount.subtract(request.getDiscount());

        orderHeader.setTotalQuantity(totalQuantity);
        orderHeader.setTotalAmount(totalAmount);

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

        var saleOrderDetailRes = generalMapper.convertToDto(orderHeader, SalesOrderDetailRes.class);
        var orderItems = orderHeader.getOrderItems();
        List<OrderProductRes> orderItemResponses = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            var orderItemRes = generalMapper.convertToDto(orderItem, OrderProductRes.class);
            orderItemRes.setProductId(orderItem.getProduct().getId());
            orderItemRes.setProductName(orderItem.getProduct().getName());
            orderItemResponses.add(orderItemRes);
        }

        saleOrderDetailRes.setCreatedByUserName(orderHeader.getCreatedByUser().getFullName());
        saleOrderDetailRes.setToCustomerName(orderHeader.getToCustomer().getName());
        saleOrderDetailRes.setOrderItems(orderItemResponses);
        return ApiResponse.<SalesOrderDetailRes>builder()
                .code(200)
                .message("Success")
                .data(saleOrderDetailRes)
                .build();
    }

    @Override
    public ApiResponse<Pagination<OrderListRes>> getAllSaleOrders(int page, int size, SaleOrderGetListFilter filters) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdStamp"));

        var saleOrderSpec = new SaleOrderSpecification(filters);
        var orderHeaders = orderHeaderRepo.findAll(saleOrderSpec ,pageable);

        var orderListRes = orderHeaders
                .<OrderListRes>stream()
                .map(orderHeader ->
                {
                    var orderRes = generalMapper.convertToDto(orderHeader, OrderListRes.class);
                    orderRes.setCustomerName(orderHeader.getToCustomer().getName());
                    orderRes.setUserCreatedName(orderHeader.getCreatedByUser().getFullName());
                    return orderRes;
                })
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
                            .status(orderHeader.getStatusId())
                            .customerId(orderHeader.getToCustomer().getId())
                            .orderName(orderHeader.getOrderName())
                            .deliveryAddress(orderHeader.getDeliveryAddressId())
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
        LocalDate to = LocalDate.of(2025, 7, 5);
        LocalDate from = LocalDate.of(2023, 7, 5);
        List<LocalDate> weeks = getAllWeeklyStartDates(from, to);
        List<Customer> customers = customerRepo.findAll();
        List<Facility> facilities = facilityRepo.findAll();
        List<Product> products = productRepo.findAll();
        List<SaleChannel> saleChannels = List.of(SaleChannel.values());
        UserLogin userLogin = userLoginRepo.findById(DEFAULT_ADMIN_USER_NAME)
                .orElseThrow(() -> new DataNotFoundException("Default admin user not found"));
        List<InventoryItem> inventoryItems = inventoryItemRepo.findAll();

        List<Address> addresses = addressRepo.findAll();

        //map addresses to customers
        Map<String, Address> addressMap = addresses.stream()
                .collect(Collectors.toMap(Address::getId, Function.identity()));

        for (LocalDate weekStart : weeks) {
            for (int i = 0; i < 7; i++) {
                LocalDate day = weekStart.plusDays(i);

//                var numberOfCustomers = ThreadLocalRandom.current().nextInt(1, customers.size()-1);
//                List<Customer> assignedCustomers = getRandomElements(customers, 0, numberOfCustomers);

                for (Product product : products) {
                    LocalDateTime timestamp = day.atTime(
                            ThreadLocalRandom.current().nextInt(0, 24),
                            ThreadLocalRandom.current().nextInt(0, 60));

                    executor.execute(() -> {
                        var orderHeader = saveOrderForProduct(customers, timestamp,  addressMap, saleChannels, product, userLogin);
                        if (orderHeader == null) {
                            return;
                        }
                        shipmentService.simulateOuboundShipment(orderHeader, userLogin, facilities);
                    });
                }

                // Sleep for a short duration to avoid overwhelming the system
                Thread.sleep(100);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderHeader saveOrderForProduct(List<Customer> assignedCustomers, LocalDateTime orderDateTime, Map<String, Address> addressMap,
                                           List<SaleChannel> saleChannels, Product assignedProduct, UserLogin userLogin) {
        try {
            SaleChannel saleChannel = CommonUtil.getRandomElement(saleChannels);
            Customer assignedCustomer = CommonUtil.getRandomElement(assignedCustomers);

            OrderHeader orderHeader = OrderHeader.builder()
                    .id(SnowFlakeIdGenerator.getInstance().nextId("SIMULATED_ORD"))
                    .orderTypeId(OrderType.SALES_ORDER.name())
                    .statusId(OrderStatus.CREATED.name())
                    .toCustomer(assignedCustomer)
                    .saleChannelId(saleChannel.name())
                    .orderName("Simulated Order " + orderDateTime)
                    .deliveryAddressId(assignedCustomer.getCurrentAddressId())
                    .deliveryFullAddress(addressMap.get(assignedCustomer.getCurrentAddressId()).getFullAddress())
                    .deliveryPhone(assignedCustomer.getPhone())
                    .deliveryAfterDate(orderDateTime.toLocalDate().plusDays(1))
                    .deliveryBeforeDate(orderDateTime.toLocalDate().plusDays(5))
                    .createdByUser(userLogin)
                    .build();

            orderHeader.setCreatedStamp(orderDateTime);
            orderHeaderRepo.save(orderHeader);

//            int numItems = ThreadLocalRandom.current().nextInt(1, 6);
//            List<Product> selectedProducts = CommonUtil.getRandomElements(products, 0, numItems);

            AtomicInteger increment = new AtomicInteger(0);
            List<OrderItem> orderItems = List.of(assignedProduct).stream()
                    .map(product -> {
                        int quantity = ThreadLocalRandom.current().nextInt(100, 501);
                        BigDecimal price = product.getWholeSalePrice();

                        var orderItem =  OrderItem.builder()
                                .id(SnowFlakeIdGenerator.getInstance().nextId(ORDER_ITEM_ID_PREFIX))
                                .order(orderHeader)
                                .product(product)
                                .quantity(quantity)
                                .price(price)
                                .amount(price.multiply(BigDecimal.valueOf(quantity)))
                                .unit("Cái")
                                .orderItemSeqId(increment.incrementAndGet())
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

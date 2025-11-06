package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.wms.algorithm.SnowFlakeIdGenerator;
import openerp.openerpresourceserver.wms.constant.enumrator.EntityType;
import openerp.openerpresourceserver.wms.constant.enumrator.PartnerType;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentType;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.ShipmentGetListFilter;
import openerp.openerpresourceserver.wms.dto.shipment.*;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.repository.specification.ShipmentSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import openerp.openerpresourceserver.wms.vrp.TimeDistance;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static io.micrometer.common.util.StringUtils.isBlank;
import static openerp.openerpresourceserver.wms.constant.Constants.INVENTORY_ITEM_DETAIL_ID_PREFIX;
import static openerp.openerpresourceserver.wms.constant.Constants.SHIPMENT_ID_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {
    private final UserLoginRepo userLoginRepo;
    private final ShipmentRepo shipmentRepo;
    private final InventoryItemDetailRepo inventoryItemDetailRepo;
    private final InventoryItemRepo inventoryItemRepo;
    private final ProductRepo productRepo;
    private final OrderHeaderRepo orderHeaderRepo;
    private final GeneralMapper generalMapper;
    private final FacilityRepo facilityRepo;
    private final OrderItemRepo orderItemRepo;
    private final AddressRepo addressRepo;
    private final DeliveryBillService deliveryBillService;

    @Override
    public ApiResponse<Void> createOutboundSaleOrder(CreateOutBoundReq req, String name) {
        OrderHeader orderHeader = orderHeaderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + req.getOrderId()));

        var productReqs = req.getProducts();

        var productIds = productReqs.stream().map(CreateOutBoundProductReq::getProductId).toList();
        var facilityIds = productReqs.stream().map(CreateOutBoundProductReq::getFacilityId).toList();
        var orderItemIds = productReqs.stream().map(CreateOutBoundProductReq::getOrderItemId).toList();

        Map<String, Product> productMap = productRepo.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Map<String, Facility> facilityMap = facilityRepo.findAllById(facilityIds).stream()
                .collect(Collectors.toMap(Facility::getId, f -> f));

        Map<String, OrderItem> orderItemMap = orderItemRepo.findAllById(orderItemIds).stream()
                .collect(Collectors.toMap(OrderItem::getId, i -> i));

        Shipment shipment = generalMapper.convertToEntity(req, Shipment.class);
        if (isBlank(shipment.getId())) {
            shipment.setId(SnowFlakeIdGenerator.getInstance().nextId(SHIPMENT_ID_PREFIX));
        }

        shipment.setShipmentTypeId(ShipmentType.OUTBOUND.name());
        shipment.setStatusId(ShipmentStatus.CREATED.name());
        shipment.setOrder(orderHeader);
        shipment.setToCustomer(orderHeader.getToCustomer());

        shipment.setCreatedByUser(userLoginRepo.findById(name)
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + name)));

        List<InventoryItemDetail> detailList = new ArrayList<>();
        BigDecimal totalWeight = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (var productReq : productReqs) {
            Product product = productMap.get(productReq.getProductId());
            Facility facility = facilityMap.get(productReq.getFacilityId());
            OrderItem orderItem = orderItemMap.get(productReq.getOrderItemId());

            if (product == null) {
                throw new DataNotFoundException("Product not found with id: " + productReq.getProductId());
            }
            if (facility == null) {
                throw new DataNotFoundException("Facility not found with id: " + productReq.getFacilityId());
            }
            if (orderItem == null) {
                throw new DataNotFoundException("Order item not found with id: " + productReq.getOrderItemId());
            }
            if (orderItem.getQuantity() < productReq.getQuantity()) {
                throw new DataNotFoundException("Not enough quantity in order item for productId: " + productReq.getProductId());
            }

            InventoryItemDetail detail = generalMapper.convertToEntity(productReq, InventoryItemDetail.class);
            detail.setId(SnowFlakeIdGenerator.getInstance().nextId(INVENTORY_ITEM_DETAIL_ID_PREFIX));
            detail.setProduct(product);
            detail.setFacility(facility);
            detail.setShipment(shipment);
            detail.setOrderItem(orderItem);
            detail.setUnit(orderItem.getUnit());
            detail.setPrice(orderItem.getPrice());

            totalWeight = totalWeight.add(product.getWeight()
                    .multiply(BigDecimal.valueOf(detail.getQuantity())));
            totalQuantity += detail.getQuantity();

            detailList.add(detail);
        }

        shipment.setTotalWeight(totalWeight);
        shipment.setTotalQuantity(totalQuantity);

        shipmentRepo.save(shipment);
        inventoryItemDetailRepo.saveAll(detailList);

        return ApiResponse.<Void>builder()
                .code(201)
                .message("Create outbound sale order success")
                .build();
    }

    @Override
    public ApiResponse<Pagination<OutBoundByOrderRes>> getOutBoundByOrder(String orderId, int page, int limit) {
        var pageRequest = CommonUtil.getPageRequest(page, limit);
        var shipments = shipmentRepo.findByOrderId(orderId, pageRequest);

        List<OutBoundByOrderRes> outBoundByOrderPageRes = shipments.getContent().stream()
                .map(shipment -> {
                    var res = generalMapper.convertToDto(shipment, OutBoundByOrderRes.class);
                    res.setToCustomerName(shipment.getToCustomer().getName());
                    return res;
                })
                .toList();

        return ApiResponse.<Pagination<OutBoundByOrderRes>>builder()
                .code(200)
                .message("Get outbound by order success")
                .data(Pagination.<OutBoundByOrderRes>builder()
                        .page(page)
                        .size(limit)
                        .data(outBoundByOrderPageRes)
                        .totalElements(shipments.getTotalElements())
                        .totalPages(shipments.getTotalPages())
                        .build())
                .build();
    }


    @Override
    public ApiResponse<OutBoundDetailRes> getOutBoundDetail(String shipmentId) {
        var shipment = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Shipment not found with id: " + shipmentId));

        var inventoryItemDetails = inventoryItemDetailRepo.findByShipmentId(shipmentId);

        List<InboundDetailProductRes> products = inventoryItemDetails.stream()
                .map(inventoryItemDetail -> {
                    var productRes = generalMapper.convertToDto(inventoryItemDetail, InboundDetailProductRes.class);
                    productRes.setProductId(inventoryItemDetail.getProduct().getId());
                    productRes.setProductName(inventoryItemDetail.getProduct().getName());
                    productRes.setRequestedQuantity(inventoryItemDetail.getOrderItem().getQuantity());
                    productRes.setFacilityId(inventoryItemDetail.getFacility().getId());
                    productRes.setFacilityName(inventoryItemDetail.getFacility().getName());
                    return productRes;
                }).toList();

        var outboundDetailRes = generalMapper.convertToDto(shipment, OutBoundDetailRes.class);
        outboundDetailRes.setToCustomerName(shipment.getToCustomer().getName());
        outboundDetailRes.setProducts(products);
        var orderHeader = shipment.getOrder();
        outboundDetailRes.setDeliveryFullAddress(orderHeader.getDeliveryFullAddress());
        outboundDetailRes.setDeliveryPhone(orderHeader.getDeliveryPhone());

        return ApiResponse.<OutBoundDetailRes>builder()
                .code(200)
                .message("Get outbound detail success")
                .data(outboundDetailRes)
                .build();
    }


    @Override
    public ApiResponse<Void> createInboundPurchaseOrder(CreateInBoundReq req, String name) {
        // logic tương tự như createOutboundSaleOrder
        OrderHeader orderHeader = orderHeaderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + req.getOrderId()));

        var orderItems = orderHeader.getOrderItems();
        var productReqs = req.getProducts();

        var productIds = productReqs.stream()
                .map(CreateInBoundProductReq::getProductId)
                .toList();

        var facilityIds = productReqs.stream()
                .map(CreateInBoundProductReq::getFacilityId)
                .toList();


        Map<String, Product> productMap = productRepo.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        Map<String, Facility> facilityMap = facilityRepo.findAllById(facilityIds).stream()
                .collect(Collectors.toMap(Facility::getId, facility -> facility));

        var shipment = generalMapper.convertToEntity(req, Shipment.class);
        if(isBlank(shipment.getId())) shipment.setId(SnowFlakeIdGenerator.getInstance().nextId(SHIPMENT_ID_PREFIX));

        shipment.setStatusId(ShipmentStatus.CREATED.name());
        shipment.setShipmentTypeId(ShipmentType.INBOUND.name());
        shipment.setFromSupplier(orderHeader.getFromSupplier());
        shipment.setOrder(orderHeader);
        shipment.setCreatedByUser(userLoginRepo.findById(name).orElseThrow(
                () -> new DataNotFoundException("User not found with id: " + name)
        ));

        var inventoryItemDetailDOs = new ArrayList<InventoryItemDetail>();
        BigDecimal totalWeight = BigDecimal.ZERO;
        var totalQuantity = 0;

        for (var shipmentItem : productReqs) {
            Product productEntity = productMap.get(shipmentItem.getProductId());
            Facility facility = facilityMap.get(shipmentItem.getFacilityId());
            // Kiếm tra số lượng sản phẩm trong OrderItem
            var orderItem = orderItems.stream()
                    .filter(item -> item.getProduct().getId().equals(shipmentItem.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new DataNotFoundException("Order item not found with productId: " + shipmentItem.getProductId()));

            // Kiểm tra số lượng sản phẩm trong OrderItem
            if (orderItem.getQuantity() < shipmentItem.getQuantity()) {
                throw new DataNotFoundException("Not enough quantity in order item");
            }

            var inventoryItemDetail = generalMapper.convertToEntity(shipmentItem, InventoryItemDetail.class);
            inventoryItemDetail.setId(SnowFlakeIdGenerator.getInstance().nextId(INVENTORY_ITEM_DETAIL_ID_PREFIX));
            inventoryItemDetail.setFacility(facility);
            inventoryItemDetail.setProduct(productEntity);
            inventoryItemDetail.setShipment(shipment);
            inventoryItemDetail.setPrice(orderItem.getPrice());
            inventoryItemDetail.setUnit(orderItem.getUnit());
            inventoryItemDetail.setOrderItem(orderItem);

            totalWeight = totalWeight.add((inventoryItemDetail.getProduct().getWeight())
                    .multiply(BigDecimal.valueOf(inventoryItemDetail.getQuantity())));
            totalQuantity += inventoryItemDetail.getQuantity();

            inventoryItemDetailDOs.add(inventoryItemDetail);
        }
        shipment.setTotalWeight(totalWeight);
        shipment.setTotalQuantity(totalQuantity);

        shipmentRepo.save(shipment);
        inventoryItemDetailRepo.saveAll(inventoryItemDetailDOs);

        return ApiResponse.<Void>builder()
                .code(201)
                .message("Create outbound sale order success")
                .build();
    }

    @Override
    public ApiResponse<Pagination<InboundByOrderRes>> getInBoundByOrder(String orderId, int page, int limit) {
        var pageRequest = CommonUtil.getPageRequest(page, limit);
        var shipments = shipmentRepo.findByOrderId(orderId, pageRequest);

        List<InboundByOrderRes> inBoundByOrderPageRes = shipments.getContent().stream()
                .map(shipment ->
                        {
                            var res = generalMapper.convertToDto(shipment, InboundByOrderRes.class);
                            res.setFromSupplierName(shipment.getFromSupplier().getName());
                            return res;
                        }
                ).toList();

        return ApiResponse.<Pagination<InboundByOrderRes>>builder()
                .code(200)
                .message("Get outbound by order success")
                .data(Pagination.<InboundByOrderRes>builder()
                        .page(page)
                        .size(limit)
                        .data(inBoundByOrderPageRes)
                        .totalElements(shipments.getTotalElements())
                        .totalPages(shipments.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public ApiResponse<InboundDetailRes> getInBoundDetail(String shipmentId) {
        var shipment = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Shipment not found with id: " + shipmentId));

        var inventoryItemDetails = inventoryItemDetailRepo.findByShipmentId(shipmentId);
        List<InboundDetailProductRes> products = inventoryItemDetails.stream()
                .map(inventoryItemDetail ->
                {
                    var inboundDetailProduct = generalMapper.convertToDto(inventoryItemDetail, InboundDetailProductRes.class);
                    inboundDetailProduct.setProductId(inventoryItemDetail.getProduct().getId());
                    inboundDetailProduct.setProductName(inventoryItemDetail.getProduct().getName());
                    inboundDetailProduct.setRequestedQuantity(inventoryItemDetail.getOrderItem().getQuantity());
                    inboundDetailProduct.setFacilityId(inventoryItemDetail.getFacility().getId());
                    inboundDetailProduct.setFacilityName(inventoryItemDetail.getFacility().getName());
                    return inboundDetailProduct;
                }
                ).toList();

        var inboundDetailRes = generalMapper.convertToDto(shipment, InboundDetailRes.class);
        inboundDetailRes.setFromSupplierName(shipment.getFromSupplier().getName());
        inboundDetailRes.setProducts(products);

        return ApiResponse.<InboundDetailRes>builder()
                .code(200)
                .message("Get outbound detail success")
                .data(inboundDetailRes)
                .build();
    }

    @Override
    public void simulateOuboundShipment(OrderHeader orderHeader, UserLogin userLogin, List<Facility> facilities) {
        try {
            // Prepare shipment entity
            Shipment shipment = Shipment.builder()
                    .id(SnowFlakeIdGenerator.getInstance().nextId("SIMULATED_SHIP"))
                    .shipmentTypeId(ShipmentType.OUTBOUND.name())
                    .toCustomer(orderHeader.getToCustomer())
                    .order(orderHeader)
                    .shipmentName("Simulated Shipment for " + orderHeader.getOrderName())
                    .expectedDeliveryDate(orderHeader.getDeliveryBeforeDate())
                    .createdByUser(userLogin)
                    .statusId(ShipmentStatus.EXPORTED.name())
                    .build();

            shipment.setCreatedStamp(orderHeader.getCreatedStamp());
            // Get order items for creating inventory item details
            var orderItems = orderHeader.getOrderItems();
            var inventoryItemDetailDOs = new ArrayList<InventoryItemDetail>();

            // Process each order item
            for (OrderItem orderItem : orderItems) {

//                // find inventory items from the list containing productId
//                List<InventoryItem> filteredInventoryItems = facilities.get(orderItem.getProduct().getId());

                // random inventory item from the filtered list
                Facility facility = CommonUtil.getRandomElement(facilities);

                // Create inventory item detail for the shipment
                InventoryItemDetail inventoryItemDetail = InventoryItemDetail.builder()
                        .id(SnowFlakeIdGenerator.getInstance().nextId("SIMULATED_INVD"))
                        .facility(facility)
                        .quantity(orderItem.getQuantity())
                        .product(orderItem.getProduct())
                        .shipment(shipment)
                        .note("Simulated shipment detail")
                        .orderItem(orderItem)
                        .unit(orderItem.getUnit())
                        .price(orderItem.getPrice())
                        .build();

                inventoryItemDetail.setCreatedStamp(orderHeader.getCreatedStamp());
                inventoryItemDetail.setLastUpdatedStamp(orderHeader.getLastUpdatedStamp());

                inventoryItemDetailDOs.add(inventoryItemDetail);
            }

            if (!inventoryItemDetailDOs.isEmpty()) {
                shipmentRepo.save(shipment);
                inventoryItemDetailRepo.saveAll(inventoryItemDetailDOs);
                deliveryBillService.simulateDeliveryBill(shipment, userLogin);
            }
        } catch (Exception e) {
            log.error("Error in simulateOuboundShipment: {}", e.getMessage());
        }
    }

    @Override
    public ApiResponse<Pagination<ShipmentForDeliveryRes>> getShipmentForDelivery(int page, int limit, String facilityId) {
        var pageRequest = CommonUtil.getPageRequest(page, limit);
        var shipments = shipmentRepo.findByShipmentTypeIdAndStatusId(
                ShipmentType.OUTBOUND.name()
                ,ShipmentStatus.EXPORTED.name(), pageRequest, Sort.by(Sort.Direction.DESC, "createdStamp"));

        // init shipmentIds
        List<String> shipmentIds = shipments.getContent().stream()
                .map(Shipment::getId)
                .toList();

        // get inventory item details by shipmentIds and facilityId
        List<InventoryItemDetail> inventoryItemDetails = inventoryItemDetailRepo.findByShipmentIdInAndFacilityId(shipmentIds, facilityId);

        // group inventory item details by shipmentId
        Map<String, List<InventoryItemDetail>> inventoryItemDetailMap = inventoryItemDetails.stream()
                .collect(Collectors.groupingBy(inventoryItemDetail ->
                        inventoryItemDetail.getShipment().getId()));

        var filteredShipments = shipments.getContent().stream()
                .filter(shipment -> inventoryItemDetailMap.containsKey(shipment.getId()))
                .toList();

        var shipmentForDeliveryResList = new ArrayList<ShipmentForDeliveryRes>();
        for(var filteredShipment : filteredShipments) {
            var totalWeight = BigDecimal.ZERO;
            var totalQuantity = 0;
            var shipmentForDeliveryRes = generalMapper.convertToDto(filteredShipment, ShipmentForDeliveryRes.class);
            shipmentForDeliveryRes.setToCustomerName(filteredShipment.getToCustomer().getName());
            List<InventoryItemDetail> details = inventoryItemDetailMap.get(filteredShipment.getId());
            List<ShipmentProductForDeliveryRes> shipmentProductForDeliveryResList = new ArrayList<>();
            for(InventoryItemDetail detail : details) {
                var shipmentProductForDeliveryRes = generalMapper.convertToDto(detail, ShipmentProductForDeliveryRes.class);
                shipmentProductForDeliveryRes.setProductId(detail.getProduct().getId());
                shipmentProductForDeliveryRes.setProductName(detail.getProduct().getName());
                shipmentProductForDeliveryRes.setFacilityId(detail.getFacility().getId());
                shipmentProductForDeliveryRes.setFacilityName(detail.getFacility().getName());
                shipmentProductForDeliveryRes.setWeight(detail.getProduct().getWeight());
                // set total weight and quantity
                totalWeight = totalWeight.add(detail.getProduct().getWeight()
                        .multiply(BigDecimal.valueOf(detail.getQuantity())));
                totalQuantity += detail.getQuantity();
                shipmentProductForDeliveryResList.add(shipmentProductForDeliveryRes);
            }
            shipmentForDeliveryRes.setShipmentItems(shipmentProductForDeliveryResList);
            shipmentForDeliveryRes.setTotalWeight(totalWeight);
            shipmentForDeliveryRes.setTotalQuantity(totalQuantity);
            shipmentForDeliveryResList.add(shipmentForDeliveryRes);
        }

        return ApiResponse.<Pagination<ShipmentForDeliveryRes>>builder()
                .code(200)
                .message("Get shipment for delivery success")
                .data(Pagination.<ShipmentForDeliveryRes>builder()
                        .page(page)
                        .size(limit)
                        .data(shipmentForDeliveryResList)
                        .totalElements(shipments.getTotalElements())
                        .totalPages(shipments.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public ApiResponse<Pagination<ShipmentGetListRes>> getAll(int page, int limit, ShipmentGetListFilter filters) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var shipmentSpec = new ShipmentSpecification(filters);
        var shipmentPage = shipmentRepo.findAll(shipmentSpec, pageReq);

        List<ShipmentGetListRes> shipmentGetListRes = shipmentPage.getContent().stream()
                .map(shipment -> {
                    var res = generalMapper.convertToDto(shipment, ShipmentGetListRes.class);
                    res.setCreatedByUserName(shipment.getCreatedByUser().getFullName());
                    if(Objects.nonNull(shipment.getHandledByUser())) {
                        res.setHandledByUserName(shipment.getHandledByUser().getFullName());
                    }
                    res.setOrderId(shipment.getOrder().getId());

                    if(Objects.equals(filters.getShipmentTypeId(), ShipmentType.OUTBOUND.name()))  {
                        res.setPartnerType(PartnerType.CUSTOMER.name());
                        res.setPartnerId(shipment.getToCustomer().getId());
                        res.setPartnerName(shipment.getToCustomer().getName());
                    }
                    else {
                        res.setPartnerType(PartnerType.SUPPLIER.name());
                        res.setPartnerId(shipment.getFromSupplier().getId());
                        res.setPartnerName(shipment.getFromSupplier().getName());
                    }
                    return res;
                }).toList();

        var pagination = Pagination.<ShipmentGetListRes>builder()
                .page(page)
                .size(limit)
                .totalElements(shipmentPage.getTotalElements())
                .totalPages(shipmentPage.getTotalPages())
                .data(shipmentGetListRes)
                .build();

        return ApiResponse.<Pagination<ShipmentGetListRes>>builder()
                .code(200)
                .message("Get all shipments success")
                .data(pagination)
                .build();
    }

    // The function is used to create a new shipment from a sales order ensuring FEFO and the minimum distance between the facility
    // chose and the deliveryAdress of order
    @Override
    public ApiResponse<Void> autoAssignShipment(String orderId, Principal principal) {
        OrderHeader orderHeader = orderHeaderRepo.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));

        var userLogin = userLoginRepo.findById(principal.getName())
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + principal.getName()));

        String deliveryAddressId = orderHeader.getDeliveryAddressId();
        if (isBlank(deliveryAddressId)) {
            deliveryAddressId = orderHeader.getToCustomer().getCurrentAddressId();
        }

        String finalDeliveryAddressId = deliveryAddressId;
        Address deliveryAddress = addressRepo.findById(deliveryAddressId)
                .orElseThrow(() -> new DataNotFoundException("Delivery address not found with id: " + finalDeliveryAddressId));

        List<OrderItem> orderItems = orderHeader.getOrderItems();
        if (orderItems.isEmpty()) {
            throw new DataNotFoundException("Order has no items to ship");
        }

        List<String> productIds = orderItems.stream()
                .map(orderItem -> orderItem.getProduct().getId())
                .distinct()
                .toList();

        List<InventoryItem> inventoryItems = inventoryItemRepo.findByProductIdIn(productIds);
        if(inventoryItems.isEmpty()) {
            throw new DataNotFoundException("No inventory items found for products in the order");
        }

        List<String> facilityIds = inventoryItems.stream()
                .map(inventoryItem -> inventoryItem.getFacility().getId())
                .distinct()
                .toList();

        Map<String, Address> facilityAddressMap = addressRepo
                .findAllByEntityIdInAndEntityType(facilityIds, EntityType.FACILITY.name())
                .stream()
                .collect(Collectors.toMap(Address::getEntityId, address -> address));

        Shipment newShipment = Shipment.builder()
                .id(SnowFlakeIdGenerator.getInstance().nextId(SHIPMENT_ID_PREFIX))
                .shipmentName("Phiếu xuất kho tự động từ đơn hàng " + orderHeader.getId())
                .shipmentTypeId(ShipmentType.OUTBOUND.name())
                .statusId(ShipmentStatus.CREATED.name())
                .toCustomer(orderHeader.getToCustomer())
                .order(orderHeader)
                .createdByUser(userLogin)
                .expectedDeliveryDate(orderHeader.getDeliveryAfterDate())
                .build();

        List<InventoryItemDetail> inventoryItemDetails = new ArrayList<>();
        BigDecimal totalWeight = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (OrderItem orderItem : orderItems) {
            String productId = orderItem.getProduct().getId();
            int quantityToShip = orderItem.getQuantity();

            List<InventoryItem> suitableItems = inventoryItems.stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .collect(Collectors.toList());

            suitableItems.sort((i1, i2) -> {
                int compare = Comparator
                        .comparing(InventoryItem::getExpirationDate, Comparator.nullsLast(LocalDate::compareTo))
                        .thenComparing(InventoryItem::getReceivedDate, Comparator.nullsLast(LocalDate::compareTo))
                        .compare(i1, i2);

                if (compare != 0) return compare;

                Address addr1 = facilityAddressMap.get(i1.getFacility().getId());
                Address addr2 = facilityAddressMap.get(i2.getFacility().getId());
                double dist1 = TimeDistance.calculateHaversineDistance(
                        deliveryAddress.getLatitude(), deliveryAddress.getLongitude(),
                        addr1.getLatitude(), addr1.getLongitude());
                double dist2 = TimeDistance.calculateHaversineDistance(
                        deliveryAddress.getLatitude(), deliveryAddress.getLongitude(),
                        addr2.getLatitude(), addr2.getLongitude());
                return Double.compare(dist1, dist2);
            });

            for (InventoryItem inventoryItem : suitableItems) {
                if (quantityToShip <= 0) break;
                int available = inventoryItem.getQuantity();
                if (available <= 0) continue;

                int assignQty = Math.min(available, quantityToShip);

                InventoryItemDetail detail = InventoryItemDetail.builder()
                        .id(SnowFlakeIdGenerator.getInstance().nextId(INVENTORY_ITEM_DETAIL_ID_PREFIX))
                        .product(inventoryItem.getProduct())
                        .facility(inventoryItem.getFacility())
                        .shipment(newShipment)
                        .quantity(assignQty)
                        .unit(orderItem.getUnit())
                        .price(orderItem.getPrice())
                        .orderItem(orderItem)
                        .build();

                inventoryItemDetails.add(detail);
                quantityToShip -= assignQty;

                totalQuantity += assignQty;
                if (inventoryItem.getProduct().getWeight() != null) {
                    totalWeight = totalWeight.add(inventoryItem.getProduct().getWeight().multiply(BigDecimal.valueOf(assignQty)));
                }
            }

            if (quantityToShip > 0) {
                log.warn("Not enough inventory for product {}. Required: {}, Available: {}",
                        productId, orderItem.getQuantity(), orderItem.getQuantity() - quantityToShip);
            }
        }

        newShipment.setTotalQuantity(totalQuantity);
        newShipment.setTotalWeight(totalWeight);

        shipmentRepo.save(newShipment);
        inventoryItemDetailRepo.saveAll(inventoryItemDetails);

        return ApiResponse.<Void>builder()
                .code(201)
                .message("Shipment created successfully")
                .build();
    }

}

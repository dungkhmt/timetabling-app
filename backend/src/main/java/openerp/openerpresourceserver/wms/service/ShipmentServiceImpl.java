package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.algorithm.SnowFlakeIdGenerator;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.micrometer.common.util.StringUtils.isBlank;
import static openerp.openerpresourceserver.wms.constant.Constants.INVENTORY_ITEM_DETAIL_ID_PREFIX;
import static openerp.openerpresourceserver.wms.constant.Constants.SHIPMENT_ID_PREFIX;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {
    private final UserLoginRepo userLoginRepo;
    private final ShipmentRepo shipmentRepo;
    private final InventoryItemDetailRepo inventoryItemDetailRepo;
    private final InventoryItemRepo inventoryItemRepo;
    private final ProductRepo productRepo;
    private final OrderHeaderRepo orderHeaderRepo;
    private final GeneralMapper generalMapper;
    private final FacilityRepo facilityRepo;

    @Override
    public ApiResponse<Void> createOutboundSaleOrder(CreateOutBoundReq req, String name) {
        // Tìm OrderHeader theo orderId
        OrderHeader orderHeader = orderHeaderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + req.getOrderId()));
        var orderItems = orderHeader.getOrderItems();
        var productReqs = req.getProducts();
        var shipment = Shipment.builder()
                .id(CommonUtil.getUUID())
                .shipmentTypeId(ShipmentType.OUTBOUND.name())
                .toCustomer(orderHeader.getToCustomer())
                .order(orderHeader)
                .shipmentName(req.getShipmentName())
                .note(req.getNote())
                .expectedDeliveryDate(req.getExpectedDeliveryDate())
                .createdByUser(userLoginRepo.findById(name).orElseThrow(
                        () -> new DataNotFoundException("User not found with id: " + name)
                ))
                .build();
        var inventoryItemDetailDOs = new ArrayList<InventoryItemDetail>();
        for (var product : productReqs) {
            // Tìm Product theo productId
            Product productEntity = productRepo.findById(product.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + product.getProductId()));

            // Tìm InventoryItem theo productId và facilityId
            InventoryItem inventoryItem = inventoryItemRepo.findById(product.getInventoryItemId())
                    .orElseThrow(() -> new DataNotFoundException("Inventory item not found with productId: " + product.getProductId()));

            // Kiểm tra số lượng tồn kho
            if (inventoryItem.getQuantity() < product.getQuantity()) {
                throw new DataNotFoundException("Not enough quantity in inventory");
            }

            // Kiếm tra số lượng sản phẩm trong OrderItem
            var orderItem = orderItems.stream()
                    .filter(item -> item.getProduct().getId().equals(product.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new DataNotFoundException("Order item not found with productId: " + product.getProductId()));

            // Kiểm tra số lượng sản phẩm trong OrderItem
            if (orderItem.getQuantity() < product.getQuantity()) {
                throw new DataNotFoundException("Not enough quantity in order item");
            }


            var inventoryItemDetail = InventoryItemDetail.builder()
//                    .inventoryItem(inventoryItem)
                    .quantity(product.getQuantity())
                    .product(productEntity)
                    .shipment(shipment)
                    .orderItem(orderItem)
                    .id(CommonUtil.getUUID())
                    .build();

            inventoryItemDetailDOs.add(inventoryItemDetail);


        }
        shipmentRepo.save(shipment);
        inventoryItemDetailRepo.saveAll(inventoryItemDetailDOs);

        return ApiResponse.<Void>builder()
                .code(201)
                .message("Create outbound sale order success")
                .build();
    }

    @Override
    public ApiResponse<Pagination<OutBoundByOrderRes>> getOutBoundByOrder(String orderId, int page, int limit) {
        var pageRequest = CommonUtil.getPageRequest(page, limit);
        var shipments = shipmentRepo.findByOrderId(orderId, pageRequest);
        var orderHeader = orderHeaderRepo.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));

        var outBoundByOrderPageRes = shipments.getContent().stream()
                .map(shipment -> OutBoundByOrderRes.builder()
                        .id(shipment.getId())
                        .shipmentType(shipment.getShipmentTypeId())
                        .shipmentName(shipment.getShipmentName())
                        .customerName(shipment.getToCustomer().getName())
                        .statusId(shipment.getStatusId())
                        .build()
                ).toList();

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
        var products = inventoryItemDetails.stream()
                .map(inventoryItemDetail -> OutBoundDetailProductRes.builder()
                        .id(inventoryItemDetail.getId())
                        .productId(inventoryItemDetail.getProduct().getId())
                        .productName(inventoryItemDetail.getProduct().getName())
                        .quantity(inventoryItemDetail.getQuantity())
                        .requestedQuantity(inventoryItemDetail.getOrderItem().getQuantity())
                        .wholeSalePrice(inventoryItemDetail.getProduct().getWholeSalePrice())
                        .unit(inventoryItemDetail.getProduct().getUnit())
                        .build()
                ).toList();

        return ApiResponse.<OutBoundDetailRes>builder()
                .code(200)
                .message("Get outbound detail success")
                .data(OutBoundDetailRes.builder()
                        .id(shipment.getId())
                        .shipmentType(shipment.getShipmentTypeId())
                        .shipmentName(shipment.getShipmentName())
                        .customerName(shipment.getToCustomer().getName())
                        .statusId(shipment.getStatusId())
                        .createdStamp(shipment.getCreatedStamp())
                        .expectedDeliveryDate(shipment.getExpectedDeliveryDate())
                        .products(products)
                        .build())
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

            totalWeight = totalWeight.add((BigDecimal.valueOf(inventoryItemDetail.getProduct().getWeight()))
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
    public void simulateOuboundShipment(OrderHeader orderHeader, UserLogin userLogin, Map<String, List<InventoryItem>> inventoryItemMap) {
        try {
            // Prepare shipment entity
            Shipment shipment = Shipment.builder()
                    .id(CommonUtil.getUUID())
                    .shipmentTypeId(ShipmentType.OUTBOUND.name())
                    .toCustomer(orderHeader.getToCustomer())
                    .order(orderHeader)
                    .shipmentName("Simulated Shipment for " + orderHeader.getOrderName())
                    .expectedDeliveryDate(orderHeader.getDeliveryBeforeDate())
                    .createdByUser(userLogin)
                    .build();

            shipment.setCreatedStamp(orderHeader.getCreatedStamp());
            // Get order items for creating inventory item details
            var orderItems = orderHeader.getOrderItems();
            var inventoryItemDetailDOs = new ArrayList<InventoryItemDetail>();

            // Process each order item
            for (OrderItem orderItem : orderItems) {

                // find inventory items from the list containing productId
                List<InventoryItem> filteredInventoryItems = inventoryItemMap.get(orderItem.getProduct().getId());

                // random inventory item from the filtered list
                InventoryItem inventoryItem = CommonUtil.getRandomElement(filteredInventoryItems);

                // Create inventory item detail for the shipment
                InventoryItemDetail inventoryItemDetail = InventoryItemDetail.builder()
                        .id(CommonUtil.getUUID())
//                        .inventoryItem(inventoryItem)
                        .quantity(orderItem.getQuantity())
                        .product(orderItem.getProduct())
                        .shipment(shipment)
                        .note("Simulated shipment detail")
                        .orderItem(orderItem)
                        .build();

                inventoryItemDetail.setCreatedStamp(orderHeader.getCreatedStamp());
                inventoryItemDetail.setLastUpdatedStamp(orderHeader.getLastUpdatedStamp());

                inventoryItemDetailDOs.add(inventoryItemDetail);
            }

            // Only save shipment if there are items to ship
            if (!inventoryItemDetailDOs.isEmpty()) {
                shipmentRepo.save(shipment);
                inventoryItemDetailRepo.saveAll(inventoryItemDetailDOs);
            }
        } catch (Exception e) {
            // Log error but continue processing
            e.printStackTrace();
        }
    }

    @Override
    public ApiResponse<Pagination<ShipmentForDeliveryRes>> getShipmentForDelivery(int page, int limit) {
        var pageRequest = CommonUtil.getPageRequest(page, limit);
        var shipments = shipmentRepo.findByStatusId(ShipmentStatus.EXPORTED.name(), pageRequest);

        List<ShipmentForDeliveryRes> shipmentForDeliveryPageRes = shipments.getContent().stream()
                .map(shipment -> {
                            var res = generalMapper.convertToDto(shipment, ShipmentForDeliveryRes.class);
                            res.setCustomerName(shipment.getToCustomer().getName());
                            return res;
                        }
                ).toList();

        return ApiResponse.<Pagination<ShipmentForDeliveryRes>>builder()
                .code(200)
                .message("Get shipment for delivery success")
                .data(Pagination.<ShipmentForDeliveryRes>builder()
                        .page(page)
                        .size(limit)
                        .data(shipmentForDeliveryPageRes)
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


}

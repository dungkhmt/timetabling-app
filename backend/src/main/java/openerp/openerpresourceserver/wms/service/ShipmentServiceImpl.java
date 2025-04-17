package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentType;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.shipment.*;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {
    private final UserLoginRepo userLoginRepo;
    private final ShipmentRepo shipmentRepo;
    private final InventoryItemDetailRepo inventoryItemDetailRepo;
    private final InventoryItemRepo inventoryItemRepo;
    private final ProductRepo productRepo;
    private final OrderHeaderRepo orderHeaderRepo;

    @Override
    public ApiResponse<Void> createOutboundSaleOrder(CreateOutBoundReq req, String name) {
        // Tìm OrderHeader theo orderId
        OrderHeader orderHeader = orderHeaderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + req.getOrderId()));
        var orderItems = orderHeader.getOrderItems();
        var productReqs = req.getProducts();
        var shipment = Shipment.builder()
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
                    .inventoryItem(inventoryItem)
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
                            .statusId(shipment.getShipmentStatusId())
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
                        .statusId(shipment.getShipmentStatusId())
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
        var shipment = Shipment.builder()
                .shipmentTypeId(ShipmentType.INBOUND.name())
                .fromSupplier(orderHeader.getFromSupplier())
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
                    .inventoryItem(inventoryItem)
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
    public ApiResponse<Pagination<InboundByOrderRes>> getInBoundByOrder(String orderId, int page, int limit) {
        var pageRequest = CommonUtil.getPageRequest(page, limit);
        var shipments = shipmentRepo.findByOrderId(orderId, pageRequest);
        var orderHeader = orderHeaderRepo.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));

        List<InboundByOrderRes> inBoundByOrderPageRes = shipments.getContent().stream()
                .map(shipment -> InboundByOrderRes.builder()
                        .id(shipment.getId())
                        .shipmentType(shipment.getShipmentTypeId())
                        .shipmentName(shipment.getShipmentName())
                        .expectedDeliveryDate(shipment.getExpectedDeliveryDate())
                        .supplierName(shipment.getFromSupplier().getName())
                        .statusId(shipment.getShipmentStatusId())
                        .build()
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
                .map(inventoryItemDetail -> InboundDetailProductRes.builder()
                        .id(inventoryItemDetail.getId())
                        .productId(inventoryItemDetail.getProduct().getId())
                        .productName(inventoryItemDetail.getProduct().getName())
                        .quantity(inventoryItemDetail.getQuantity())
                        .requestedQuantity(inventoryItemDetail.getOrderItem().getQuantity())
                        .wholeSalePrice(inventoryItemDetail.getProduct().getWholeSalePrice())
                        .unit(inventoryItemDetail.getProduct().getUnit())
                        .build()
                ).toList();

        return ApiResponse.<InboundDetailRes>builder()
                .code(200)
                .message("Get outbound detail success")
                .data(InboundDetailRes.builder()
                        .id(shipment.getId())
                        .shipmentType(shipment.getShipmentTypeId())
                        .shipmentName(shipment.getShipmentName())
                        .supplierName(shipment.getFromSupplier().getName())
                        .statusId(shipment.getShipmentStatusId())
                        .createdStamp(shipment.getCreatedStamp())
                        .expectedDeliveryDate(shipment.getExpectedDeliveryDate())
                        .products(products)
                        .build())
                .build();
    }

    @Override
    public void simulateOuboundShipment(OrderHeader orderHeader, UserLogin userLogin) {
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

            // Get order items for creating inventory item details
            var orderItems = orderHeader.getOrderItems();
            var inventoryItemDetailDOs = new ArrayList<InventoryItemDetail>();

            // Process each order item
            for (OrderItem orderItem : orderItems) {
                // Find inventory item for this product in this facility
                InventoryItem inventoryItem = inventoryItemRepo
                        .findByProductIdAndFacilityId(orderItem.getProduct().getId(), orderHeader.getFacility().getId());


                // Skip if no inventory item found
                if (inventoryItem == null ) {
                    continue;
                }

                // Use half of ordered quantity for shipment (simulating partial fulfillment)
                int shipQuantity = Math.max(1, orderItem.getQuantity() / 2);

                // Ensure we don't exceed available inventory
                if (inventoryItem.getQuantity() < shipQuantity) {
                    shipQuantity = inventoryItem.getQuantity();
                }

                // Skip if no quantity to ship
                if (shipQuantity <= 0) {
                    continue;
                }

                // Create inventory item detail for the shipment
                InventoryItemDetail inventoryItemDetail = InventoryItemDetail.builder()
                        .id(CommonUtil.getUUID())
                        .inventoryItem(inventoryItem)
                        .quantity(shipQuantity)
                        .product(orderItem.getProduct())
                        .shipment(shipment)
                        .orderItem(orderItem)
                        .build();

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


}

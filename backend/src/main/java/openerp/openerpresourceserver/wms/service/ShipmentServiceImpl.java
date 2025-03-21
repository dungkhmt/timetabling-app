package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentType;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.shipment.CreateOutBounndReq;
import openerp.openerpresourceserver.wms.dto.shipment.OutBoundByOrderRes;
import openerp.openerpresourceserver.wms.dto.shipment.OutBoundDetailProductRes;
import openerp.openerpresourceserver.wms.dto.shipment.OutBoundDetailRes;
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
    public ApiResponse<Void> createOutboundSaleOrder(CreateOutBounndReq req, String name) {
        // Tìm OrderHeader theo orderId
        OrderHeader orderHeader = orderHeaderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + req.getOrderId()));
        var orderItems = orderHeader.getOrderItems();
        var productReqs = req.getProducts();
        var shipment = Shipment.builder()
                .shipmentTypeId(ShipmentType.OUTBOUND.name())
                .toCustomer(orderHeader.getToCustomer())
                .order(orderHeader)
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
                        .products(products)
                        .build())
                .build();
    }


}

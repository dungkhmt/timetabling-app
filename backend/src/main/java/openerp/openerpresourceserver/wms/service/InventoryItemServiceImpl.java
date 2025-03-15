package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentType;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.saleOrder.CreateOutBounndReq;
import openerp.openerpresourceserver.wms.dto.saleOrder.InventoryItemForOrderRes;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryItemServiceImpl implements InventoryItemService{
    private final InventoryItemRepo inventoryItemRepo;
    private final ProductRepo productRepo;
    private final OrderHeaderRepo orderHeaderRepo;
    private final UserLoginRepo userLoginRepo;
    private final ShipmentRepo shipmentRepo;
    private final InventoryItemDetailRepo inventoryItemDetailRepo;
    @Override
    public ApiResponse<Pagination<InventoryItemForOrderRes>> getInventoryItems(int page, int limit, String orderId) {
        PageRequest pageRequest = PageRequest.of(page, limit);

        // Tìm OrderHeader theo orderId
        OrderHeader orderHeader = orderHeaderRepo.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));

        // Lấy danh sách productId từ OrderItems
        List<String> productIds = orderHeader.getOrderItems().stream()
                .map(orderItem -> orderItem.getProduct().getId())
                .collect(Collectors.toList());

        // Truy vấn InventoryItem theo productIds
        Page<InventoryItem> inventoryItems = inventoryItemRepo.findByProductIdIn(productIds, pageRequest);

        // Chuyển đổi dữ liệu sang response
        List<InventoryItemForOrderRes> inventoryItemResponses = inventoryItems.stream()
                .map(inventoryItem -> InventoryItemForOrderRes.builder()
                        .id(inventoryItem.getId())
                        .quantity(inventoryItem.getQuantity())
                        .facilityName(inventoryItem.getFacility().getName())
                        .productId(inventoryItem.getProduct().getId())
                        .build()
                ).toList();


        // Đóng gói kết quả phân trang
        Pagination<InventoryItemForOrderRes> pagination = new Pagination<>(
                inventoryItemResponses,
                inventoryItems.getNumber(),
                inventoryItems.getSize(),
                inventoryItems.getTotalElements(),
                inventoryItems.getTotalPages()
        );

        return ApiResponse.<Pagination<InventoryItemForOrderRes>>builder()
                .code(200)
                .message("Get inventory items success")
                .data(pagination)
                .build();
    }

    @Override
    public ApiResponse<Void> createOutboundSaleOrder(CreateOutBounndReq req, String name) {
        // Tìm OrderHeader theo orderId
        OrderHeader orderHeader = orderHeaderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + req.getOrderId()));
        var orderItems = orderHeader.getOrderItems();
        var productReqs = req.getProducts();
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

            var shipment = Shipment.builder()
                    .shipmentTypeId(ShipmentType.OUTBOUND.name())
                    .toCustomer(orderHeader.getToCustomer())
                    .order(orderHeader)
                    .createdByUser(userLoginRepo.findById(name).orElseThrow(
                            () -> new DataNotFoundException("User not found with id: " + name)
                    ))
                    .build();

            var inventoryItemDetail = InventoryItemDetail.builder()
                                    .inventoryItem(inventoryItem)
                                    .quantity(product.getQuantity())
                                    .product(productEntity)
                                    .shipment(shipment)
                                    .id(CommonUtil.getUUID())
                                    .build();

            if(req.getId() == null) {
                shipment.setId(CommonUtil.getUUID());
            }
            shipment.setId(req.getId());

            shipmentRepo.save(shipment);
            inventoryItemDetailRepo.save(inventoryItemDetail);

        }

        
        return ApiResponse.<Void>builder()
                .code(201)
                .message("Create outbound sale order success")
                .build();
    }

}

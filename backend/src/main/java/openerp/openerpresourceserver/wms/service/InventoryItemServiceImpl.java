package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.saleOrder.InventoryItemForOrderRes;
import openerp.openerpresourceserver.wms.entity.InventoryItem;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import openerp.openerpresourceserver.wms.entity.Product;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.repository.InventoryItemRepo;
import openerp.openerpresourceserver.wms.repository.OrderHeaderRepo;
import openerp.openerpresourceserver.wms.repository.OrderItemRepo;
import openerp.openerpresourceserver.wms.repository.ProductRepo;
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
    private final OrderItemRepo  orderItemRepo;
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
                        .build()
                ).collect(Collectors.toList());


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

}

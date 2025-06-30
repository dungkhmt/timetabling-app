package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.inventoryItem.FacilityForOrderRes;
import openerp.openerpresourceserver.wms.dto.inventoryItem.InventoryItemForOrderRes;
import openerp.openerpresourceserver.wms.dto.inventoryItem.InventoryProductRes;
import openerp.openerpresourceserver.wms.entity.Facility;
import openerp.openerpresourceserver.wms.entity.InventoryItem;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.FacilityRepo;
import openerp.openerpresourceserver.wms.repository.InventoryItemRepo;
import openerp.openerpresourceserver.wms.repository.OrderHeaderRepo;
import openerp.openerpresourceserver.wms.repository.ProductRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryItemServiceImpl implements InventoryItemService{
    private final InventoryItemRepo inventoryItemRepo;
    private final ProductRepo productRepo;
    private final OrderHeaderRepo orderHeaderRepo;
    private final FacilityRepo facilityRepo;
    private final GeneralMapper generalMapper;

    @Override
    public ApiResponse<Pagination<InventoryItemForOrderRes>> getInventoryItemsForOutBound(int page, int limit, String orderId) {
        PageRequest pageRequest = PageRequest.of(page, limit);

        OrderHeader orderHeader = orderHeaderRepo.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));

        List<String> productIds = orderHeader.getOrderItems().stream()
                .map(orderItem -> orderItem.getProduct().getId())
                .collect(Collectors.toList());

        Page<InventoryItem> inventoryItems = inventoryItemRepo.findByProductIdIn(productIds, pageRequest);

        Map<String, List<InventoryItem>> productInventoryItemMap = inventoryItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getId()));

        List<InventoryItemForOrderRes> inventoryItemForOrderResList = new ArrayList<>();

        for (Map.Entry<String, List<InventoryItem>> entry : productInventoryItemMap.entrySet()) {
            String productId = entry.getKey();
            List<InventoryItem> items = entry.getValue();

            Map<String, List<InventoryItem>> facilityGrouped = items.stream()
                    .collect(Collectors.groupingBy(item -> item.getFacility().getId()));

            List<FacilityForOrderRes> facilityForOrderResList = new ArrayList<>();

            for (Map.Entry<String, List<InventoryItem>> facilityEntry : facilityGrouped.entrySet()) {
                String facilityId = facilityEntry.getKey();
                List<InventoryItem> facilityItems = facilityEntry.getValue();

                int quantitySum = facilityItems.stream()
                        .mapToInt(InventoryItem::getQuantity)
                        .sum();

                String facilityName = facilityItems.get(0).getFacility().getName();

                FacilityForOrderRes facilityRes = FacilityForOrderRes.builder()
                        .facilityId(facilityId)
                        .facilityName(facilityName)
                        .quantity(quantitySum)
                        .build();

                facilityForOrderResList.add(facilityRes);
            }

            InventoryItemForOrderRes itemRes = InventoryItemForOrderRes.builder()
                    .productId(productId)
                    .facilityForOrderRes(facilityForOrderResList)
                    .build();

            inventoryItemForOrderResList.add(itemRes);
        }

        Pagination<InventoryItemForOrderRes> pagination = Pagination.<InventoryItemForOrderRes>builder()
                .page(page)
                .size(limit)
                .totalPages(inventoryItems.getTotalPages())
                .totalElements(inventoryItems.getTotalElements())
                .data(inventoryItemForOrderResList)
                .build();

        return ApiResponse.<Pagination<InventoryItemForOrderRes>>builder()
                .code(200)
                .message("Get inventory items success")
                .data(pagination)
                .build();
    }


    @Override
    public ApiResponse<Pagination<InventoryItemForOrderRes>> getInventoryItemsForInBound(int page, int limit, String orderId) {
        OrderHeader orderHeader = orderHeaderRepo.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found with id: " + orderId));

        List<String> productIds = orderHeader.getOrderItems().stream()
                .map(orderItem -> orderItem.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        List<Facility> allFacilities = facilityRepo.findAll();

        List<InventoryItem> inventoryItems = inventoryItemRepo.findByProductIdIn(productIds);

        Map<String, Map<String, Integer>> inventoryMap = inventoryItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getId(),
                        Collectors.groupingBy(
                                item -> item.getFacility().getId(),
                                Collectors.summingInt(InventoryItem::getQuantity)
                        )
                ));

        int totalElements = productIds.size();
        int totalPages = (int) Math.ceil((double) totalElements / limit);
        int fromIndex = Math.min(page * limit, totalElements);
        int toIndex = Math.min(fromIndex + limit, totalElements);
        List<String> paginatedProductIds = productIds.subList(fromIndex, toIndex);

        List<InventoryItemForOrderRes> inventoryItemForOrderResList = new ArrayList<>();

        for (String productId : paginatedProductIds) {
            List<FacilityForOrderRes> facilityForOrderResList = new ArrayList<>();

            for (Facility facility : allFacilities) {
                String facilityId = facility.getId();
                String facilityName = facility.getName();

                int quantity = inventoryMap
                        .getOrDefault(productId, Map.of())
                        .getOrDefault(facilityId, 0);

                FacilityForOrderRes facilityRes = FacilityForOrderRes.builder()
                        .facilityId(facilityId)
                        .facilityName(facilityName)
                        .quantity(quantity)
                        .build();

                facilityForOrderResList.add(facilityRes);
            }

            InventoryItemForOrderRes itemRes = InventoryItemForOrderRes.builder()
                    .productId(productId)
                    .facilityForOrderRes(facilityForOrderResList)
                    .build();

            inventoryItemForOrderResList.add(itemRes);
        }

        Pagination<InventoryItemForOrderRes> pagination = Pagination.<InventoryItemForOrderRes>builder()
                .page(page)
                .size(limit)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .data(inventoryItemForOrderResList)
                .build();

        return ApiResponse.<Pagination<InventoryItemForOrderRes>>builder()
                .code(200)
                .message("Get inventory items for inbound success")
                .data(pagination)
                .build();
    }

    @Override
    public ApiResponse<Pagination<InventoryProductRes>> getInventoryItemByProductId(int page, int limit, String productId) {
        var pageRequest = PageRequest.of(page, limit);
        Page<InventoryItem> inventoryItems = inventoryItemRepo.findByProductId(productId, pageRequest);
        if(inventoryItems.getContent().isEmpty()) {
            throw new DataNotFoundException("No inventory items found for product with id: " + productId);
        }

        List<InventoryProductRes> inventoryProductRes = inventoryItems.getContent().stream().map(
                item -> {
                    var res = generalMapper.convertToDto(item, InventoryProductRes.class);
                    res.setProductId(item.getProduct().getId());
                    res.setProductName(item.getProduct().getName());
                    res.setFacilityId(item.getFacility().getId());
                    res.setFacilityName(item.getFacility().getName());
                    return res;
                }
        ).toList();

        Pagination<InventoryProductRes> pagination = Pagination.<InventoryProductRes>builder()
                .page(page)
                .size(limit)
                .totalPages(inventoryItems.getTotalPages())
                .totalElements(inventoryItems.getTotalElements())
                .data(inventoryProductRes)
                .build();

        return ApiResponse.<Pagination<InventoryProductRes>>builder()
                .code(200)
                .message("Get inventory items by product id success")
                .data(pagination)
                .build();
    }


}

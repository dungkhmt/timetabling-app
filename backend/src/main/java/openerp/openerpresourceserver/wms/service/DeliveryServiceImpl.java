package openerp.openerpresourceserver.wms.service;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.DeliveryBillStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryBill;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryBillProduct;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryListPageRes;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryBillGetListFilter;
import openerp.openerpresourceserver.wms.entity.DeliveryBill;
import openerp.openerpresourceserver.wms.entity.DeliveryBillItem;
import openerp.openerpresourceserver.wms.entity.InventoryItemDetail;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryBillService {
    private final DeliveryBillRepo deliveryBillRepo;
    private final DeliveryBillItemRepo deliveryBillItemRepo;
    private final ShipmentRepo shipmentRepo;
    private final InventoryItemDetailRepo inventoryItemDetailRepo;
    private final GeneralMapper convert;
    private final UserLoginRepo userLoginRepo;
    @Override
    public ApiResponse<Void> createDeliveryBill(CreateDeliveryBill req, Principal principal) {
        var shipment = shipmentRepo.findById(req.getShipmentId()).orElseThrow(()
        -> new DataNotFoundException("Shipment not found with id: " + req.getShipmentId()));
        var userLogin = userLoginRepo.findById(principal.getName()).orElseThrow(
                () -> new DataNotFoundException("User not found with id: " + principal.getName()));

        // Map productId to InventoryItemDetail
        Map<String ,InventoryItemDetail> inventoryItemDetails = inventoryItemDetailRepo.findByShipmentId(req.getShipmentId())
                .stream().collect(Collectors.toMap(item -> item.getProduct().getId(), Function.identity()));

        List<CreateDeliveryBillProduct> products = req.getProducts();

        var deliveryBill = DeliveryBill.builder()
                .id(CommonUtil.getUUID())
                .shipment(shipment)
                .deliveryBillName(req.getDeliveryBillName())
                .expectedDeliveryDate(req.getExpectedDeliveryDate() != null ? req.getExpectedDeliveryDate() : shipment.getExpectedDeliveryDate())
                .priority(req.getPriority() != null ? req.getPriority() : 1)
                .note(req.getNote())
                .statusId(DeliveryBillStatus.CREATED.name())
                .toCustomer(shipment.getToCustomer())
                .build();

        AtomicDouble totalWeight = new AtomicDouble(0);
        AtomicInteger count = new AtomicInteger(1);
        List<DeliveryBillItem> deliveryBillItems = new ArrayList<>();
        products.forEach(product -> {
            InventoryItemDetail inventoryItemDetail = inventoryItemDetails.get(product.getProductId());
            if (inventoryItemDetail == null) {
                throw new DataNotFoundException("Product not found in shipment: " + product.getProductId());
            }
            // Check if the quantity is available
            if (inventoryItemDetail.getQuantity() < product.getQuantity()) {
                throw new IllegalArgumentException("Insufficient quantity for product: " + product.getProductId());
            }
            DeliveryBillItem deliveryBillItem = DeliveryBillItem.builder()
                    .id(CommonUtil.getUUID())
                    .deliveryBill(deliveryBill)
                    .deliveryBillItemSeqId(CommonUtil.getSequenceId("DBI", 5, count.getAndIncrement()))
                    .product(inventoryItemDetail.getProduct())
                    .quantity(product.getQuantity())
                    .build();

            totalWeight.addAndGet(inventoryItemDetail.getProduct().getWeight() * product.getQuantity());
            deliveryBillItems.add(deliveryBillItem);

        });

        deliveryBill.setTotalWeight(BigDecimal.valueOf(totalWeight.get()));
        deliveryBill.setCreatedByUser(userLogin);
        deliveryBillRepo.save(deliveryBill);
        deliveryBillItemRepo.saveAll(deliveryBillItems);
        return ApiResponse.<Void>builder()
                .code(201)
                .message("Delivery bill created successfully")
                .build();
    }

    @Override
    public ApiResponse<Pagination<DeliveryListPageRes>> getDeliveryBills(int page, int limit, DeliveryBillGetListFilter filters) {
        var pageReq = CommonUtil.getPageRequest(page, limit);
        var deliveryBills = deliveryBillRepo.findAll(pageReq);

        List<DeliveryListPageRes> deliveryBillList = deliveryBills.getContent().stream()
                .map(deliveryBill -> {
                    var deliveryBillGetListRes = convert.convertToDto(deliveryBill, DeliveryListPageRes.class);
                    deliveryBillGetListRes.setCustomerName(deliveryBill.getToCustomer().getName());
                    return deliveryBillGetListRes;
                }).toList();

        return ApiResponse.<Pagination<DeliveryListPageRes>>builder()
                .code(200)
                .message("Get delivery bills successfully")
                .data(Pagination.<DeliveryListPageRes>builder()
                        .page(page)
                        .size(limit)
                        .totalElements(deliveryBills.getTotalElements())
                        .totalPages(deliveryBills.getTotalPages())
                        .data(deliveryBillList)
                        .build())
                .build();
    }
}

package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.wms.algorithm.SnowFlakeIdGenerator;
import openerp.openerpresourceserver.wms.constant.enumrator.DeliveryBillStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.DeliveryBillTripStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryBill;
import openerp.openerpresourceserver.wms.dto.delivery.CreateDeliveryBillProduct;
import openerp.openerpresourceserver.wms.dto.delivery.DeliveryListPageRes;
import openerp.openerpresourceserver.wms.dto.filter.DeliveryBillGetListFilter;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.repository.specification.DeliveryBillSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static openerp.openerpresourceserver.wms.constant.Constants.DELIVERY_BILL_ID_PREFIX;
import static openerp.openerpresourceserver.wms.constant.Constants.DELIVERY_BILL_ITEM_ID_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryBillService {
    private final DeliveryBillRepo deliveryBillRepo;
    private final DeliveryBillItemRepo deliveryBillItemRepo;
    private final ShipmentRepo shipmentRepo;
    private final InventoryItemDetailRepo inventoryItemDetailRepo;
    private final GeneralMapper convert;
    private final UserLoginRepo userLoginRepo;
    private final GeneralMapper generalMapper;
    private final FacilityRepo facilityRepo;
    private final ProductRepo productRepo;

    @Override
    public ApiResponse<Void> createDeliveryBill(CreateDeliveryBill req, Principal principal) {
        var shipment = shipmentRepo.findById(req.getShipmentId()).orElseThrow(()
        -> new DataNotFoundException("Shipment not found with id: " + req.getShipmentId()));
        var userLogin = userLoginRepo.findById(principal.getName()).orElseThrow(
                () -> new DataNotFoundException("User not found with id: " + principal.getName()));
        var facility = facilityRepo.findById(req.getFacilityId()).orElseThrow(
                () -> new DataNotFoundException("Facility not found with id: " + req.getFacilityId())
        );

        List<CreateDeliveryBillProduct> products = req.getProducts();

        var deliveryBill = generalMapper.convertToEntity(req, DeliveryBill.class);
        deliveryBill.setId(SnowFlakeIdGenerator.getInstance().nextId(DELIVERY_BILL_ID_PREFIX));
        deliveryBill.setShipment(shipment);
        deliveryBill.setToCustomer(shipment.getToCustomer());
        deliveryBill.setFacility(facility);
        deliveryBill.setExpectedDeliveryDate(req.getExpectedDeliveryDate() != null ? req.getExpectedDeliveryDate() : shipment.getExpectedDeliveryDate());
        deliveryBill.setPriority(req.getPriority() != null ? req.getPriority() : 1);
        deliveryBill.setStatusId(DeliveryBillStatus.CREATED.name());
        deliveryBill.setDeliveryStatusId(DeliveryBillTripStatus.UNASSIGNED.name());

        var productIds = products.stream()
                .map(CreateDeliveryBillProduct::getProductId)
                .collect(Collectors.toList());

        Map<String, Product> productMap = productRepo.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        var totalWeight = BigDecimal.ZERO;
        var totalQuantity = 0;
        var seq = 1;
        List<DeliveryBillItem> deliveryBillItems = new ArrayList<>();
        for(var deliveryBillItemReq: req.getProducts()) {
            var deliveryBillItem = generalMapper.convertToEntity(deliveryBillItemReq, DeliveryBillItem.class);
            deliveryBillItem.setId(SnowFlakeIdGenerator.getInstance().nextId(DELIVERY_BILL_ITEM_ID_PREFIX));
            deliveryBillItem.setDeliveryBill(deliveryBill);
            deliveryBillItem.setDeliveryBillItemSeqId(seq++);
            var product = productMap.get(deliveryBillItemReq.getProductId());
            if (product == null) {
                throw new DataNotFoundException("Product not found with id: " + deliveryBillItemReq.getProductId());
            }
            deliveryBillItem.setProduct(product);
            deliveryBillItems.add(deliveryBillItem);
            totalWeight = totalWeight.add(deliveryBillItem.getWeight().multiply(BigDecimal.valueOf(deliveryBillItem.getQuantity())));
            totalQuantity += deliveryBillItem.getQuantity();
        }

        deliveryBill.setTotalWeight(totalWeight);
        deliveryBill.setTotalQuantity(totalQuantity);
        deliveryBill.setCreatedByUser(userLogin);
        shipment.setStatusId(ShipmentStatus.FULLY_TO_BE_DELIVERED.name());
        shipmentRepo.save(shipment);
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
        var specification = new DeliveryBillSpecification(filters);
        var deliveryBills = deliveryBillRepo.findAll(specification,pageReq);

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

    @Override
    public void simulateDeliveryBill(Shipment shipment, UserLogin userLogin) {
        try {
            // Chỉ tạo delivery bill cho shipment đã exported
            if (!ShipmentStatus.EXPORTED.name().equals(shipment.getStatusId())) {
                return;
            }

            // Random 80% chance tạo delivery bill
            if (ThreadLocalRandom.current().nextDouble() > 0.8) {
                return;
            }

            // Lấy inventory item details
            List<InventoryItemDetail> inventoryItemDetails = inventoryItemDetailRepo.findByShipmentId(shipment.getId());
            if (inventoryItemDetails.isEmpty()) {
                return;
            }

            Facility facility = inventoryItemDetails.get(0).getFacility();

            // Tạo delivery bill
            DeliveryBill deliveryBill = DeliveryBill.builder()
                    .id(SnowFlakeIdGenerator.getInstance().nextId(DELIVERY_BILL_ID_PREFIX))
                    .deliveryBillName("Phiếu giao hàng từ " + shipment.getShipmentName())
                    .shipment(shipment)
                    .toCustomer(shipment.getToCustomer())
                    .facility(facility)
                    .expectedDeliveryDate(shipment.getExpectedDeliveryDate() != null ?
                            shipment.getExpectedDeliveryDate() : LocalDate.now().plusDays(1))
                    .priority(ThreadLocalRandom.current().nextInt(1, 4))
                    .statusId(DeliveryBillStatus.CREATED.name())
                    .deliveryStatusId(DeliveryBillTripStatus.UNASSIGNED.name())
                    .createdByUser(userLogin)
                    .note("Phiếu giao hàng simulation")
                    .build();

            deliveryBill.setCreatedStamp(shipment.getCreatedStamp().plusHours(
                    ThreadLocalRandom.current().nextInt(1, 6)));

            // Tạo delivery bill items
            List<DeliveryBillItem> deliveryBillItems = new ArrayList<>();
            BigDecimal totalWeight = BigDecimal.ZERO;
            int totalQuantity = 0;
            int seq = 1;

            for (InventoryItemDetail detail : inventoryItemDetails) {
                double deliveryRatio = 0.7 + ThreadLocalRandom.current().nextDouble(0.3); // 70%-100%
                int deliveryQuantity = Math.max(1, (int) (detail.getQuantity() * deliveryRatio));

                DeliveryBillItem deliveryBillItem = DeliveryBillItem.builder()
                        .id(SnowFlakeIdGenerator.getInstance().nextId(DELIVERY_BILL_ITEM_ID_PREFIX))
                        .deliveryBill(deliveryBill)
                        .product(detail.getProduct())
                        .quantity(deliveryQuantity)
                        .price(detail.getPrice())
                        .weight(detail.getProduct().getWeight())
                        .unit(detail.getUnit())
                        .deliveryBillItemSeqId(seq++)
                        .build();

                deliveryBillItem.setCreatedStamp(deliveryBill.getCreatedStamp());
                deliveryBillItems.add(deliveryBillItem);

                totalQuantity += deliveryQuantity;
                if (deliveryBillItem.getWeight() != null) {
                    totalWeight = totalWeight.add(
                            deliveryBillItem.getWeight().multiply(BigDecimal.valueOf(deliveryQuantity))
                    );
                }
            }

            deliveryBill.setTotalWeight(totalWeight);
            deliveryBill.setTotalQuantity(totalQuantity);

            // Cập nhật status shipment
            shipment.setStatusId(ShipmentStatus.FULLY_TO_BE_DELIVERED.name());

            // Lưu database
            shipmentRepo.save(shipment);
            deliveryBillRepo.save(deliveryBill);
            deliveryBillItemRepo.saveAll(deliveryBillItems);

            log.debug("Simulated delivery bill {} for shipment {}",
                    deliveryBill.getId(), shipment.getId());

        } catch (Exception e) {
            log.error("Error simulating delivery bill for shipment {}: {}",
                    shipment.getId(), e.getMessage());
        }
    }
}

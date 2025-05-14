package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderType;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.PurchaseOrderGetListFilter;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.CreatePurchaseOrderReq;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.PurchaseOrderDetailRes;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.PurchaseOrderListRes;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import openerp.openerpresourceserver.wms.entity.OrderItem;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.repository.specification.PurchaseOrderSpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final GeneralMapper generalMapper;
    private final FacilityRepo facilityRepo;
    private final OrderHeaderRepo orderHeaderRepo;
    private final SupplierRepo supplierRepo;
    private final OrderItemRepo orderItemRepo;
    private final UserLoginRepo userLoginRepo;
    private final ProductRepo productRepo;

    @Override
    public ApiResponse<Void> createPurchaseOrder(CreatePurchaseOrderReq purchaseOrder, String name) {
        var facility = facilityRepo.findById(purchaseOrder.getFacilityId()).orElseThrow(
                () -> new DataNotFoundException("Facility not found with id: " + purchaseOrder.getFacilityId()));

        var supplier = supplierRepo.findById(purchaseOrder.getSupplierId()).orElseThrow(
                () -> new DataNotFoundException("Supplier not found with id: " + purchaseOrder.getSupplierId()));

        var userLogin = userLoginRepo.findById(name).orElseThrow(
                () -> new DataNotFoundException("User not found with id: " + name));

        var orderHeader = generalMapper.convertToEntity(purchaseOrder, OrderHeader.class);
        orderHeader.setId(CommonUtil.getUUID());
        orderHeader.setFacility(facility);
        orderHeader.setFromSupplier(supplier);
        orderHeader.setCreatedByUser(userLogin);
        orderHeader.setOrderTypeId(OrderType.PURCHASE_ORDER.name());
        orderHeader.setStatus(OrderStatus.CREATED.name());

        List<OrderItem> orderItemList= new ArrayList<>();
        AtomicInteger seq = new AtomicInteger(0);
        purchaseOrder.getOrderItems().forEach(item -> {
            var orderItem = generalMapper.convertToEntity(item, OrderItem.class);
            orderItem.setOrder(orderHeader);
            var product = productRepo.findById(item.getProductId()).orElseThrow(
                    () -> new DataNotFoundException("Product not found with id: " + item.getProductId()));
            orderItem.setOrderItemSeqId(CommonUtil.getSequenceId("ORDITEM", 5, seq.getAndIncrement()));
            orderItem.setProduct(product);
            orderItem.setId(CommonUtil.getUUID());
            orderItem.setAmount(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemList.add(orderItem);
        });

        orderHeaderRepo.save(orderHeader);
        orderItemRepo.saveAll(orderItemList);

        return ApiResponse.<Void>builder()
                .code(201)
                .message("Create purchase order successfully")
                .build();


    }

    @Override
    public ApiResponse<Pagination<PurchaseOrderListRes>> getAllPurchaseOrder(int page, int limit, PurchaseOrderGetListFilter filters) {
        var pageReq = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdStamp"));
        var specification = new PurchaseOrderSpecification(filters);
        var purchaseOrders = orderHeaderRepo.findAll(specification, pageReq);
        List<PurchaseOrderListRes> purchaseOrderListRes = purchaseOrders.getContent().stream()
                .map(purchaseOrder -> {
                    var orderListRes = generalMapper.convertToDto(purchaseOrder, PurchaseOrderListRes.class);
                    orderListRes.setSupplierName(purchaseOrder.getFromSupplier().getName());
                    orderListRes.setFacilityName(purchaseOrder.getFacility().getName());
                    orderListRes.setCreatedByUserName(purchaseOrder.getCreatedByUser().getFullName());
                    return orderListRes;
                })
                .toList();

        var PagRes = Pagination.<PurchaseOrderListRes>builder()
                .page(page)
                .size(limit)
                .totalPages(purchaseOrders.getTotalPages())
                .totalElements(purchaseOrders.getTotalElements())
                .data(purchaseOrderListRes)
                .build();

        return ApiResponse.<Pagination<PurchaseOrderListRes>>builder()
                .code(200)
                .message("Get all purchase order successfully")
                .data(PagRes)
                .build();



    }

    @Override
    public ApiResponse<PurchaseOrderDetailRes> getPurchaseOrderDetail(String id) {
        var purchaseOrder = orderHeaderRepo.findById(id).orElseThrow(
                () -> new DataNotFoundException("Purchase order not found with id: " + id));

        var purchaseOrderDetailRes = generalMapper.convertToDto(purchaseOrder, PurchaseOrderDetailRes.class);
        purchaseOrderDetailRes.setSupplierName(purchaseOrder.getFromSupplier().getName());
        purchaseOrderDetailRes.setFacilityName(purchaseOrder.getFacility().getName());
        purchaseOrderDetailRes.setCreatedByUser(purchaseOrder.getCreatedByUser().getFullName());
        purchaseOrderDetailRes.setStatus(purchaseOrder.getStatus());
        purchaseOrderDetailRes.setDeliveryAfterDate(purchaseOrder.getDeliveryAfterDate());
        purchaseOrderDetailRes.setNote(purchaseOrder.getNote());


        var orderItemList = purchaseOrder.getOrderItems().stream()
                .map(orderItem -> {
                    var orderItemRes = generalMapper.convertToDto(orderItem, openerp.openerpresourceserver.wms.dto.saleOrder.OrderProductRes.class);
                    orderItemRes.setProductName(orderItem.getProduct().getName());
                    orderItemRes.setProductId(orderItem.getProduct().getId());
                    return orderItemRes;
                })
                .toList();
        purchaseOrderDetailRes.setOrderItems(orderItemList);

        return ApiResponse.<PurchaseOrderDetailRes>builder()
                .code(200)
                .message("Get purchase order detail successfully")
                .data(purchaseOrderDetailRes)
                .build();
    }
}

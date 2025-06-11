package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.algorithm.SnowFlakeIdGenerator;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderType;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.JsonReq;
import openerp.openerpresourceserver.wms.dto.OrderItemReq;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.PurchaseOrderGetListFilter;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.CreatePurchaseOrderReq;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.PurchaseOrderDetailRes;
import openerp.openerpresourceserver.wms.dto.purchaseOrder.PurchaseOrderListRes;
import openerp.openerpresourceserver.wms.entity.OrderHeader;
import openerp.openerpresourceserver.wms.entity.OrderItem;
import openerp.openerpresourceserver.wms.entity.Product;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.repository.specification.PurchaseOrderSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static io.micrometer.common.util.StringUtils.isBlank;
import static openerp.openerpresourceserver.wms.constant.Constants.ORDER_ITEM_ID_PREFIX;

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
    public ApiResponse<Void> createPurchaseOrder(CreatePurchaseOrderReq req, String name) {
        var supplier = supplierRepo.findById(req.getSupplierId()).orElseThrow(
                () -> new DataNotFoundException("Supplier not found with id: " + req.getSupplierId()));

        var userLogin = userLoginRepo.findById(name).orElseThrow(
                () -> new DataNotFoundException("User not found with id: " + name));

        var orderHeader = generalMapper.convertToEntity(req, OrderHeader.class);

        if(isBlank(orderHeader.getId())) {
            orderHeader.setId(SnowFlakeIdGenerator.getInstance().nextId(ORDER_ITEM_ID_PREFIX));
        }

        orderHeader.setFromSupplier(supplier);
        orderHeader.setCreatedByUser(userLogin);
        orderHeader.setOrderTypeId(OrderType.PURCHASE_ORDER.name());
        orderHeader.setStatusId(OrderStatus.CREATED.name());

        var orderItemReqList = req.getOrderItems();

        var productIds = orderItemReqList.stream()
                .map(OrderItemReq::getProductId)
                .toList();

        var productIdMap = productRepo.findAllById(productIds).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, Function.identity()));

        var orderItemList= new ArrayList<OrderItem>();

        var totalAmount = BigDecimal.ZERO;
        var totalQuantity = 0;
        var seq = 1;

        for(var orderItemReq : req.getOrderItems()) {
            var product = productIdMap.get(orderItemReq.getProductId());
            if (product == null) {
                throw new DataNotFoundException("Product not found with id: " + orderItemReq.getProductId());
            }

            var orderItem = generalMapper.convertToEntity(orderItemReq, OrderItem.class);
            orderItem.setOrder(orderHeader);
            orderItem.setProduct(product);
            orderItem.setOrderItemSeqId(seq++);
            var amount = ((orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                    .subtract(orderItem.getDiscount()))
                    .multiply(BigDecimal.ONE.add(orderItem.getTax()));
            orderItem.setAmount(amount);
            totalAmount = totalAmount.add(amount);
            totalQuantity += orderItemReq.getQuantity();
            orderItemList.add(orderItem);
        }

        for (JsonReq importCost : req.getCosts()) {
            var cost = (Integer) importCost.getValue();
            totalAmount = totalAmount.add(BigDecimal.valueOf(cost));
        }

        totalAmount = totalAmount.subtract(req.getDiscount());

        orderHeader.setTotalAmount(totalAmount);
        orderHeader.setTotalQuantity(totalQuantity);

        orderHeaderRepo.save(orderHeader);
        orderItemRepo.saveAll(orderItemList);

        return ApiResponse.<Void>builder()
                .code(201)
                .message("Create purchase order successfully")
                .build();


    }

    @Override
    public ApiResponse<Pagination<PurchaseOrderListRes>> getAllPurchaseOrder(int page, int limit, PurchaseOrderGetListFilter filters) {
        var pageReq = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdStamp"));
        var specification = new PurchaseOrderSpecification(filters);
        var purchaseOrders = orderHeaderRepo.findAll(specification, pageReq);
        List<PurchaseOrderListRes> purchaseOrderListRes = purchaseOrders.getContent().stream()
                .map(purchaseOrder -> {
                    var orderListRes = generalMapper.convertToDto(purchaseOrder, PurchaseOrderListRes.class);
                    orderListRes.setSupplierName(purchaseOrder.getFromSupplier().getName());
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
        purchaseOrderDetailRes.setCreatedByUser(purchaseOrder.getCreatedByUser().getFullName());
        purchaseOrderDetailRes.setStatus(purchaseOrder.getStatusId());
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

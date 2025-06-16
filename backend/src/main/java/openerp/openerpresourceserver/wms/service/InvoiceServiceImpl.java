package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.algorithm.SnowFlakeIdGenerator;
import openerp.openerpresourceserver.wms.constant.enumrator.InventoryStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.InvoiceType;
import openerp.openerpresourceserver.wms.constant.enumrator.OrderItemBillingType;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.invoice.InvoiceDTO;
import openerp.openerpresourceserver.wms.entity.*;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;

import static openerp.openerpresourceserver.wms.constant.Constants.*;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepo invoiceRepo;
    private final InvoiceItemRepo invoiceItemRepo;
    private final ShipmentRepo shipmentRepo;
    private final UserLoginRepo userLoginRepo;
    private final InventoryItemDetailRepo inventoryItemDetailRepo;
    private final InventoryItemRepo inventoryItemRepo;
    private final OrderItemBillingRepo orderItemBillingRepo;
    private final GeneralMapper generalMapper;
    private final AddressRepo addressRepo;

    @Override
    @Transactional
    public ApiResponse<Void> exportOutBound(String shipmentId, String name) {
        var shipment = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Shipment not found with id: " + shipmentId));

        var inventoryItemDetails = inventoryItemDetailRepo.findByShipmentId(shipmentId);

//        for (var inventoryItemDetail : inventoryItemDetails) {
//            var inventoryItem = inventoryItemDetail.getInventoryItem();
//            inventoryItem.setQuantity(inventoryItem.getQuantity() - inventoryItemDetail.getQuantity());
//            inventoryItemRepo.save(inventoryItem);
//        }

        for(var inventoryItemDetail : inventoryItemDetails) {
            var facilityId = inventoryItemDetail.getFacility().getId();
            var productId = inventoryItemDetail.getProduct().getId();
            // sorted by expiration date
            var inventoryItems = inventoryItemRepo.findByProductIdAndFacilityId(productId, facilityId)
                    .stream()
                    .sorted(Comparator.comparing(InventoryItem::getExpirationDate))
                    .toList();

            if(inventoryItems.isEmpty()) {
                throw new DataNotFoundException("Inventory item not found for product: " + productId + " and facility: " + facilityId);
            }

            var totalAvailableQuantity = inventoryItems.stream()
                    .mapToInt(InventoryItem::getQuantity)
                    .sum();

            if(totalAvailableQuantity < inventoryItemDetail.getQuantity()) {
                throw new RuntimeException("Not enough inventory for product: " + productId + " and facility: " + facilityId);
            }

            // for each inventory item, reduce the quantity until the required quantity is met
            int remainingQuantity = inventoryItemDetail.getQuantity();
            for (var inventoryItem : inventoryItems) {
                if (remainingQuantity <= 0) {
                    break;
                }
                if (inventoryItem.getQuantity() >= remainingQuantity) {
                    inventoryItem.setQuantity(inventoryItem.getQuantity() - remainingQuantity);
                    inventoryItemRepo.save(inventoryItem);
                    remainingQuantity = 0;
                } else {
                    remainingQuantity -= inventoryItem.getQuantity();
                    inventoryItem.setQuantity(0);
                    inventoryItemRepo.save(inventoryItem);
                }
            }
        }

        //Create Invoice here
        var newInvoice = Invoice.builder()
                .id(SnowFlakeIdGenerator.getInstance().nextId(INVOICE_ID_PREFIX))
                .invoiceName("Hóa đơn bán - " + shipment.getShipmentName())
                .invoiceTypeId(InvoiceType.SALES_INVOICE.name())
                .createdByUser(userLoginRepo.findById(name).orElseThrow(()
                        -> new DataNotFoundException("User not found with name: " + name)))
                .toCustomer(shipment.getToCustomer())
                        .build();

        var newInvoiceItems = new ArrayList<InvoiceItem>();
        var newOrderItemBillings = new ArrayList<OrderItemBilling>();

        var seq = 1;

        for (var inventoryItemDetail : inventoryItemDetails) {
            var newInvoiceItem = InvoiceItem.builder()
                    .id(CommonUtil.getUUID())
                    .invoiceItemSeqId(seq++)
                    .invoice(newInvoice)
                    .build();
            newInvoiceItems.add(newInvoiceItem);

            var orderItemBilling = OrderItemBilling.builder()
                    .id(CommonUtil.getUUID())
                    .orderItem(inventoryItemDetail.getOrderItem())
                    .invoiceItem(newInvoiceItem)
                    .inventoryItemDetail(inventoryItemDetail)
                    .facility(inventoryItemDetail.getFacility())
                    .product(inventoryItemDetail.getProduct())
                    .quantity(inventoryItemDetail.getQuantity())
                    .orderItemBillingTypeId(OrderItemBillingType.SALES_BILLING.name())
                    .unit(inventoryItemDetail.getProduct().getUnit())
                    .amount(inventoryItemDetail.getProduct().getWholeSalePrice().multiply(BigDecimal.valueOf(inventoryItemDetail.getQuantity())))
                    .build();
            newOrderItemBillings.add(orderItemBilling);
        }

        invoiceRepo.save(newInvoice);
        invoiceItemRepo.saveAll(newInvoiceItems);
        orderItemBillingRepo.saveAll(newOrderItemBillings);


        shipment.setStatusId(ShipmentStatus.EXPORTED.name());
        shipment.setHandledByUser(userLoginRepo.findById(name)
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + name)));

        shipmentRepo.save(shipment);

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Export shipment success")
                .build();
    }

    @Override
    public ApiResponse<Void> importInBound(String shipmentId, String name) {
        // Logic is similar to exportOutBound but increseases the quantity
        var shipment = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Shipment not found with id: " + shipmentId));

        var inventoryItemDetails = inventoryItemDetailRepo.findByShipmentId(shipmentId);

        for (var inventoryItemDetail : inventoryItemDetails) {
            var facilityId = inventoryItemDetail.getFacility().getId();
            var productId = inventoryItemDetail.getProduct().getId();
            var lotId = inventoryItemDetail.getLotId();
            var manufacturingDate = inventoryItemDetail.getManufacturingDate();
            var expirationDate = inventoryItemDetail.getExpirationDate();
            var inventoryItem = inventoryItemRepo.findByFacilityIdAndProductIdAndLotIdAndManufacturingDateAndExpirationDate(
                    facilityId, productId, lotId, manufacturingDate, expirationDate);

            if(inventoryItem == null) {
                // add new inventory item if it does not exist
                var newInventoryItem = generalMapper.convertToEntity(inventoryItemDetail, InventoryItem.class);
                newInventoryItem.setId(SnowFlakeIdGenerator.getInstance().nextId(INVENTORY_ITEM_ID_PREFIX));
                newInventoryItem.setStatusId(InventoryStatus.VALID.name());
                inventoryItemRepo.save(newInventoryItem);
            } else {
                inventoryItem.setQuantity(inventoryItem.getQuantity() + inventoryItemDetail.getQuantity());
                inventoryItemRepo.save(inventoryItem);
            }
        }

        //Create Invoice here
        var newInvoice = Invoice.builder()
                .id(SnowFlakeIdGenerator.getInstance().nextId(INVOICE_ID_PREFIX))
                .invoiceName("Hóa đơn mua - " + shipment.getShipmentName())
                .invoiceTypeId(InvoiceType.PURCHASE_INVOICE.name())
                .createdByUser(userLoginRepo.findById(name).orElseThrow(()
                        -> new DataNotFoundException("User not found with name: " + name)))
                .fromSupplier(shipment.getFromSupplier())
                .build();

        var newInvoiceItems = new ArrayList<InvoiceItem>();
        var newOrderItemBillings = new ArrayList<OrderItemBilling>();
        var seq = 1;

        for (var inventoryItemDetail : inventoryItemDetails) {
            var newInvoiceItem = InvoiceItem.builder()
                    .id(SnowFlakeIdGenerator.getInstance().nextId(INVENTORY_ITEM_ID_PREFIX))
                    .invoiceItemSeqId(seq++)
                    .invoice(newInvoice)
                    .build();
            newInvoiceItems.add(newInvoiceItem);

            var orderItemBilling = OrderItemBilling.builder()
                    .id(SnowFlakeIdGenerator.getInstance().nextId(ORDER_ITEM_BILLING_ID_PREFIX))
                    .orderItem(inventoryItemDetail.getOrderItem())
                    .invoiceItem(newInvoiceItem)
                    .inventoryItemDetail(inventoryItemDetail)
                    .facility(inventoryItemDetail.getFacility())
                    .product(inventoryItemDetail.getProduct())
                    .quantity(inventoryItemDetail.getQuantity())
                    .orderItemBillingTypeId(OrderItemBillingType.PURCHASE_BILLING.name())
                    .unit(inventoryItemDetail.getProduct().getUnit())
                    .amount(inventoryItemDetail.getProduct().getWholeSalePrice().multiply(BigDecimal.valueOf(inventoryItemDetail.getQuantity())))
                    .build();
            newOrderItemBillings.add(orderItemBilling);
        }

        invoiceRepo.save(newInvoice);
        invoiceItemRepo.saveAll(newInvoiceItems);
        orderItemBillingRepo.saveAll(newOrderItemBillings);
        shipment.setStatusId(ShipmentStatus.IMPORTED.name());
        shipment.setHandledByUser(userLoginRepo.findById(name)
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + name)));
        shipmentRepo.save(shipment);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Import shipment success")
                .build();

    }

    @Override
    public ApiResponse<InvoiceDTO> getInvoiceByShipmentId(String shipmentId) {
            var shipment = shipmentRepo.findById(shipmentId)
                    .orElseThrow(() -> new DataNotFoundException("Shipment not found with id: " + shipmentId));
            
            var orderItemBillings = orderItemBillingRepo.findByShipmentId(shipmentId);
            if (orderItemBillings.isEmpty()) {
                throw new DataNotFoundException("No invoice found for shipment: " + shipmentId);
            }
            
            var invoiceItem = orderItemBillings.get(0).getInvoiceItem();
            var invoice = invoiceItem.getInvoice();
            
            return ApiResponse.<InvoiceDTO>builder()
                    .code(200)
                    .message("Invoice retrieved successfully")
                    .data(buildInvoiceDTO(invoice, shipment))
                    .build();

    }

    @Override
    public ApiResponse<InvoiceDTO> getInvoiceById(String invoiceId) {
            var invoice = invoiceRepo.findById(invoiceId)
                    .orElseThrow(() -> new DataNotFoundException("Invoice not found with id: " + invoiceId));
            
            return ApiResponse.<InvoiceDTO>builder()
                    .code(200)
                    .message("Invoice retrieved successfully")
                    .data(buildInvoiceDTO(invoice, null))
                    .build();
    }

    private InvoiceDTO buildInvoiceDTO(Invoice invoice, Shipment shipment) {
        var orderItemBillings = orderItemBillingRepo.findByInvoiceId(invoice.getId());
        
        var invoiceItems = orderItemBillings.stream()
                .map(this::buildInvoiceItemDTO)
                .toList();
        
        var subtotal = invoiceItems.stream()
                .map(InvoiceDTO.InvoiceItemDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalDiscount = invoiceItems.stream()
                .map(InvoiceDTO.InvoiceItemDTO::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalTax = invoiceItems.stream()
                .map(InvoiceDTO.InvoiceItemDTO::getTax)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalQuantity = invoiceItems.stream()
                .mapToInt(InvoiceDTO.InvoiceItemDTO::getQuantity)
                .sum();
        
        var totalAmount = subtotal.subtract(totalDiscount).add(totalTax);

        InvoiceDTO.CustomerInfoDTO customerInfo = null;
        InvoiceDTO.SupplierInfoDTO supplierInfo = null;
        
        if (invoice.getToCustomer() != null) {
            var address = addressRepo.findById(
                    invoice.getToCustomer().getCurrentAddressId()
            ).orElseThrow(
                    () -> new DataNotFoundException("Customer address not found for customer: " + invoice.getToCustomer().getId())
            );
            var customer = invoice.getToCustomer();
            customerInfo = generalMapper.convertToEntity(
                    customer, InvoiceDTO.CustomerInfoDTO.class);
            customerInfo.setFullAddress(address.getFullAddress());
        }
        
        if (invoice.getFromSupplier() != null) {
            var address = addressRepo.findById(
                    invoice.getFromSupplier().getCurrentAddressId()
            ).orElseThrow(
                    () -> new DataNotFoundException("Supplier address not found for supplier: " + invoice.getFromSupplier().getId())
            );
            var supplier = invoice.getFromSupplier();
            supplierInfo = generalMapper.convertToEntity(
                    supplier, InvoiceDTO.SupplierInfoDTO.class);
            supplierInfo.setFullAddress(address.getFullAddress());
        }
        
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceName(invoice.getInvoiceName())
                .invoiceTypeId(invoice.getInvoiceTypeId())
                .statusId(invoice.getStatusId())
                .createdStamp(invoice.getCreatedStamp())
                .customerInfo(customerInfo)
                .supplierInfo(supplierInfo)
                .items(invoiceItems)
                .subtotal(subtotal)
                .totalDiscount(totalDiscount)
                .totalTax(totalTax)
                .totalAmount(totalAmount)
                .totalQuantity(totalQuantity)
                .createdByUserName(invoice.getCreatedByUser() != null ?
                        invoice.getCreatedByUser().getFullName() : "N/A")
                .build();
    }

    private InvoiceDTO.InvoiceItemDTO buildInvoiceItemDTO(OrderItemBilling billing) {
        var product = billing.getProduct();
        var orderItem = billing.getOrderItem();
        
        BigDecimal discount = orderItem.getDiscount() != null ? orderItem.getDiscount() : BigDecimal.ZERO;
        BigDecimal tax = orderItem.getTax() != null ? orderItem.getTax() : BigDecimal.ZERO;
        
        return InvoiceDTO.InvoiceItemDTO.builder()
                .id(billing.getId())
                .seqId(billing.getInvoiceItem().getInvoiceItemSeqId())
                .productId(product.getId())
                .productName(product.getName())
                .unit(billing.getUnit())
                .quantity(billing.getQuantity())
                .discount(discount)
                .tax(tax)
                .price(orderItem.getPrice())
                .amount(billing.getAmount())
                .build();
    }
}

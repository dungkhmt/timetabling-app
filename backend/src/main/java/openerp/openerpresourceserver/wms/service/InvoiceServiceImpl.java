package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.entity.Invoice;
import openerp.openerpresourceserver.wms.entity.InvoiceItem;
import openerp.openerpresourceserver.wms.entity.InvoiceItemPK;
import openerp.openerpresourceserver.wms.entity.OrderItemBilling;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.repository.*;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Override
    @Transactional
    public ApiResponse<Void> exportShipment(String shipmentId, String name) {
        var shipment = shipmentRepo.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Shipment not found with id: " + shipmentId));

        var inventoryItemDetails = inventoryItemDetailRepo.findByShipmentId(shipmentId);

        for (var inventoryItemDetail : inventoryItemDetails) {
            var inventoryItem = inventoryItemDetail.getInventoryItem();
            inventoryItem.setQuantity(inventoryItem.getQuantity() - inventoryItemDetail.getQuantity());
            inventoryItemRepo.save(inventoryItem);
        }

        //Create Invoice here
        var newInvoice = Invoice.builder()
                .createdByUser(userLoginRepo.findById(name).orElseThrow(()
                        -> new DataNotFoundException("User not found with name: " + name)))
                .toCustomer(shipment.getToCustomer())
                        .build();

        var newInvoiceItems = new ArrayList<InvoiceItem>();
        var newOrderItemBillings = new ArrayList<OrderItemBilling>();

        AtomicInteger index = new AtomicInteger(0);

        for (var inventoryItemDetail : inventoryItemDetails) {
            var newInvoiceItem = InvoiceItem.builder()
                    .id(CommonUtil.getUUID())
                    .invoiceItemSeqId(CommonUtil.getSequenceId("IVIT", 5, index.getAndIncrement()))
                    .invoice(newInvoice)
                    .build();
            newInvoiceItems.add(newInvoiceItem);

            var orderItemBilling = OrderItemBilling.builder()
                    .id(CommonUtil.getUUID())
                    .orderItem(inventoryItemDetail.getOrderItem())
                    .invoiceItem(newInvoiceItem)
                    .shipment(shipment)
                    .product(inventoryItemDetail.getProduct())
                    .quantity(inventoryItemDetail.getQuantity())
                    .amount(inventoryItemDetail.getProduct().getWholeSalePrice().multiply(BigDecimal.valueOf(inventoryItemDetail.getQuantity())))
                    .build();
            newOrderItemBillings.add(orderItemBilling);
        }

        invoiceRepo.save(newInvoice);
        invoiceItemRepo.saveAll(newInvoiceItems);
        orderItemBillingRepo.saveAll(newOrderItemBillings);



        shipment.setShipmentStatusId(ShipmentStatus.EXPORTED.name());
        shipment.setExportedByUser(userLoginRepo.findById(name)
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + name)));

        shipmentRepo.save(shipment);

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Export shipment success")
                .build();
    }
}

package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "wms2_order_item_billing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemBilling extends BaseEntity {
    @Id
    private String id;

    private String orderItemBillingTypeId;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_item_id")
    private InvoiceItem invoiceItem;

//    @ManyToOne
//    @JoinColumn(name = "shipment_id")
//    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @OneToOne
    @JoinColumn(name = "inventory_item_detail_id")
    private InventoryItemDetail inventoryItemDetail;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    private BigDecimal amount;

    private String unit;

}

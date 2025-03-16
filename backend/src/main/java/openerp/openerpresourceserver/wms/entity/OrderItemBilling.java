package openerp.openerpresourceserver.wms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

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
    @Column(name = "id", length = 40)
    private String id;



    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "order_id", referencedColumnName = "order_id", insertable = false, updatable = false),
            @JoinColumn(name = "order_item_seq_id", referencedColumnName = "order_item_seq_id", insertable = false, updatable = false)
    })
    private OrderItem orderItem;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "invoice_id", referencedColumnName = "invoice_id", insertable = false, updatable = false),
            @JoinColumn(name = "invoice_item_seq_id", referencedColumnName = "invoice_item_seq_id", insertable = false, updatable = false)
    })
    private InvoiceItem invoiceItem;

    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "amount", precision = 25, scale = 5)
    private BigDecimal amount;

}

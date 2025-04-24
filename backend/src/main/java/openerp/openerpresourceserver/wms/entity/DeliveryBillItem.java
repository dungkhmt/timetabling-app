package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_delivery_bill_item")
public class DeliveryBillItem {
    @Id
    private String id;

    @Column(name = "delivery_bill_id", length = 40)
    private String deliveryBillId;

    @Column(name = "delivery_bill_item_seq_id", length = 10)
    private String deliveryBillItemSeqId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}

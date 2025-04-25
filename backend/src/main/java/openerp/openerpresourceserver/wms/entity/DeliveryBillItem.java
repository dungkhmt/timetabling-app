package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "wms2_delivery_bill_item")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryBillItem extends BaseEntity{
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "delivery_bill_id")
    private DeliveryBill deliveryBill;

    @Column(name = "delivery_bill_item_seq_id", length = 10)
    private String deliveryBillItemSeqId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}

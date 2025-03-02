package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_delivery_bill_item")
public class DeliveryBillItem {
    @EmbeddedId
    private DeliveryBillItemPK id;

    @MapsId("deliveryBillId")
    @ManyToOne
    @JoinColumn(name = "delivery_bill_id")
    private DeliveryBill deliveryBill;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}

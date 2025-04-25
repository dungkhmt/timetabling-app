package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_delivery_route_item")
public class DeliveryRouteItem {
    @Id
    private String id;

    @Column(name = "delivery_route_seq_id", length = 10)
    private String deliveryRouteSeqId;

    @ManyToOne
    @JoinColumn(name = "delivery_route_id")
    private DeliveryRoute deliveryRoute;

    @Column(name = "seq", nullable = false)
    private Integer seq;

    @ManyToOne
    @JoinColumn(name = "delivery_bill_id")
    private DeliveryBill deliveryBill;

    @Column(name = "status_id", length = 100)
    private String statusId;
}

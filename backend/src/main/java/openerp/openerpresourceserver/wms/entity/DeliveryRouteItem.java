package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_delivery_route_item")
public class DeliveryRouteItem {
    @Id
    private String id;

    private String deliveryRouteSeqId;

    @ManyToOne
    @JoinColumn(name = "delivery_route_id")
    private DeliveryRoute deliveryRoute;

    private Integer sequenceId;

    @ManyToOne
    @JoinColumn(name = "delivery_bill_id")
    private DeliveryBill deliveryBill;

    private String statusId;
}

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
@Table(name = "wms2_delivery_route_item")
public class DeliveryRouteItem {
    @EmbeddedId
    private DeliveryRouteItemPK id;

    @MapsId("deliveryRouteId")
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

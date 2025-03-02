package openerp.openerpresourceserver.wms.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class DeliveryRouteItemPK implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "delivery_route_id", length = 40)
    private String deliveryRouteId;

    @Column(name = "delivery_route_seq_id", length = 10)
    private String deliveryRouteSeqId;
}

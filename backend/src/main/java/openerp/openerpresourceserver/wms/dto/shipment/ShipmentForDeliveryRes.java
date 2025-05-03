package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ShipmentForDeliveryRes {
    private String id;
    private String shipmentName;
    private String shipmentStatusId;
    private String customerName;
}

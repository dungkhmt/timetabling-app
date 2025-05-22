package openerp.openerpresourceserver.wms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRouteGetListRes {
    private String id;

    private String deliveryPlanId;

    private String deliveryPlanName;

    private String assignToShipperId;

    private String assignToShipperName;

    private String assignToVehicleId;

    private String assignToVehicleName;

    private String statusId;
}

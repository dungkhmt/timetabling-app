package openerp.openerpresourceserver.wms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryRoutePlanRes {
    private String id;

    private String deliveryPlanId;

    private String assignToShipperId;

    private String assignToShipperName;

    private String assignToVehicleId;

    private String assignToVehicleName;

    private String statusId;
}

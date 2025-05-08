package openerp.openerpresourceserver.wms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.wms.entity.DeliveryRoute;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPlanDetailRes {
    private String id;

    private String description;

    private String statusId;

    private String createdByUserName;

    private BigDecimal totalWeight;

    private String delveryPlanName;

    private LocalDate deliveryDate;

    private String facilityName;

    List<ShipperDeliveryPlanRes> shippers;

    List<DeliveryBillPlanRes> deliveryBills;

    List<DeliveryRoutePlanRes> existingRoutes;
}

package openerp.openerpresourceserver.wms.dto.delivery;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShipperDeliveryPlanRes {

    private String userLoginId;

    private String shipperName;

    @Column(name = "status_id", length = 100)
    private String statusId;

    @Column(name = "last_latitude")
    private BigDecimal lastLatitude;

    @Column(name = "last_longitude")
    private BigDecimal lastLongitude;
}

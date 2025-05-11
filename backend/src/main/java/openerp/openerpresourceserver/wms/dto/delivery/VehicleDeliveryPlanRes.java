package openerp.openerpresourceserver.wms.dto.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDeliveryPlanRes {
    private String id;

    private String vehicleName;

    private String vehicleTypeId;

    private BigDecimal capacity;

    private Integer length;

    private Integer width;

    private Integer height;

    private String statusId;

    private String description;
}

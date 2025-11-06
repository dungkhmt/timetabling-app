package openerp.openerpresourceserver.wms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPlanPageRes {

    private String id;

    private String description;

    private String statusId;

    private String createdByUserName;

    private BigDecimal totalWeight;

    private String deliveryPlanName;

    private LocalDate deliveryDate;

    private String facilityName;

    private LocalDateTime createdStamp;
}

package openerp.openerpresourceserver.wms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityMovementDto {
    private String facilityId;
    private String facilityName;
    private int importQuantity;
    private int exportQuantity;
}

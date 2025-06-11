package openerp.openerpresourceserver.wms.dto.inventoryItem;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacilityForOrderRes {
    private String facilityName;
    private String facilityId;
    private Integer quantity;
}

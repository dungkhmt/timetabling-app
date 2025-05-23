package openerp.openerpresourceserver.wms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipperPerformanceDto {
    private String shipperId;
    private String shipperName;
    private int assignedRoutes;
    private int completedRoutes;
    private int inProgressRoutes;
}

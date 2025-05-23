package openerp.openerpresourceserver.wms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMovementDto {
    private String date; // Format: "yyyy-MM-dd"
    private int importQuantity;
    private int exportQuantity;
}

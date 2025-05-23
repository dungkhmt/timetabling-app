package openerp.openerpresourceserver.wms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportExportReportDto {
    private List<DailyMovementDto> dailyMovements;
    private int totalImportQuantity;
    private int totalExportQuantity;
    private List<ProductMovementDto> topImportedProducts;
    private List<ProductMovementDto> topExportedProducts;
    private List<FacilityMovementDto> facilityMovements; // Only for overall report
}


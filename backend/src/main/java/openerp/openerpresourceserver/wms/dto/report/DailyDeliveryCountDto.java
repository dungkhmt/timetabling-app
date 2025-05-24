package openerp.openerpresourceserver.wms.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyDeliveryCountDto {
    private LocalDate date;
    private String formattedDate; // Format: dd/MM/yyyy
    private int billsCount;
    private int plansCount;
    private int routesCount;
}

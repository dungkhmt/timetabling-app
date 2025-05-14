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
public class DailyOrderCountDto {
    private LocalDate date;
    private int count;
    private String formattedDate; // Can be used for display (optional)
}

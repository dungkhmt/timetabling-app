package openerp.openerpresourceserver.examtimetabling.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailySessionDistributionDTO {
    private String date; // Formatted as dd/MM/yyyy
    private List<DistributionItemDTO> sessions; // Session distribution for this day
}

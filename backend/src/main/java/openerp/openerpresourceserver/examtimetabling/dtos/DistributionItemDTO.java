package openerp.openerpresourceserver.examtimetabling.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DistributionItemDTO {
    private String name;
    private long count;
}

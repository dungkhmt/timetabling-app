package openerp.openerpresourceserver.generaltimetabling.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVersionRequest {
    private String name;
    private String status;
    private Integer numberSlotsPerSession;
}

package openerp.openerpresourceserver.generaltimetabling.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeTableVersionRequest {
    private String name;
    private String status;
    private String semester;
    private String userId;
    private Integer numberSlotsPerSession;
    private String batchId;
}
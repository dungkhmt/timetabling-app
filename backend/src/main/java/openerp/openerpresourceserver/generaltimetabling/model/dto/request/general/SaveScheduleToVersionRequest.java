package openerp.openerpresourceserver.generaltimetabling.model.dto.request.general;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveScheduleToVersionRequest {
    private List<SaveScheduleItemRequest> saveRequests;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SaveScheduleItemRequest {
        private Long classSegmentId;
        private Long versionId;
        private String room;
        private Integer startTime;
        private Integer endTime;
        private Integer weekday;
    }
}
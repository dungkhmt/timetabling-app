package openerp.openerpresourceserver.generaltimetabling.model.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelInputManualAssignTimeTable {
    private Long versionId;
    private Long classSegmentId;
    private String session;
    private Integer day;
    private Integer startTime;
    private Integer duration;
    private String roomCode;
}

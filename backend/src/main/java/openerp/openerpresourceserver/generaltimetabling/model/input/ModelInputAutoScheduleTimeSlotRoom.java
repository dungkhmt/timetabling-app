package openerp.openerpresourceserver.generaltimetabling.model.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelInputAutoScheduleTimeSlotRoom {
    List<Long> ids;
    private String semester;
    private int timeLimit;
    private String algorithm;
    private int maxDaySchedule;
    private String days;// sequence os days, 2, 3, 4... (separated by a comma)
    private String slots;// sequence of slots 1, 2, 3, ... (separated by a comma)
    private Long versionId;
}


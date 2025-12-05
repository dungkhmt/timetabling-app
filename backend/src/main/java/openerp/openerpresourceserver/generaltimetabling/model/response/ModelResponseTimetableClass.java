package openerp.openerpresourceserver.generaltimetabling.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelResponseTimetableClass {
    private Integer day;
    private String session; // S (morning) and C (afternoon)
    private Integer startTime;
    private Integer endTime;
    private Integer duration;
    private String classCodes;
}

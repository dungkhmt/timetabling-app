package openerp.openerpresourceserver.teacherassignment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeClassDto {
    private Long classId;
    private Integer sessionNumber;
    private String week;
    private String shift;
    private String startTime;
    private String endTime;
    private String dayOfWeek;
}

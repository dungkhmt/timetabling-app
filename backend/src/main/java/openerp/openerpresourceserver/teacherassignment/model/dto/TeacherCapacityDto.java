package openerp.openerpresourceserver.teacherassignment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeTeacherCapacity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherCapacityDto {
    private CompositeTeacherCapacity id;

    private Integer priority;

    private String lastUpdatedStamp;

    private String createdStamp;
    private Integer score;

}

package openerp.openerpresourceserver.teacherassignment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.teacherassignment.model.entity.relation.BatchClass;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenedClassDto {
    private Long classId;
    private Long accompaniedClassId;
    private String courseId;
    private String semester;
    private String note;
    private Long maxStudents;
    private String typeProgram;
    private List<TimeClassDto> timeClasses;

    private BatchClassDto batchClass;

}

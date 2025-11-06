package openerp.openerpresourceserver.teacherassignment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeBatchTeacher;
import openerp.openerpresourceserver.thesisdefensejuryassignment.dto.TeacherDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTeacherDto {
    private CompositeBatchTeacher id;

    private TeacherDto teacher;
}

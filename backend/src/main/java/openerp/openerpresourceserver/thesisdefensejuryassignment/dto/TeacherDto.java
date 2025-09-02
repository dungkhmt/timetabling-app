package openerp.openerpresourceserver.thesisdefensejuryassignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.teacherassignment.model.dto.BatchTeacherDto;
import openerp.openerpresourceserver.teacherassignment.model.dto.TeacherCapacityDto;
import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.BatchTeacher;
import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.TeacherCapacity;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDto {
    private String id;
    private String teacherName;
    private String userLoginId;
    private int maxCredit;

    private List<TeacherCapacityDto> teacherCapacities;

}

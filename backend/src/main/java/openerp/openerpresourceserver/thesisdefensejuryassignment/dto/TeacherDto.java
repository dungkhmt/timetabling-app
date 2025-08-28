package openerp.openerpresourceserver.thesisdefensejuryassignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDto {
    private String id;
    private String teacherName;
    private String userLoginId;
    private int maxCredit;
}

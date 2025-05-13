package openerp.openerpresourceserver.examtimetabling.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamClassGroupWithUsageDTO {
    private Integer id;
    private String name;
    private boolean isUsing;
}

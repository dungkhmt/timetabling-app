package openerp.openerpresourceserver.examtimetabling.dtos;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ExamClassGroupBulkCreateDTO {
    @NotEmpty(message = "Group names list cannot be empty")
    private List<String> groupNames;
}

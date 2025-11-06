package openerp.openerpresourceserver.teacherassignment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDto {
    private Long id;
    private String semester;
    private String name;
    private String createdByUserId;

    List<BatchTeacherDto> batchTeachers;

}

package openerp.openerpresourceserver.teacherassignment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeBatchClass;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchClassDto {
    private CompositeBatchClass id;
}

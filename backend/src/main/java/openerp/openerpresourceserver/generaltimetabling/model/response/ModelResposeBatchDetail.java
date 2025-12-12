package openerp.openerpresourceserver.generaltimetabling.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingBatch;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelResposeBatchDetail {
    private Long batchId;
    private String batchName;
    private String semester;
    private List<TimeTablingBatch> batchesOfSemester;
}

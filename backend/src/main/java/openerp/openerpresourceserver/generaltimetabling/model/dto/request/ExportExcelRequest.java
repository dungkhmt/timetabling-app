package openerp.openerpresourceserver.generaltimetabling.model.dto.request;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportExcelRequest {
    private Long versionId;
    private Integer numberSlotsPerSession;
}
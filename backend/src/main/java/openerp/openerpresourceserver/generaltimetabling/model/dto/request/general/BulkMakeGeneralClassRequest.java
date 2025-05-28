package openerp.openerpresourceserver.generaltimetabling.model.dto.request.general;

import lombok.*;
import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateTimeTablingClassRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkMakeGeneralClassRequest {
    private CreateTimeTablingClassRequest classRequest;
    private int quantity;
    private String classType;
}

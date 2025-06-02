package openerp.openerpresourceserver.generaltimetabling.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkMakeGeneralClassDto {
    private CreateTimeTablingClassDto classRequest;
    private int quantity;
    private String classType;
}

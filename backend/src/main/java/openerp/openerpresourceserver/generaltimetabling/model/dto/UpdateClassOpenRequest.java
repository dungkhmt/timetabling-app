package openerp.openerpresourceserver.generaltimetabling.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClassOpenRequest {
    private Long id;
    private Integer quantity;
    private Integer quantityMax;
    private String moduleCode;
    private String moduleName;
    private String classType;
    private Integer duration;
    private String learningWeeks;
    private String promotion;
    private String crew;

}

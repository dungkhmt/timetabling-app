package openerp.openerpresourceserver.generaltimetabling.model.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelInputAdvancedFilter {
    private Long versionId;
    private String filterMinQty;
    private String filterMaxQty;
    private String filterCourseCodes;
    private String filterClassTypes;
    private Long groupId;
}

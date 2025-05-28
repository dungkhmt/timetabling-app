package openerp.openerpresourceserver.generaltimetabling.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateClassSegmentRequest {
    private String semester;
    private Long versionId;
}


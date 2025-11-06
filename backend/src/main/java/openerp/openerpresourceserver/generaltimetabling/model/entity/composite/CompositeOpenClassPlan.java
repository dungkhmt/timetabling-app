package openerp.openerpresourceserver.generaltimetabling.model.entity.composite;

import lombok.*;

import java.io.Serializable;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CompositeOpenClassPlan implements Serializable {
    private Long classCode;
    private Long part;
    private String roomId;
    private String semester;
}

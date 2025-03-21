package openerp.openerpresourceserver.generaltimetabling.model.entity.composite;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CompositeClusterClassId implements Serializable {
    private Long classId;
    private Long clusterId;
}

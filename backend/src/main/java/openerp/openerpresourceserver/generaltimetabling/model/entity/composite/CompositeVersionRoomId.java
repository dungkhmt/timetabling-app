package openerp.openerpresourceserver.generaltimetabling.model.entity.composite;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CompositeVersionRoomId {
    private Long versionId;
    private String roomId;
}

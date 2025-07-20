package openerp.openerpresourceserver.generaltimetabling.model.entity.composite;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CompositeBatchRoomId {
    private Long batchId;
    private String roomId;
}

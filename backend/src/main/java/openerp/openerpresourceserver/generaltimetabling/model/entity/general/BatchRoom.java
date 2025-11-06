package openerp.openerpresourceserver.generaltimetabling.model.entity.general;

import jakarta.persistence.*;
import lombok.*;
import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeBatchRoomId;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timetabling_batch_room")
@IdClass(CompositeBatchRoomId.class)
public class BatchRoom {
    @Id
    @Column(name="batch_id")
    private Long batchId;

    @Id
    @Column(name="room_id")
    private String roomId;
}

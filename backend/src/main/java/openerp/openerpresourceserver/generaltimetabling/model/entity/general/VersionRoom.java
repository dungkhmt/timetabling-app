package openerp.openerpresourceserver.generaltimetabling.model.entity.general;



import jakarta.persistence.*;
import lombok.*;

import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeVersionRoomId;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timetabling_version_room")
@IdClass(CompositeVersionRoomId.class)
public class VersionRoom {
    @Id
    @Column(name="version_id")
    private Long versionId;

    @Id
    @Column(name="room_id")
    private String roomId;

    @Column(name="status")
    private String status;
}


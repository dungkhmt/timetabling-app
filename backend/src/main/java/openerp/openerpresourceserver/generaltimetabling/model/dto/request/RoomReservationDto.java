package openerp.openerpresourceserver.generaltimetabling.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomReservationDto {
    private Long parentId;
    private Integer duration;
    private Long versionId;
}
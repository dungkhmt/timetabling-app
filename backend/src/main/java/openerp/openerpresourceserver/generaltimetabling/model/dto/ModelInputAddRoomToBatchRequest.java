package openerp.openerpresourceserver.generaltimetabling.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ModelInputAddRoomToBatchRequest {
    private Long batchId;
    private List<Long> roomIds;

}

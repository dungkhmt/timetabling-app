package openerp.openerpresourceserver.generaltimetabling.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ModelInputAddRoomToBatchRequest {

    @NotNull
    private Long batchId;
    private List<String> roomIds;

}

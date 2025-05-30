package openerp.openerpresourceserver.generaltimetabling.model.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelInputSearchRoom {
    private int searchRoomCapacity;
    private String timeSlots;
    private String versionId;
    private String semester;
}

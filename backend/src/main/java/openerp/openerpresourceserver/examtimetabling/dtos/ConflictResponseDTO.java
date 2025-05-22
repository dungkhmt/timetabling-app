package openerp.openerpresourceserver.examtimetabling.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictResponseDTO {
    private String roomId;
    private String date;
    private String session;
    private String conflictType; // "ROOM" or "CLASS"
    private String examClassId1; // Only for CLASS conflicts
    private String examClassId2; // Only for CLASS conflicts

}

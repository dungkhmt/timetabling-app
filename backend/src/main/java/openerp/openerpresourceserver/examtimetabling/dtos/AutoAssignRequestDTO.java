package openerp.openerpresourceserver.examtimetabling.dtos;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for exam timetabling request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoAssignRequestDTO {
    
    private UUID examTimetableId;
    
    private List<UUID> classIds;
    
    private List<String> examDates;

    public void setSuccess(boolean b) {
    }

    public void setMessage(String string) {
    }
}

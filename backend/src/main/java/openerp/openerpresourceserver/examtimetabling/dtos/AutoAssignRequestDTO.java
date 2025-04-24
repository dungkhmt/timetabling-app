package openerp.openerpresourceserver.examtimetabling.dtos;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Min;
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

    private String algorithm = "default"; 
    
    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private Integer timeLimit = 30; 

    public void setSuccess(boolean b) {
    }

    public void setMessage(String string) {
    }
}

package openerp.openerpresourceserver.examtimetabling.dtos;

import java.util.UUID;

import lombok.Data;

@Data
public class AssignmentUpdateDTO {
    private String assignmentId;
    private String roomId;
    private Integer weekNumber;
    private String date;
    private String sessionId;
}

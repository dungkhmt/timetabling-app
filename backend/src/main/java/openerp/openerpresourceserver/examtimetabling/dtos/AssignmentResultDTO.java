package openerp.openerpresourceserver.examtimetabling.dtos;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class AssignmentResultDTO {
    private final UUID assignmentId;
    private final UUID roomId;
    private final UUID sessionId;
    private final LocalDate date;
    private final int weekNumber;
}

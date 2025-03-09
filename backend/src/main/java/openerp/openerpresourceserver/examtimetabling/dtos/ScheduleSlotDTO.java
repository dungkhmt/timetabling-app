package openerp.openerpresourceserver.examtimetabling.dtos;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class ScheduleSlotDTO {
    private final UUID sessionId;
    private final LocalDate date;
    private final int weekNumber;
}

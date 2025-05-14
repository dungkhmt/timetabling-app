package openerp.openerpresourceserver.examtimetabling.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class SessionDTO {
    private UUID id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String displayName;
    private boolean isUsing;
}

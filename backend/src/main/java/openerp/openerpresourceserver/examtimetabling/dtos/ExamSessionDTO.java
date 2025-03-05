package openerp.openerpresourceserver.examtimetabling.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ExamSessionDTO {
      private UUID id;
      private String name;
      private UUID examTimetableSessionCollectionId;
      private LocalDateTime startTime;
      private LocalDateTime endTime;
}

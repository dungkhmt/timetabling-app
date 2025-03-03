package openerp.openerpresourceserver.examtimetabling.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class ExamTimetableDetailDTO {
    private UUID id;
    private String name;
    private UUID examPlanId;
    private Integer planStartWeek;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private UUID sessionCollectionId;
    private String sessionCollectionName;
    private List<Integer> weeks;
    private List<DateDTO> dates;
    private List<SlotDTO> slots;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long completedAssignments;
    private long totalAssignments;
    
    @Data
    public static class DateDTO {
        private Integer weekNumber;
        private String name;
        private String date; // use format "dd/mm/yyyy"
    }
    
    @Data
    public static class SlotDTO {
        private UUID id;
        private String name;
    }
}

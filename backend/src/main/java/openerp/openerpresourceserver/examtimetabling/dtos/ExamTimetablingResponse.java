package openerp.openerpresourceserver.examtimetabling.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for exam timetabling response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamTimetablingResponse {
    
    // Whether the operation was successful
    private boolean success;
    
    // Message providing details about the result
    private String message;
    
    // Execution time in milliseconds (for testing)
    private Long executionTimeMs;
}

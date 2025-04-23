package openerp.openerpresourceserver.examtimetabling.algorithm.model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains details of a class assignment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDetails {
    private UUID timeSlotId;
    
    private UUID roomId;
    
    private UUID sessionId;
    
    private LocalDate date;
    
    /**
     * Check if this assignment conflicts with another one
     */
    public boolean conflictsWith(AssignmentDetails other) {
        // If they're on different dates or different sessions, no conflict
        if (!this.date.equals(other.date) || !this.sessionId.equals(other.sessionId)) {
            return false;
        }
        
        // If they're in the same room, they conflict
        return this.roomId.equals(other.roomId);
    }
}

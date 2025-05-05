package openerp.openerpresourceserver.examtimetabling.algorithm.model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a room at a specific time slot (session + date)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotRoomPair {
    private UUID sessionId;
    
    private LocalDate date;
    
    private String roomId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TimeSlotRoomPair that = (TimeSlotRoomPair) o;
        
        if (!sessionId.equals(that.sessionId)) return false;
        if (!date.equals(that.date)) return false;
        return roomId.equals(that.roomId);
    }
    
    @Override
    public int hashCode() {
        int result = sessionId.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + roomId.hashCode();
        return result;
    }
}

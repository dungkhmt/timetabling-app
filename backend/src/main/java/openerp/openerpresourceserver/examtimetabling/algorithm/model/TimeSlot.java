package openerp.openerpresourceserver.examtimetabling.algorithm.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a time slot (combination of session and date)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {
    // Unique identifier for this time slot
    private UUID id;
    
    // Reference to the session template
    private UUID sessionId;
    
    // The date for this time slot
    private LocalDate date;
    
    // Start and end times
    private LocalTime startTime;
    private LocalTime endTime;
    
    // Descriptive name
    private String name;
    
    /**
     * Check if this time slot is on the same day as another
     */
    public boolean isSameDayAs(TimeSlot other) {
        return this.date.equals(other.date);
    }
    
    /**
     * Check if this time slot is consecutive with another
     * (either immediately before or after)
     */
    public boolean isConsecutiveWith(TimeSlot other) {
        // Check if they're on the same day first
        if (!isSameDayAs(other)) {
            return false;
        }
        
        // Check if one ends when the other begins
        return this.endTime.equals(other.startTime) || 
               other.endTime.equals(this.startTime);
    }
    
    /**
     * Check if this time slot is on the day after another
     */
    public boolean isDayAfter(TimeSlot other) {
        return this.date.equals(other.date.plusDays(1));
    }
    
    /**
     * Check if this time slot is on the day before another
     */
    public boolean isDayBefore(TimeSlot other) {
        return this.date.equals(other.date.minusDays(1));
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TimeSlot timeSlot = (TimeSlot) o;
        return id.equals(timeSlot.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

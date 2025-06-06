package openerp.openerpresourceserver.examtimetabling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exam_timetable_assignment")
public class ExamTimetableAssignment {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "exam_timetable_id")
    private UUID examTimetableId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_timetable_id", insertable = false, updatable = false)
    private ExamTimetable examTimetable;
    
    @Column(name = "exam_timtabling_class_id")
    private UUID examTimetablingClassId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_timtabling_class_id", insertable = false, updatable = false)
    private ExamClass examClass;
    
    @Column(name = "room_id")
    private String roomId;

    @Column(name = "session")
    private String session;
    
    @Column(name = "exam_session_id")
    private UUID examSessionId;

    @Column(name = "week_number", nullable = true)
    private Integer weekNumber;
    
    @Column(name = "date", nullable = true)
    private LocalDate date;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

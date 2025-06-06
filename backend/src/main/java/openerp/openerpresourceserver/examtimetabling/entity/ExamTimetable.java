package openerp.openerpresourceserver.examtimetabling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exam_timetable")
public class ExamTimetable {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "exam_plan_id")
    private UUID examPlanId;

    @Column(name = "exam_timetable_session_collection_id")
    private UUID examTimetableSessionCollectionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_plan_id", insertable = false, updatable = false)
    private ExamPlan examPlan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_timetable_session_collection_id", insertable = false, updatable = false)
    private ExamTimetableSessionCollection sessionCollection;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = LocalDateTime.now();
    }
}

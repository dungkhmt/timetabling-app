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
@Table(name = "exam_timetable_session")
public class ExamTimetableSession {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "exam_timetable_session_collection_id")
    private UUID examTimetableSessionCollectionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_timetable_session_collection_id", insertable = false, updatable = false)
    private ExamTimetableSessionCollection sessionCollection;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
}

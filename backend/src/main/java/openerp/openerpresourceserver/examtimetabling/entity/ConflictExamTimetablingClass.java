package openerp.openerpresourceserver.examtimetabling.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conflict_exam_timetabling_class")
public class ConflictExamTimetablingClass {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "exam_timetabling_class_id_1", nullable = false)
    private UUID examTimetablingClassId1;
    
    @Column(name = "exam_timetabling_class_id_2", nullable = false)
    private UUID examTimetablingClassId2;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_timetabling_class_id_1", insertable = false, updatable = false)
    private ExamClass examClass1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_timetabling_class_id_2", insertable = false, updatable = false)
    private ExamClass examClass2;
}

package openerp.openerpresourceserver.generaltimetabling.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "timetabling_course")
public class Course {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "name")
    private String courseName;

    @Column(name="volumn")
    private String volumn;

    @Column(name = "slots_priority")
    private String slotsPriority;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdatedStamp;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @Column(name = "max_teacher_in_charge")
    private Integer maxTeacherInCharge;
}

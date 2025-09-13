package openerp.openerpresourceserver.teacherassignment.model.entity.relationship;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeTeacherCapacity;
import openerp.openerpresourceserver.thesisdefensejuryassignment.entity.Teacher;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "teacher_course_for_assignment_plan")
public class TeacherCapacity {
    @Id
    private CompositeTeacherCapacity id;

    private Integer priority;

    private String lastUpdatedStamp;

    private String createdStamp;
    private Integer score;

    @ManyToOne
    @JoinColumn(name = "teacherId", referencedColumnName = "id", insertable = false, updatable = false)
    @MapsId("teacherId")
    private Teacher teacher;

}

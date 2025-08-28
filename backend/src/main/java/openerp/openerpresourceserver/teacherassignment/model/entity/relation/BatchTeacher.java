package openerp.openerpresourceserver.teacherassignment.model.entity.relation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeBatchTeacher;
import openerp.openerpresourceserver.thesisdefensejuryassignment.entity.Teacher;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "teacherclassassignment_batch_teacher")
public class BatchTeacher {
    @EmbeddedId
    private CompositeBatchTeacher id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("batchId")
    @JoinColumn(name = "batchId", referencedColumnName = "id", insertable = false, updatable = false)
    private Batch batch;

    @ManyToOne
    @MapsId("teacherUserId")
    @JoinColumn(name = "teacher_user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Teacher teacher;

}

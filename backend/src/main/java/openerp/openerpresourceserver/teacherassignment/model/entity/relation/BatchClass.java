package openerp.openerpresourceserver.teacherassignment.model.entity.relation;

import jakarta.persistence.*;
import lombok.*;
import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;
import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;
import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeBatchClass;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "teacherclassassignment_batch_class")
public class BatchClass {

    @EmbeddedId
    private CompositeBatchClass id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("batchId")
    @JoinColumn(name = "batchId", referencedColumnName = "id", insertable = false, updatable = false)
    private Batch batch;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classId", referencedColumnName = "classId", insertable = false, updatable = false)
    private OpenedClass openedClass;


}

package openerp.openerpresourceserver.teacherassignment.model.entity.composite;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class CompositeBatchTeacher implements Serializable {
    private Long batchId;
    private String teacherUserId;

}

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
public class CompositeBatchClass implements Serializable {
    private Long batchId;
    private Long classId;
}

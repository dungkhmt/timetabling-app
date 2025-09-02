package openerp.openerpresourceserver.teacherassignment.model.entity.composite;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class CompositeTeacherCapacity implements Serializable {
    private String teacherId;
    private String courseId;
    private UUID planId;

}

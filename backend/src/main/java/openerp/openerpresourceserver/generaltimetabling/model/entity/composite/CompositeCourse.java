package openerp.openerpresourceserver.generaltimetabling.model.entity.composite;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CompositeCourse implements Serializable {
    private String courseId;
    private String typeProgram;
}

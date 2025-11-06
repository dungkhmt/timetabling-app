package openerp.openerpresourceserver.teacherassignment.model.entity.composite;

import lombok.*;
import openerp.openerpresourceserver.generaltimetabling.model.entity.OpenClassPlan;
import openerp.openerpresourceserver.teacherassignment.model.entity.Batch;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CompositeTimeClass implements Serializable {
    private Long classId;
    private Integer sessionNumber;
    private String week;
}

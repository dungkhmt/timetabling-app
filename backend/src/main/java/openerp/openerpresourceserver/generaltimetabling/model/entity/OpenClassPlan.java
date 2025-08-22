package openerp.openerpresourceserver.generaltimetabling.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeOpenClassPlan;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "open_classes_plan")
@IdClass(CompositeOpenClassPlan.class)
public class OpenClassPlan {


    private String typeProgram;

    @Id
    private String semester;

    @Id
    private Long classCode;
    private Long accompanyingClassCode;
    private String courseId;
    private String note;

    @Id
    private Long part;
    private Long dayOfWeek;
    private String startTime;
    private String endTime;
    private String group;
    private String weeks;

    @Id
    private String roomId;
    private Long maxQuantity;

}

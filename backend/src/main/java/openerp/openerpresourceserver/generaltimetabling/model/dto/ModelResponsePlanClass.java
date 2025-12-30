package openerp.openerpresourceserver.generaltimetabling.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelResponsePlanClass {
    private Long id;
    private Integer quantityMax;
    private String classType;
    private String mass;
    private String programName;
    private String moduleCode;
    private String moduleName;
    private String semester;
    private Integer numberOfClasses;
    private Integer lectureMaxQuantity;
    private Integer exerciseMaxQuantity;
    private Integer lectureExerciseMaxQuantity;
    private String learningWeeks;
    private String crew;
    private String weekType; // Ensures this field is in the model
    private int duration;
    private Date createdStamp;
    private String promotion;
    private Long groupId;
    private Long batchId;
    private String createdByUserId;

    private int nbClassScheduled;
}

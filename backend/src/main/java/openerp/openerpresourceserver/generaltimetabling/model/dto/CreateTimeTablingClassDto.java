package openerp.openerpresourceserver.generaltimetabling.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTimeTablingClassDto {
    private Long id;
    private Integer quantityMax;
    private Integer exerciseMaxQuantity;
    private Integer lectureExerciseMaxQuantity;
    private Integer lectureMaxQuantity;
    private String classType;
    //private int nbClasses;
    private Long parentClassId;// in case the new created class is a BT class, parentClassId is the LT class
    private String mass;
    private String programName;
    private String moduleCode;
    private String moduleName;
    private String semester;
    private String learningWeeks;
    private String crew;
    private String weekType;
    private int duration;
    private Long batchId;
    private String createdByUserId;
}
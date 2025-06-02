package openerp.openerpresourceserver.generaltimetabling.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateSubClassDto {
    private Long fromParentClassId;
    private String classType;
    private int numberStudents;
    private int duration;
    private int numberClasses;
}


package openerp.openerpresourceserver.examtimetabling.dtos;

import java.util.UUID;

import lombok.Data;

@Data
public class ConflictCreateDTO {
    private UUID examTimetablingClassId1;
    
    private UUID examTimetablingClassId2;
}

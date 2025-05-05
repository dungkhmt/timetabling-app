package openerp.openerpresourceserver.examtimetabling.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRoom {
    private String id;
    private String name;
    private Integer numberSeat;
    private Integer numberComputer;
}

package openerp.openerpresourceserver.examtimetabling.dtos;

import java.util.List;

import lombok.Data;

@Data
public class ConflictDTO {
    private String weekName;
    private String date;
    private String roomName;
    private String sessionName;
    private List<String> examClassIds;
}

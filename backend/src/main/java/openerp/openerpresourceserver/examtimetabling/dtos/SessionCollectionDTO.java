package openerp.openerpresourceserver.examtimetabling.dtos;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class SessionCollectionDTO {
    private UUID id;
    private String name;
    private List<SessionDTO> sessions;
}

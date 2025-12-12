package openerp.openerpresourceserver.generaltimetabling.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelSchedulingLog {
    private UUID uuid;
    private String classCode;
    private Long classSegmentId;
    private String description;
    private Date createdDate;
}

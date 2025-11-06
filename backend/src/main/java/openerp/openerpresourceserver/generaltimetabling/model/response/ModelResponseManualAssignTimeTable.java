package openerp.openerpresourceserver.generaltimetabling.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelResponseManualAssignTimeTable {
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_NOT_FOUND = "NOT_FOUND";
    public static final String STATUS_CONFLICT = "CONFLICT";
    public static final String STATUS_OUT_OF_RANGE = "OUT_OF_RANGE";
    private String status;
    private String message;
}

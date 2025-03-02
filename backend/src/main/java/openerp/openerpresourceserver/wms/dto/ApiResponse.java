package openerp.openerpresourceserver.wms.dto;

import lombok.Builder;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;
@Builder
@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
}

package openerp.openerpresourceserver.wms.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class Pagination<T> {
    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}

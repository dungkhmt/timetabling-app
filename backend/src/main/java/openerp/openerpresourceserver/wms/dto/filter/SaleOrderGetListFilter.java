package openerp.openerpresourceserver.wms.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleOrderGetListFilter {
    private String status;
    private LocalDateTime startCreatedAt;
    private LocalDateTime endCreatedAt;
    private String keyword;
    private List<String> saleChannelId;
}

package openerp.openerpresourceserver.wms.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipperGetListFilter {
    private String keyword;
    private String statusId;
    private String deliveryStatusId;
}

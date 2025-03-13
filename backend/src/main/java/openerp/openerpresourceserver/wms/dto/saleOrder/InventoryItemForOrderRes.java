package openerp.openerpresourceserver.wms.dto.saleOrder;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class InventoryItemForOrderRes {
    private String id;
    private String facilityName;
    private Integer quantity;
}

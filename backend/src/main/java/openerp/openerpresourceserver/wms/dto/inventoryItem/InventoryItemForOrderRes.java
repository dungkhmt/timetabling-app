package openerp.openerpresourceserver.wms.dto.inventoryItem;

import lombok.*;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class InventoryItemForOrderRes {
    private String productId;
    List<FacilityForOrderRes> facilityForOrderRes;
}

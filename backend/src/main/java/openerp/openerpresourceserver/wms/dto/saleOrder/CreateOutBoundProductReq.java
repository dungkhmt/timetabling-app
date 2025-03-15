package openerp.openerpresourceserver.wms.dto.saleOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateOutBoundProductReq {
    private String productId;
    private String inventoryItemId;
    private Integer quantity;
    private String orderId;
    private String orderItemSeqId;
}

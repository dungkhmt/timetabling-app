package openerp.openerpresourceserver.wms.dto.orderBillItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OrderItemBillingGetListRes {
    private String id;
    private String productId;
    private String productName;
    private String orderItemBillingTypeId;
    private String orderItemId;
    private Integer quantity;
    private String unit;
    private String facilityId;
    private String facilityName;
    private String createdStamp;
}

package openerp.openerpresourceserver.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItemReq {
    private String productId;
    private Integer quantity;
    private String price;
    private String unit;
}

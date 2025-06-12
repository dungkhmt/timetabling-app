package openerp.openerpresourceserver.wms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryBillProduct {
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private String unit;
    private BigDecimal weight;
}

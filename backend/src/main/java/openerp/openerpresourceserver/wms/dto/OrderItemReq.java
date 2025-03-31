package openerp.openerpresourceserver.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItemReq {
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private String unit;
    private BigDecimal discount;
    private BigDecimal tax;
}

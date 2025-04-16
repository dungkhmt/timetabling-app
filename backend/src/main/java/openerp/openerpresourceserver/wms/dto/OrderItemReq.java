package openerp.openerpresourceserver.wms.dto;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderItemReq {
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private String unit;
    private BigDecimal discount;
    private BigDecimal tax;
}

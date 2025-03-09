package openerp.openerpresourceserver.wms.dto.saleOrder;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderProductRes {
    private String id;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private String unit;
    private BigDecimal discount;
    private BigDecimal tax;
}

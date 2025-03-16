package openerp.openerpresourceserver.wms.dto.shipment;

import jakarta.persistence.Column;
import lombok.*;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OutBoundDetailProductRes {
    private String id;
    private String productId;
    private String productName;
    private Integer quantity;
    private String unit;
    private Integer requestedQuantity;
    private BigDecimal wholeSalePrice;
}

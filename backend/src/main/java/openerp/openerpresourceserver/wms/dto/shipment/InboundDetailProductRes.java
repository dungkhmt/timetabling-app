package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InboundDetailProductRes {
    private String id;
    private String productId;
    private String productName;
    private String facilityId;
    private String facilityName;
    private Integer quantity;
    private String unit;
    private Integer requestedQuantity;
    private BigDecimal price;
}

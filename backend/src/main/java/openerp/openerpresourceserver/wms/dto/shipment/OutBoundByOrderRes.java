package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OutBoundByOrderRes {
    private String id;
    private String shipmentTypeId;
    private String shipmentName;
    private String toCustomerName;
    private BigDecimal totalWeight;
    private Integer totalQuantity;
    private String statusId;
    private LocalDate expectedDeliveryDate;
}

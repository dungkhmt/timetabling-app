package openerp.openerpresourceserver.wms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryBillPlanRes {
    private String id;

    private String shipmentId;

    private String shipmentName;

    private String deliveryBillName;

    private Integer priority;

    private String sequenceId;

    private BigDecimal totalWeight;

    private String toCustomerName;

    private LocalDate expectedDeliveryDate;

    private String note;

    private String statusId;
}

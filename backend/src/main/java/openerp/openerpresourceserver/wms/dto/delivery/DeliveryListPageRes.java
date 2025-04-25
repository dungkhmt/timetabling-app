package openerp.openerpresourceserver.wms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class DeliveryListPageRes {
    private String id;
    private String deliveryBillName;

    private Integer priority;

    private String sequenceId;

    private BigDecimal totalWeight;

    private String customerName;

    private LocalDate expectedDeliveryDate;

    private String note;

    private String statusId;

}

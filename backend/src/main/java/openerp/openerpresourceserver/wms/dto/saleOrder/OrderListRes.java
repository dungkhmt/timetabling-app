package openerp.openerpresourceserver.wms.dto.saleOrder;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderListRes {
    private String id;
    private String customerName;
    private String status;
    private String facilityName;
    private LocalDateTime createdStamp;
    private BigDecimal totalAmount;
}

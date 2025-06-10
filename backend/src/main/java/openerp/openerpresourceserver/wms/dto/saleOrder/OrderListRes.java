package openerp.openerpresourceserver.wms.dto.saleOrder;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderListRes {
    private String id;
    private String customerName;
    private String statusId;
    private LocalDateTime createdStamp;
    private BigDecimal totalAmount;
    private Integer totalQuantity;
    private LocalDate orderDate;
    private String orderName;
    private String userCreatedName;
    private LocalDate deliveryAfterDate;
}

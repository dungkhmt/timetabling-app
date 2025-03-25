package openerp.openerpresourceserver.wms.dto.saleOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderListExportedRes {
    private String id;
    private String orderName;
    private LocalDateTime createdStamp;
    private String customerName;
    private String status;
    private Integer totalQuantity;
    private String saleChannelId;
    private BigDecimal totalAmount;
    private String customerId;
    private String deliveryAddress;
    private String deliveryPhone;
    private LocalDate deliveryAfterDate;
    private String note;
}

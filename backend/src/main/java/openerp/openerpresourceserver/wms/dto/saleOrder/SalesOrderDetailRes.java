package openerp.openerpresourceserver.wms.dto.saleOrder;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesOrderDetailRes {
    private String id;
    private Integer numberOfInvoices;
    private String orderTypeId;
    private String status;
    private String saleChannelId;
    private String facilityName;
    private String customerName;
    private String createdByUser;
    private String deliveryAddress;
    private String deliveryPhone;
    private LocalDateTime createdStamp;
    private LocalDate deliveryAfterDate;
    private String note;
    private Integer priority;
    private BigDecimal totalAmount;
    private Integer totalQuantity;
    private List<OrderProductRes> orderItems;
}

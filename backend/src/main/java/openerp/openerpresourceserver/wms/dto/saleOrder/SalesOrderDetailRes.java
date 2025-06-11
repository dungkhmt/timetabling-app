package openerp.openerpresourceserver.wms.dto.saleOrder;

import lombok.*;
import openerp.openerpresourceserver.wms.dto.OrderProductRes;

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
    private String orderTypeId;
    private String statusId;
    private String saleChannelId;
    private String toCustomerName;
    private String createdByUserName;
    private String deliveryFullAddress;
    private String deliveryAddressId;
    private String deliveryPhone;
    private LocalDateTime createdStamp;
    private LocalDate deliveryAfterDate;
    private LocalDate deliveryBeforeDate;
    private String note;
    private Integer priority;
    private BigDecimal totalAmount;
    private Integer totalQuantity;
    private List<OrderProductRes> orderItems;
}

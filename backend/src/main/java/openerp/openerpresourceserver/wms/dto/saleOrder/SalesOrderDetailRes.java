package openerp.openerpresourceserver.wms.dto.saleOrder;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesOrderDetailRes {
    private String id;
    private Integer numberOfInvoice;
    private String status;
    private String saleChannel;
    private String facility;
    private String customer;
    private String createdByUser;
    private String deliveryAddress;
    private String deliveryPhone;
    private LocalDateTime createdStamp;
    private LocalDateTime deliveryAfterDate;
    private String note;
    private Integer priority;
    private List<OrderProductRes> orderItems;
}

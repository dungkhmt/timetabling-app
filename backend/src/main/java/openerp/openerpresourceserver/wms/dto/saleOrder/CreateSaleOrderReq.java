package openerp.openerpresourceserver.wms.dto.saleOrder;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import openerp.openerpresourceserver.wms.dto.OrderItemReq;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CreateSaleOrderReq {
    private String id;
    @NotBlank
    private String toCustomerId;
    private String orderName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryBeforeDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryAfterDate;
    private String deliveryAddressId;
    private String deliveryFullAddress;
    private String deliveryPhone;
    private String note;
    private LocalDate orderDate;
    @NotBlank
    private String saleChannelId;
    private BigDecimal discount;
    @Size(min = 1, message = "At least one product is required")
    private List<OrderItemReq> orderItems;
}

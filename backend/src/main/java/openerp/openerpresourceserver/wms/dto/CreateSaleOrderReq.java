package openerp.openerpresourceserver.wms.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateSaleOrderReq {
    @NotBlank
    private String facilityId;
    @NotBlank
    private String customerId;
    private String saleOrderName;
    private Integer numberOfInvoice;
    private LocalDateTime deliveryBeforeDate;
    private LocalDateTime deliveryAfterDate;
    private String discountType;
    private BigDecimal discountValue;
    private String deliveryAddress;
    private String deliveryPhone;
    private String purpose;
    private String note;
    @NotBlank
    private String saleChannel;
    private String deliveryMethod;
    private String shippingCarrier;
    private Boolean isExportedInvoice;
    private List<OrderItemReq> orderItems;
}

package openerp.openerpresourceserver.wms.dto.saleOrder;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateSaleOrderReq {
    private String id;
    @NotBlank
    private String facilityId;
    @NotBlank
    private String customerId;
    @NotBlank
    private String userCreatedId;
    private String saleOrderName;
    private Integer numberOfInvoice;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryBeforeDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryAfterDate;
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

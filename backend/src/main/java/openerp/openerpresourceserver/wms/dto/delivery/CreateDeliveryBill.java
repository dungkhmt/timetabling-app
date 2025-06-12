package openerp.openerpresourceserver.wms.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryBill {
    private String id;
    @NotBlank
    private String shipmentId;
    @NotBlank
    private String facilityId;
    private String deliveryBillName;
    private String deliveryAddressId;
    private Integer priority;
    private String note;
    private LocalDate expectedDeliveryDate;
    private List<CreateDeliveryBillProduct> products;
}

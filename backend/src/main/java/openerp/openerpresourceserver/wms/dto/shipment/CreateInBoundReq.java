package openerp.openerpresourceserver.wms.dto.shipment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateInBoundReq {
    private String id;
    private String note;
    @NotBlank
    private String orderId;
    private String shipmentName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate expectedDeliveryDate;
    private List<CreateInBoundProductReq> products;
}

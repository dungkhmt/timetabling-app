package openerp.openerpresourceserver.wms.dto.shipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateInBoundProductReq {
    @NotBlank
    private String productId;
    @NotBlank
    private String facilityId;
    @NotBlank
    private String orderItemId;
    @NotNull
    private Integer quantity;

    private String lotId;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;
}

package openerp.openerpresourceserver.wms.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateProductPriceReq {
    private String id;

    @NotBlank
    private String productId;

    @NotNull
    private BigDecimal price;

    private String description;

    @NotNull
    private LocalDateTime startDate;

    private LocalDateTime endDate;
}

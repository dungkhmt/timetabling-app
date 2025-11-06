package openerp.openerpresourceserver.wms.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductReq {
    private String id;

    @NotBlank
    private String name;

    @NotNull
    private Double weight;

    private Double height;

    private String unit;

    @NotNull
    private BigDecimal costPrice;

    @NotNull
    private BigDecimal wholeSalePrice;

    private BigDecimal retailPrice;

    private String productCategoryId;

    private String statusId;

    private BigDecimal vatRate = BigDecimal.ZERO;
}

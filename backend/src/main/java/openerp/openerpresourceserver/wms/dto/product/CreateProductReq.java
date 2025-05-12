package openerp.openerpresourceserver.wms.dto.product;

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

    private String name;

    private Double weight;

    private Double height;

    private String unit;

    private BigDecimal costPrice;

    private BigDecimal wholeSalePrice;

    private BigDecimal retailPrice;

    private String productCategoryId;

    private String statusId;
}

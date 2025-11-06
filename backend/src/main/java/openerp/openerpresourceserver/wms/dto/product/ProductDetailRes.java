package openerp.openerpresourceserver.wms.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailRes {
    private String id;

    private String name;

    private Double weight;

    private Double height;

    private String unit;

    private BigDecimal costPrice;

    private BigDecimal wholeSalePrice;

    private BigDecimal retailPrice;

    private String productCategoryId;

    private String productCategoryName;

    private String statusId;

    private BigDecimal vatRate;
}

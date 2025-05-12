package openerp.openerpresourceserver.wms.dto.product;

import lombok.Data;
import openerp.openerpresourceserver.wms.entity.ProductCategory;

import java.math.BigDecimal;
@Data
public class ProductGetListRes {

    private String id;

    private String name;

    private Double weight;

    private Double height;

    private String unit;

    private BigDecimal costPrice;

    private BigDecimal wholeSalePrice;

    private BigDecimal retailPrice;

    private ProductCategory category;

    private String statusId;
}

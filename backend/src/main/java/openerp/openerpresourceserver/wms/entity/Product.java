package openerp.openerpresourceserver.wms.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "wms2_product")
public class Product extends BaseEntity {
    @Id
    private String id;

    private String name;

    private Double weight;

    private Double height;

    private String unit;

    private BigDecimal costPrice;

    private BigDecimal wholeSalePrice;

    private BigDecimal retailPrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    private String statusId;

    private String imageId;

    private String extraProps;
}

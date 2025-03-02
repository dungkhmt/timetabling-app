package openerp.openerpresourceserver.wms.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_product")
public class Product {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "height")
    private Double height;

    @Column(name = "unit", length = 40)
    private String unit;

    @Column(name = "cost_price", precision = 18, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "whole_sale_price", precision = 18, scale = 2)
    private BigDecimal wholeSalePrice;

    @Column(name = "retail_price", precision = 18, scale = 2)
    private BigDecimal retailPrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Column(name = "status_id", length = 100)
    private String statusId;

    @Column(name = "image_id", length = 40)
    private String imageId;

    @Column(name = "extra_props", length = 200)
    private String extraProps;

    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @Column(name = "last_updated_timestamp")
    private LocalDateTime lastUpdatedTimestamp;
}

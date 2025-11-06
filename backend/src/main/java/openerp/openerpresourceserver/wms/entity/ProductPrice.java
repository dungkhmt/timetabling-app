package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms2_product_price")
public class ProductPrice extends BaseEntity {
    @Id
    private String id;

    private String productId;

    private String statusId;

    private BigDecimal price;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}

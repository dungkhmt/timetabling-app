package openerp.openerpresourceserver.wms.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_product_category")
public class ProductCategory {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;
}

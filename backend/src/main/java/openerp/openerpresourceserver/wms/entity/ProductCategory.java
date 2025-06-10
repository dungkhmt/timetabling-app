package openerp.openerpresourceserver.wms.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_product_category")
public class ProductCategory {
    @Id
    private String id;

    private String name;
}

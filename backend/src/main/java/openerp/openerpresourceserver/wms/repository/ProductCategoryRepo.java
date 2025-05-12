package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepo extends JpaRepository<ProductCategory, String> {
}

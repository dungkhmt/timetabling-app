package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepo extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    Page<Product> findByNameContaining(String query, PageRequest pageRequest);
}

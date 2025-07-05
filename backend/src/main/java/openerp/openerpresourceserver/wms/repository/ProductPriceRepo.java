package openerp.openerpresourceserver.wms.repository;

import jakarta.validation.constraints.NotBlank;
import openerp.openerpresourceserver.wms.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductPriceRepo extends JpaRepository<ProductPrice, String> {

    List<ProductPrice> findAllByProductId(@NotBlank String productId);

    Optional<ProductPrice> findByProductIdAndStatusId(String productId, String name);

    List<ProductPrice> findAllByProductIdInAndStatusId(List<String> productIds, String name);
}

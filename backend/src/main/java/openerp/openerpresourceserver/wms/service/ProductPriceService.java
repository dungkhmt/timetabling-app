package openerp.openerpresourceserver.wms.service;

import jakarta.validation.Valid;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.product.CreateProductPriceReq;
import openerp.openerpresourceserver.wms.entity.ProductPrice;

import java.util.List;

public interface ProductPriceService {
    ApiResponse<Void> createProductPrice(@Valid CreateProductPriceReq req);

    ApiResponse<List<ProductPrice>> getProductPrice(String productId);
}

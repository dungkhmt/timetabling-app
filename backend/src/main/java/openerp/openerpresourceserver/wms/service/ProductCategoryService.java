package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.entity.ProductCategory;

import java.util.List;

public interface ProductCategoryService {
    ApiResponse<List<ProductCategory>> getProductCategories();
}
